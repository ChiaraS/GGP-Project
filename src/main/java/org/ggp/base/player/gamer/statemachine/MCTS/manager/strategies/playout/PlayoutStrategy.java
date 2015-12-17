package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

public interface PlayoutStrategy {

	public int[] playout(InternalPropnetMachineState state, int[] playoutVisitedNodes) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException, GoalDefinitionException;

}
