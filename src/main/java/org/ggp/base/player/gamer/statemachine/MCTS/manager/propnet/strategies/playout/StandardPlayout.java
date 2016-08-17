package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector.JointMoveSelector;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class StandardPlayout implements PlayoutStrategy {

	protected InternalPropnetStateMachine theMachine;

	protected JointMoveSelector jointMoveSelector;

	public StandardPlayout(InternalPropnetStateMachine theMachine, JointMoveSelector jointMoveSelector) {
		this.theMachine = theMachine;
		this.jointMoveSelector = jointMoveSelector;
	}

	@Override
	public int[] playout(InternalPropnetMachineState state,
			int[] playoutVisitedNodes, int maxDepth) {
		//InternalPropnetMachineState lastState;

        int nDepth = 0;

        while(nDepth < maxDepth && !this.theMachine.isTerminal(state)) {

        	List<InternalPropnetMove> jointMove = null;
			try {
				jointMove = this.getJointMove(state);
			} catch (MoveDefinitionException e) {
				GamerLogger.logError("MCTSManager", "Exception getting a joint move while performing a playout.");
				GamerLogger.logStackTrace("MCTSManager", e);
				break;
			}
			state = this.theMachine.getInternalNextState(state, jointMove);
            nDepth++;
        }
        if(playoutVisitedNodes != null)
        	playoutVisitedNodes[0] = nDepth;

        //System.out.println("Playout state erminal: " + this.theMachine.isTerminal(state));

		//lastState = this.theMachine.performSafeLimitedDepthCharge(state, playoutVisitedNodes, maxDepth);

		// Now try to get the goals of the state.
		return this.theMachine.getSafeGoalsAvg(state);
	}

	public List<InternalPropnetMove> getJointMove(InternalPropnetMachineState state) throws MoveDefinitionException{
		return this.jointMoveSelector.getJointMove(state);
	}

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
