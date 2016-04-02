package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.jointmoveselector;

import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;

public interface ProverJointMoveSelector {

	public List<Move> getJointMove(MachineState state) throws MoveDefinitionException, StateMachineException;

	public String getJointMoveSelectorParameters();

	public String printJointMoveSelector();

}
