package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.singlemoveselector;

import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public interface SingleMoveSelector {

	public Move getMoveForRole(MachineState state, int roleIndex) throws MoveDefinitionException, StateMachineException;

	public String getSingleMoveSelectorParameters();

	public String printSingleMoveSelector();

}
