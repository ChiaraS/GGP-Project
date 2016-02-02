package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import org.ggp.base.util.statemachine.implementation.internalPropnet.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.internalPropnet.structure.InternalPropnetMachineState;

public class RandomPlayout implements PlayoutStrategy {

	private InternalPropnetStateMachine theMachine;

	public RandomPlayout(InternalPropnetStateMachine theMachine){
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

	// ERROR TEST - START
	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.PlayoutStrategy#playout(org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState, int[], int)
	 */
	/*
	@Override
	public int[] playout(InternalPropnetMachineState state, int[] playoutVisitedNodes, int maxDepth, List<List<InternalPropnetMove>> errorPath, boolean[] error){

		InternalPropnetMachineState lastState;

		lastState = this.theMachine.performSafeLimitedDepthCharge(state, playoutVisitedNodes, maxDepth, errorPath, error);

		// Now try to get the goals of the state.
		return this.theMachine.getSafeGoals(lastState);
	}
	*/
	// ERROR TEST - END

}
