package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

public class RandomPlayout implements PlayoutStrategy {

	private InternalPropnetStateMachine theMachine;

	public RandomPlayout(InternalPropnetStateMachine theMachine){
		this.theMachine = theMachine;
	}

	@Override
	public int[] playout(InternalPropnetMachineState state, int[] playoutVisitedNodes) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException, GoalDefinitionException {
		InternalPropnetMachineState terminalState = this.theMachine.performDepthCharge(state, playoutVisitedNodes);
		return this.theMachine.getGoals(terminalState);
	}

}
