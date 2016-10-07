package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.jointmoveselector.ProverJointMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverSimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class ProverStandardPlayout implements ProverPlayoutStrategy {

	protected StateMachine theMachine;

	protected ProverJointMoveSelector jointMoveSelector;

	public ProverStandardPlayout(StateMachine theMachine, ProverJointMoveSelector jointMoveSelector) {
		this.theMachine = theMachine;
		this.jointMoveSelector = jointMoveSelector;
	}

	@Override
	public ProverSimulationResult playout(MachineState state,
			int[] playoutVisitedNodes, int maxDepth) {
		//InternalPropnetMachineState lastState;

        boolean terminal = true;

        try {
			terminal = this.theMachine.isTerminal(state);
		} catch (StateMachineException e) {
			GamerLogger.logError("MCTSManager", "Exception computing state terminality while performing a playout.");
			GamerLogger.logStackTrace("MCTSManager", e);
			terminal = true;
		}

		if(terminal || maxDepth == 0){

			if(playoutVisitedNodes != null)
	        	playoutVisitedNodes[0] = 0;

			return null;
		}

        int nDepth = 0;

        List<Move> jointMove;

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
				state = this.theMachine.getNextState(state, jointMove);
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
        }while(nDepth < maxDepth && !terminal) ;
        if(playoutVisitedNodes != null)
        	playoutVisitedNodes[0] = nDepth;

        //System.out.println("Playout state erminal: " + this.theMachine.isTerminal(state));

		//lastState = this.theMachine.performSafeLimitedDepthCharge(state, playoutVisitedNodes, maxDepth);

		// Now try to get the goals of the state.
		return new ProverSimulationResult(this.theMachine.getSafeGoalsAvg(state));
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
