package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector;

import java.util.List;

import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public interface JointMoveSelector {

	public List<Move> getJointMove(MachineState state) throws MoveDefinitionException, StateMachineException;

	public String getJointMoveSelectorParameters();

	public String printJointMoveSelector();


}
