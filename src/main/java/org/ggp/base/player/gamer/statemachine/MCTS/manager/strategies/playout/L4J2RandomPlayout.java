package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import org.ggp.base.util.statemachine.L4J2InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

public class L4J2RandomPlayout implements PlayoutStrategy {

	private L4J2InternalPropnetStateMachine theMachine;

	public L4J2RandomPlayout(L4J2InternalPropnetStateMachine theMachine){
		this.theMachine = theMachine;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.PlayoutStrategy#playout(org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState, int[], int)
	 */
	@Override
	public int[] playout(InternalPropnetMachineState state, int[] playoutVisitedNodes, int maxDepth){

		InternalPropnetMachineState lastState;

		lastState = this.theMachine.performSafeLimitedDepthCharge(state, playoutVisitedNodes, maxDepth);

		// Now try to get the goals of the state.
		return this.theMachine.getSafeGoals(lastState);
	}

}
