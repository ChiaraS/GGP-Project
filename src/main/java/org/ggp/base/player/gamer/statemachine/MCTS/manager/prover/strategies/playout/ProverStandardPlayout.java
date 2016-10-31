package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.jointmoveselector.ProverJointMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverSimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public class ProverStandardPlayout implements ProverPlayoutStrategy {

	protected StateMachine theMachine;

	protected ProverJointMoveSelector jointMoveSelector;

	public ProverStandardPlayout(StateMachine theMachine, ProverJointMoveSelector jointMoveSelector) {
		this.theMachine = theMachine;
		this.jointMoveSelector = jointMoveSelector;
	}

	@Override
	public ProverSimulationResult playout(ExplicitMachineState state, int maxDepth) {
		//MachineState lastState;

        boolean terminal = true;

        try {
			terminal = this.theMachine.isTerminal(state);
		} catch (StateMachineException e) {
			GamerLogger.logError("MCTSManager", "Exception computing state terminality while performing a playout.");
			GamerLogger.logStackTrace("MCTSManager", e);
			terminal = true;
		}

		// NOTE that this is just an extra check: if the state is terminal or the depth limit has been reached,
		// we just return the final goals of the state. At the moment the MCTS manager already doesn't call the
        // play-out if the state is terminal or if the depth limit has been reached, so this check will never be
        // true, but it's here just to be safe.
        // ALSO NOTE that the instruction "terminal = this.theMachine.isTerminal(state);" shouldn't throw an exception
        // here because the MCTSManager already called it to check the terminality of the state and thus already
        // dealt with a possible exception.
		if(terminal || maxDepth == 0){

			//if(playoutVisitedNodes != null)
	        //	playoutVisitedNodes[0] = 0;

			GamerLogger.logError("MCTSManager", "Playout strategy shouldn't be called on a terminal node. The MCTSManager must take care of computing the simulation result in this case.");
			//throw new RuntimeException("Playout strategy called on a terminal node.");

			return new ProverSimulationResult(0, this.theMachine.getSafeGoalsAvg(state));

		}

        int nDepth = 0;

        List<ExplicitMove> jointMove;

        do{

        	jointMove = null;
			try {
				jointMove = this.jointMoveSelector.getJointMove(state);
			} catch (MoveDefinitionException | StateMachineException e) {
				GamerLogger.logError("MCTSManager", "Exception getting a joint move while performing a playout.");
				GamerLogger.logStackTrace("MCTSManager", e);
				break;
			}
			try {
				state = this.theMachine.getExplicitNextState(state, jointMove);
			} catch (TransitionDefinitionException | StateMachineException e) {
				GamerLogger.logError("MCTSManager", "Exception getting the next state while performing a playout.");
				GamerLogger.logStackTrace("MCTSManager", e);
				break;
			}
            nDepth++;

            try {
				terminal = this.theMachine.isTerminal(state);
			} catch (StateMachineException e) {
				GamerLogger.logError("MCTSManager", "Exception computing state terminality while performing a playout.");
				GamerLogger.logStackTrace("MCTSManager", e);
				terminal = true;
				break;
			}
        }while(nDepth < maxDepth && !terminal);

        //if(playoutVisitedNodes != null)
        	//playoutVisitedNodes[0] = nDepth;

        //System.out.println("Playout state erminal: " + this.theMachine.isTerminal(state));

		//lastState = this.theMachine.performSafeLimitedDepthCharge(state, playoutVisitedNodes, maxDepth);

		// Now try to get the goals of the state.
		return new ProverSimulationResult(nDepth, this.theMachine.getSafeGoalsAvg(state));
	}

	/*
	public List<Move> getJointMove(MachineState state) throws MoveDefinitionException, StateMachineException{
		return this.jointMoveSelector.getJointMove(state);
	}
	*/

	@Override
	public String getStrategyParameters() {
		return this.jointMoveSelector.printJointMoveSelector();
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}


}
