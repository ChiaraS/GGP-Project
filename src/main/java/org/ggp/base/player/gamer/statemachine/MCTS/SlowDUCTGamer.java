/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.exceptions.MCTSException;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.StandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.RandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.DUCTSelection;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.creationManager.SeparatePropnetCreationManager;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

/**
 * This player performs Decoupled UCT Monte Carlo Tree Search.
 *
 * At the beginning of each game it tries to build the propnet. If it builds it will use for the
 * whole game the state machine based on the propnet itherwise it will use the cached prover.
 *
 * Depending on the chosen state machine it will perform DUCT using the corresponding tree structure
 * (i.e. the one that uses internal propnet states to perform MCTS if the propnet managed to build,
 * the one that uses standard states otherwise).
 *
 * @author C.Sironi
 *
 */
public class SlowDUCTGamer extends StateMachineGamer {

	/**
	 * The player must complete the executions of methods with a timeout by the time
	 * [timeout - safetyMargin(ms)] to increase the certainty of answering to the Game
	 * Manager in time.
	 */
	private long safetyMargin;

	/**
	 * Game step. Keeps track of the current game step.
	 */
	private int gameStep;


	/**
	 * Parameters used by the mcts manager
	 */
	private double c;
	private double uctOffset;
	private int gameStepOffset;
	private int maxSearchDepth;


	/**
	 * The class that takes care of performing Monte Carlo tree search.
	 */
	private InternalPropnetMCTSManager mctsManager;

	/**
	 *
	 */
	public SlowDUCTGamer() {
		// TODO: change code so that the parameters can be set from outside.
		this.safetyMargin = 1000L;

		this.c = 0.7;
		this.uctOffset = 0.01;
		this.gameStepOffset = 2;
		this.maxSearchDepth = 1000;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#getInitialStateMachine()
	 */
	@Override
	public StateMachine getInitialStateMachine() {

		if(System.currentTimeMillis() < this.getMetagamingTimeout() - this.safetyMargin){

	        // Create the executor service that will run the propnet manager that creates the propnet
	        ExecutorService executor = Executors.newSingleThreadExecutor();

	        // Create the propnet creation manager
	        SeparatePropnetCreationManager manager = new SeparatePropnetCreationManager(getMatch().getGame().getRules(), this.getMetagamingTimeout());

	        // Start the manager
	  	  	executor.execute(manager);

	  	  	// Shutdown executor to tell it not to accept any more task to execute.
			// Note that this doesn't interrupt previously started tasks.
			executor.shutdown();

			// Tell the executor to wait until the currently running task has completed execution or the timeout has elapsed.
			try{
				executor.awaitTermination(this.getMetagamingTimeout() - System.currentTimeMillis() - this.safetyMargin, TimeUnit.MILLISECONDS);
			}catch(InterruptedException e){ // The thread running the gamer has been interrupted => stop playing.
				executor.shutdownNow(); // Interrupt everything
				GamerLogger.logError("Gamer", "Gamer interrupted while computing initial state machine.");
				GamerLogger.logStackTrace("Gamer", e);
				Thread.currentThread().interrupt();
				return new CachedStateMachine(new ProverStateMachine());
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
				    return new SeparateInternalPropnetStateMachine(propnet, propnetState);
				}
			}
		}

		return new CachedStateMachine(new ProverStateMachine());
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineMetaGame(long)
	 */
	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		// For now the player can play only with the state machine based on the propnet.
		// TODO: temporary solution! FIX!
		// We throw an exception if the state machine based on the propnet couldn't be initialized.
		if(!(this.getStateMachine() instanceof SeparateInternalPropnetStateMachine)){
			throw new StateMachineException("Impossible to play without the state machine based on the propnet.");
		}

		this.gameStep = 0;

		SeparateInternalPropnetStateMachine thePropnetMachine = (SeparateInternalPropnetStateMachine) this.getStateMachine();
		Random r = new Random();
		InternalPropnetRole myRole = thePropnetMachine.getInternalRoles()[thePropnetMachine.getRoleIndices().get(this.getRole())];

		// Create the MCTS manager and start simulations.
		this.mctsManager = new InternalPropnetMCTSManager(new DUCTSelection(r, uctOffset, c),
	       		new RandomExpansion(r), new RandomPlayout(thePropnetMachine), new StandardBackpropagation(),
	       		new MaximumScoreChoice(r), thePropnetMachine, myRole, gameStepOffset, maxSearchDepth);

		//FIX!
		// If search fails during metagame?? TODO: should i throw exception here and say i'm not able to play?
		// If i don't it'll throw exception later anyway! better stop now?
		try {
			this.mctsManager.search(thePropnetMachine.getInternalInitialState(), timeout, gameStep);
		} catch (MCTSException e) {
			GamerLogger.logError("Gamer", "Exception during search while metagaming.");
			GamerLogger.logStackTrace("Gamer", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineStop()
	 */
	@Override
	public void stateMachineStop() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineAbort()
	 */
	@Override
	public void stateMachineAbort() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.Gamer#preview(org.ggp.base.util.game.Game, long)
	 */
	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.Gamer#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
