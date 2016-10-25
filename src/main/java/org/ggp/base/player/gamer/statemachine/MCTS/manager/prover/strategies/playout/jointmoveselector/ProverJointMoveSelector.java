package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.jointmoveselector;

import java.util.List;

import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;

public interface ProverJointMoveSelector {

	public List<ProverMove> getJointMove(ProverMachineState state) throws MoveDefinitionException, StateMachineException;

	public String getJointMoveSelectorParameters();

	public String printJointMoveSelector();

}
