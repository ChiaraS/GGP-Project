package org.ggp.base.player.gamer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.ConfigurableStateMachineGamer;
import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.placeholders.FPGAPropnetLibrary;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetManager;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.FPGAPropnetStateMachine;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.abstractsm.FpgaStateMachine;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.symbol.grammar.SymbolPool;

public abstract class FpgaPropnetGamer extends ConfigurableStateMachineGamer {

	/**
	 * Used to tell to the player if and how to build the propnet state machine.
	 *
	 * More precisely:
	 * - ALWAYS = the player will build a new propnet and thus a new state machine
	 * 			  every time a new match starts (i.e. everytime the getInitialStateMachine
	 * 			  is called).
	 * - ONCE = the player will assume to always be playing the same game and thus
	 * 			it will use the same propnet state machine and build it only the
	 * 			first time. If the build succeeds this gamer will always use the same
	 * 			propnet state machine for every match, otherwise it will always return
	 * 			the prover state machine.
	 * 			ATTENTION! DANGER! If you use this setting make sure that the Game
	 * 			Manager always sends exactly the same game description or the player
	 *  		will start behaving in a weird way. The game description, for example,
	 *  		must not be scrambled or the names of the moves will be different and the
	 *  		gamer will return wrong moves.
	 * - NEVER = the player will never build a new propnet and thus never a new state
	 * 			 machine, but will use the one (if any) that is given as input to the
	 * 			 constructor.
	 *
	 * @author C.Sironi
	 *
	 */
    public enum PROPNET_BUILD{
    	ALWAYS, ONCE, NEVER
    }

    /*------------------------- THE FPGA PROPNET STATE MACHINE ---------------------------*/

	/**
	 * The personal reference to the propnet machine.
	 */
	protected FPGAPropnetStateMachine thePropnetMachine;

	/**
	 * True if this gamer never tried to build a propnet before.
	 * It is used when the gamer is assumed to play always the same game and returns the
	 * initial state machine. If this gamer has no state machine it might be because it is
	 * the first time the player is being used or because it already tried to build a state
	 * machine based on the propnet and failed, so we should avoid trying again.
	 */
	private boolean firstTry;

	/*----------------------- SETTINGS FOR THE STATE MACHINE ------------------------*/

	/**
	 * Its value tells how this gamer should deal with the propnet state machine:
	 * - build a new propnet for every new match
	 * - build the propnet only once for the first played game and then re-use always the same
	 *   (assumes the gamer will always play the same game).
	 * - never build the propnet but always use the one set from outside. NOTE that in this case
	 *   if no PropNet has been set the gamer will automatically fall back to the Prover
	 */
	protected PROPNET_BUILD propnetBuild;

	/**
	 * The player must complete metagaming with by the time [timeout - safetyMargin(ms)]
	 * to increase the certainty of answering to the Game Manager in time.
	 */
	protected long buildPnSafetyMargin;

	/*---------------------- SETTINGS FOR THE GAMER -------------------------*/

	/**
	 * The player must complete the executions of metagame by the time
	 * [timeout - metagameSafetyMargin(ms)] to increase the certainty
	 * of answering to the Game Manager in time.
	 */
	protected long metagameSafetyMargin;

	/**
	 * The player must complete the executions of the move selection method
	 * by the time [timeout - selectMoveSafetyMargin(ms)] to increase the
	 * certainty of answering to the Game Manager in time.
	 */
	protected long selectMoveSafetyMargin;

	/**
	 *
	 */
	public FpgaPropnetGamer() {

		this(GamerConfiguration.gamersSettingsFolderPath + "/" + defaultSettingsFileName);
		/*
		this.thePropnetMachine = null;
		this.useProver = false;
		this.propnetBuild = PROPNET_BUILD.ALWAYS;
		this.buildPnSafetyMargin = 5000L;
		this.firstTry = true;
		this.proverCache = true;
		this.pnCache = false;
		this.selectMoveSafetyMargin = 10000L;
		*/
	}

	public FpgaPropnetGamer(String settingsFilePath) {
		super(settingsFilePath);
	}

	@Override
	protected void configureGamer(GamerSettings gamerSettings){
		this.thePropnetMachine = null;
		this.firstTry = true;
		String propnetBuildString = gamerSettings.getPropertyValue("Gamer.propnetBuild");
		switch(propnetBuildString){
		case "ALWAYS": case "always":
			this.propnetBuild = PROPNET_BUILD.ALWAYS;
			break;
		case "ONCE": case "once":
			this.propnetBuild = PROPNET_BUILD.ONCE;
			break;
		case "NEVER": case "never":
			this.propnetBuild = PROPNET_BUILD.NEVER;
			break;
		default:
			GamerLogger.logError("Gamer", "Impossible to create gamer, wrong specification of property Gamer.propnetBuild:" + propnetBuildString + ".");
			throw new RuntimeException("Impossible to create gamer, wrong specification of property Gamer.propnetBuild:" + propnetBuildString + ".");
		}
		this.buildPnSafetyMargin = gamerSettings.getLongPropertyValue("Gamer.buildPnSafetyMargin");
		this.metagameSafetyMargin = gamerSettings.getLongPropertyValue("Gamer.metagameSafetyMargin");
		this.selectMoveSafetyMargin = gamerSettings.getLongPropertyValue("Gamer.selectMoveSafetyMargin");

	}

	/**
	 * This method sets the state machine with a state machine built externally and sets
	 * this game to always use such state machine without creating a new one. Always remember
	 * to also initialize the state machine before giving it as input to this method.
	 *
	 * This method is safe to use if called right after the initialization of the gamer.
	 * It should also be safe to use between matches.
	 * However, switching state machine while playing a match is potentially not safe and depends
	 * on the actual implementation of the state machine.
	 *
	 * @param thePropnetMachine the state machine to set.
	 */
	public void setExternalStateMachine(InternalPropnetStateMachine thePropnetMachine){

		if(thePropnetMachine instanceof SeparateInternalPropnetStateMachine) {
			this.thePropnetMachine = new FPGAPropnetStateMachine(this.random, new FPGAPropnetLibrary((SeparateInternalPropnetStateMachine)thePropnetMachine));

			this.propnetBuild = PROPNET_BUILD.NEVER;
		}else {
			GamerLogger.logError("Gamer", "Can only mock the FPGA propnet library using SeparateInternalPropnetStateMachine.");
		}
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#getInitialStateMachine()
	 */
	@Override
	public AbstractStateMachine getInitialStateMachine() {

		GamerLogger.log("Gamer", "Returning initial state machine.");

		switch(this.propnetBuild){
		case ALWAYS: // Create a new state machine for every game:
			GamerLogger.log("Gamer", "Standard gamer (not single-game).");
			GamerLogger.log("Gamer", "Creating state machine for the game.");

			this.thePropnetMachine = this.createStateMachine();
			break;
		case ONCE: // Build once, then re-use:

			GamerLogger.log("Gamer", "Single-game gamer.");

			// If the propnet machine already exists, return it.
			if(this.thePropnetMachine != null){

				GamerLogger.log("Gamer", "Propnet state machine already created for the game. Returning same state machine.");
				//System.out.println("Returning SAME propnet state machine.");

			}else{

				// Otherwise, if it doesn't exist because we never tried to build a propnet,
				// create it and return it.
				if(this.firstTry){

					this.firstTry = false;

					GamerLogger.log("Gamer", "First try to create the propnet state machine.");

					this.thePropnetMachine = this.createStateMachine();

				}else{

					GamerLogger.log("Gamer", "Already tried to build propnet and failed. Returning prover state machine.");
					//System.out.println("Already FAILED with propnet, not gonna try again: returning prover state machine.");

				}
			}
			break;
		case NEVER:
			break;
		}

		if(this.thePropnetMachine != null){
			GamerLogger.log("Gamer", "Returning FPGA PropNet state machine without cache.");
			return new FpgaStateMachine(this.thePropnetMachine);
		}else{
			GamerLogger.logError("Gamer", "Impossible to create gamer, creation of FPGAStateMachine failed!");
			throw new RuntimeException("Impossible to create gamer, creation of FPGAStateMachine failed!");
		}

	}

	private FPGAPropnetStateMachine createStateMachine(){
		if(System.currentTimeMillis() < this.getMetagamingTimeout() - this.buildPnSafetyMargin){


			/***************** CHANGE FOLLOWING CODE TO INITIALIZE THE FPGA LIBRARY *******************
			 * For now here we create the software propnet and use it to initialize a fake FPGA library
			 * to test the rest of the code.
			 */

	        // Create the executor service that will run the propnet manager that creates the propnet
	        ExecutorService executor = Executors.newSingleThreadExecutor();

	        // Create the propnet creation manager
	        SeparateInternalPropnetManager manager = new SeparateInternalPropnetManager(getMatch().getGame().getRules(), this.getMetagamingTimeout());

	        // Start the manager
	  	  	executor.execute(manager);

	  	  	// Shutdown executor to tell it not to accept any more task to execute.
			// Note that this doesn't interrupt previously started tasks.
			executor.shutdown();

			// Tell the executor to wait until the currently running task has completed execution or the timeout has elapsed.
			try{
				executor.awaitTermination(this.getMetagamingTimeout() - System.currentTimeMillis() - this.buildPnSafetyMargin, TimeUnit.MILLISECONDS);
			}catch(InterruptedException e){ // The thread running the gamer has been interrupted => stop playing.
				executor.shutdownNow(); // Interrupt everything
				GamerLogger.logError("Gamer", "Gamer interrupted while computing initial propnet state machine: returning prover state machine.");
				GamerLogger.logStackTrace("Gamer", e);
				Thread.currentThread().interrupt();

				//System.out.println("Returning prover state machine.");

				//return new CachedStateMachine(new ProverStateMachine());
				return null;
			}

			// Here the available time has elapsed, so we must interrupt the thread if it is still running.
			executor.shutdownNow();

			// If the thread is terminated, we can get the propnet, otherwise we return the prover.
			// TODO: if the thread isn't terminated, we don't wait for it to do so, we ignore the propnet
			// and give back the prover. A check must be added: it could be that the manager built the
			// propnet but it is still busy optimizing it. In this case the last completed optimization
			// of the propnet is usable so we should not discard it! For example, if the thread isn't
			// terminated, we could wait for half of the time still available and check again. We cannot
			// get the propnet before being sure that the manager has terminated, otherwise we risk getting
			// one in an inconsistent state. Also the manager must be fixed so that if it gets interrupted
			// while running an optimization it can return the propnet and its state at the previous optimization.
			if(executor.isTerminated()){

				// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.
				ImmutablePropNet propnet = manager.getImmutablePropnet();
				ImmutableSeparatePropnetState propnetState = manager.getInitialPropnetState();

				if(propnet != null && propnetState != null){

					// Create the state machine giving it the propnet and the propnet state.
				    //this.thePropnetMachine =  new SeparateInternalPropnetStateMachine(propnet, propnetState);

				    GamerLogger.log("Gamer", "Propnet built successfully: returning FPGA propnet state machine.");
				    //System.out.println("Returning propnet state machine.");

				    //return this.thePropnetMachine;

				    SeparateInternalPropnetStateMachine softwarePropnetMachine = new SeparateInternalPropnetStateMachine(this.random, propnet, propnetState);

				    return new FPGAPropnetStateMachine(this.random, new FPGAPropnetLibrary(softwarePropnetMachine));

				}else{
					GamerLogger.logError("Gamer", "Propnet builder ended execution but at leas one among the immutable propnet structure and the propnet state is null: returning prover state machine.");
				}
			}else{
				GamerLogger.logError("Gamer", "The propnet state machine didn't build in time: returning prover state machine.");
			}
		}else{
			GamerLogger.logError("Gamer", "No time to build propnet state machine: returning prover state machine.");
		}

		//System.out.println("Returning prover state machine.");

		//return new CachedStateMachine(new ProverStateMachine());
		return null;
	}



	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineStop()
	 */
	@Override
	public void stateMachineStop() {
		// TODO: ATTENTION! You should add a different check here. The garbage collector can be called and the pool drained ONLY if there is just ONE player active in the whole program!
		if(this.propnetBuild == PROPNET_BUILD.ALWAYS){
			GdlPool.drainPool();
		    SymbolPool.drainPool();

		    long endGCTime = System.currentTimeMillis() + 3000;
		    for (int ii = 0; ii < 1000 && System.currentTimeMillis() < endGCTime; ii++){
		        System.gc();
		        try {Thread.sleep(1);} catch (InterruptedException lEx) {/* Whatever */}
		    }
		}
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineAbort()
	 */
	@Override
	public void stateMachineAbort() {
		// TODO: ATTENTION! You should add a different check here. The garbage collector can be called and the pool drained ONLY if there is just ONE player active in the whole program!
		if(this.propnetBuild == PROPNET_BUILD.ALWAYS){

			GdlPool.drainPool();
		    SymbolPool.drainPool();

		    long endGCTime = System.currentTimeMillis() + 3000;
		    for (int ii = 0; ii < 1000 && System.currentTimeMillis() < endGCTime; ii++){
		        System.gc();
		        try {Thread.sleep(1);} catch (InterruptedException lEx) {/* Whatever */}
		    }

		}
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.Gamer#preview(org.ggp.base.util.game.Game, long)
	 */
	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// TODO Auto-generated method stub

	}

	@Override
	protected String printGamer(){
		return super.printGamer() +
				"\nSTATE_MACHINE_TYPE = " + (this.getStateMachine() == null ? "null" : this.getStateMachine().getName()) +
				"\nPROPNET_MACHINE_TYPE = " + (this.thePropnetMachine == null ? "null" : this.thePropnetMachine.getClass().getSimpleName()) +
				"\nPROPNET_BUILD = " + this.propnetBuild +
				"\nBUILD_PN_SAFETY_MARGIN = " + this.buildPnSafetyMargin +
				"\nMETAGAME_SAFETY_MARGIN = " + this.metagameSafetyMargin +
				"\nSELECT_MOVE_SAFETY_MARGIN = " + this.selectMoveSafetyMargin;
	}


}
