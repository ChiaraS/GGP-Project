package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.jointmoveselector;

import java.util.List;

import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public interface ProverJointMoveSelector {

	public List<ExplicitMove> getJointMove(ExplicitMachineState state) throws MoveDefinitionException, StateMachineException;

	public String getJointMoveSelectorParameters();

	public String printJointMoveSelector();

}
