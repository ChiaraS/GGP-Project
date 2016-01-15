package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

public class RandomPlayout implements PlayoutStrategy {

	private InternalPropnetStateMachine theMachine;

	public RandomPlayout(InternalPropnetStateMachine theMachine){
		this.theMachine = theMachine;
	}

	@Override
	public int[] playout(InternalPropnetMachineState state, int[] playoutVisitedNodes, int maxDepth){

		InternalPropnetMachineState lastState;

		lastState = this.theMachine.performSafeLimitedDepthCharge(state, playoutVisitedNodes, maxDepth);

		// Now try to get the goals of the state.
		return this.theMachine.getSafeGoals(lastState);
	}

}
