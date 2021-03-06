/**
 *
 */
package org.ggp.base.util.statemachine.hybrid;

import java.util.List;
import java.util.Random;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.implementation.yapProlog.YapStateMachine;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

/**
 * This class implements a state machine based on Yap Prolog that is backed up by the
 * Prover state machine. When the Yap state machine fails to answer to a query, this
 * state machine tries to answer using the prover state machine.
 *
 * NOTE: this state machine is specific for the use of the YapStateMachine as the main
 * machine and the ProverStateMachine as backup. This is because when the yap state
 * machine's initialization fails not because of the failure of the initialization/startup
 * of the Yap Prover engine but because of the failure of the computation of the initial
 * state and/or of the roles of the game, it can still be used. In this case a special
 * treatment is needed, that is the initial state and/or the roles must be computed
 * externally and then set in the YapStateMahcine. Other state machines might need a
 * different treatment or be totally unrecoverable, that's why this state machine cannot
 * be used for any possible state machine class.
 * TODO: fix this. E.g.: change the initialization methods so that, if they fail, no
 * recovery at all is possible, not even by an external state machine. Or create an abstract
 * methods common to all state machines that can make the state machine usable even after
 * the failure of the initialization if some parameters (e.g. initial state and roles
 * computed by another state machine) are made available to the state machine.
 *
 * @author C.Sironi
 *
 */
public class BackedYapStateMachine extends StateMachine {

	/**
	 * The main machine to be used to reason on the game.
	 */
	private YapStateMachine mainMachine;

	/**
	 * The machine to be used when the main machine fails to answer to a query.
	 */
	private ProverStateMachine backupMachine;

	/**
	 * Constructor that sets the main state machine that this state machine must use to
	 * answer queries and the backup state machine that this state machine must use to
	 * answer queries when the main state machine fails.
	 *
	 * @param mainMachine the main machine to be used to reason on the game.
	 * @param backupMachine the machine to be used when the main machine fails to answer
	 * to a query.
	 */
	public BackedYapStateMachine(Random random, YapStateMachine mainMachine, ProverStateMachine backupMachine) {

		super(random);

		this.mainMachine = mainMachine;
		this.backupMachine = backupMachine;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#initialize(java.util.List)
	 */
	@Override
	public void initialize(List<Gdl> description, long timeout){

		// TODO: move the description processing here. If used singularly the two state machines
		// both perform DistinctAndNotMover on the GDL description, but it might save time to compute
		// it only once here (seems not to be saving much time, usually just 3-4 milliseconds).
		// description = DistinctAndNotMover.run(description);
		// If you move it here remember to leave the instruction also in both the state machines
		// because they need it when running by themselves. Just find a way to tell them not to
		// process the description anymore if it already has been outside of their code.

		// Since the initialization of the prover state machine will always succeed we don't have to worry about
		// it failing. However it is reasonable to assume that the initialization of the BackedYapStateMachine
		// is also failed if the initialization of the machine that should act as a backup fails. Hence the
		// commented code throwing such exception. Uncomment the code in case the ProverStateMachine code will be
		// changed to let the initialization also throw an exception.
		//try{
		this.backupMachine.initialize(description, timeout);
		//}catch(StateMachineInitializationException e){
		//	GamerLogger.logError("StateMachine", "[BACKED YAP] Impossible to create a backed yap state machine because the initialization of backup machine failed!");
		//	GamerLogger.logStackTrace("StateMachine", e);
		//	throw new StateMachineInitializationException("State machine initialization failed. Impossible to initialize backup state machine!", e);
		//}

		try{
			this.mainMachine.initialize(description, timeout);
		}catch(StateMachineInitializationException e){
			if(this.mainMachine.isUsable()){

				GamerLogger.logError("StateMachine", "[BACKED YAP] Initialization of the main state machine failed. Recovering!");
				GamerLogger.logStackTrace("StateMachine", e);

				if(this.mainMachine.getExplicitInitialState() == null){
					this.mainMachine.setInitialState(this.backupMachine.getExplicitInitialState());
				}
				if(this.mainMachine.getExplicitRoles() == null){
					this.mainMachine.setRoles(this.backupMachine.getExplicitRoles());
				}
			}else{
				GamerLogger.logError("StateMachine", "[BACKED YAP] Initialization of the main state machine failed. Using only backup state machine!");
				GamerLogger.logStackTrace("StateMachine", e);
				this.mainMachine.shutdown();
				this.mainMachine = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getRoles()
	 */
	@Override
	public List<ExplicitRole> getExplicitRoles() {
		if(this.mainMachine != null){
			return this.mainMachine.getExplicitRoles();
		}else{
			return this.backupMachine.getExplicitRoles();
		}
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getInitialState()
	 */
	@Override
	public ExplicitMachineState getExplicitInitialState() {
		if(this.mainMachine != null){
			return this.mainMachine.getExplicitInitialState();
		}else{
			return this.backupMachine.getExplicitInitialState();
		}
	}

	@Override
	public List<Double> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role)
			throws StateMachineException {

		if(this.mainMachine != null){
			try{
				return this.mainMachine.getAllGoalsForOneRole(state, role);
			}catch(StateMachineException e){
				GamerLogger.logError("StateMachine", "[BACKED YAP] Failed to get one role goals. Falling back to backup machine.");
				GamerLogger.logStackTrace("StateMachine", e);
			}
		}

		return this.backupMachine.getAllGoalsForOneRole(state, role);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#isTerminal(org.ggp.base.util.statemachine.MachineState)
	 */
	@Override
	public boolean isTerminal(ExplicitMachineState state) /*throws StateMachineException*/ {

		if(this.mainMachine != null){
			try{
				return this.mainMachine.isTerminal(state);
			}catch(StateMachineException e){
				GamerLogger.logError("StateMachine", "[BACKED YAP] Failed to compute state terminality. Falling back to backup machine.");
				GamerLogger.logStackTrace("StateMachine", e);
			}
		}

		return this.backupMachine.isTerminal(state);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getLegalMoves(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role)
			throws MoveDefinitionException/*, StateMachineException*/ {

		if(this.mainMachine != null){
			try{
				return this.mainMachine.getExplicitLegalMoves(state, role);
			}catch(MoveDefinitionException | StateMachineException e){
				GamerLogger.logError("StateMachine", "[BACKED YAP] Failed to get legal moves. Falling back to backup machine.");
				GamerLogger.logStackTrace("StateMachine", e);
			}
		}

		return this.backupMachine.getExplicitLegalMoves(state, role);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getNextState(org.ggp.base.util.statemachine.MachineState, java.util.List)
	 */
	@Override
	public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves)
			throws TransitionDefinitionException/*, StateMachineException*/ {

		if(this.mainMachine != null){
			try{
				return this.mainMachine.getExplicitNextState(state, moves);
			}catch(TransitionDefinitionException | StateMachineException e){
				GamerLogger.logError("StateMachine", "[BACKED YAP] Failed to get next state. Falling back to backup machine.");
				GamerLogger.logStackTrace("StateMachine", e);
			}
		}

		return this.backupMachine.getExplicitNextState(state, moves);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#shutdown()
	 */
	@Override
	public void shutdown() {
		if(this.mainMachine != null){
			this.mainMachine.shutdown();
		}
		if(this.backupMachine != null){
			this.backupMachine.shutdown();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getName()
	 */
	@Override
    public String getName() {
		String mainMachineName = "null";
		String backupMachineName = "null";
        if(this.mainMachine != null){
            mainMachineName = this.mainMachine.getName();
        }
        if(this.backupMachine != null){
        	backupMachineName = this.backupMachine.getName();
        }
        return "Backed(" + mainMachineName + ", " + backupMachineName + ")";
    }

}
