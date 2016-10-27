package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector;

import java.util.List;

import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public interface PnJointMoveSelector{

	public List<CompactMove> getJointMove(CompactMachineState state) throws MoveDefinitionException;

	public String getJointMoveSelectorParameters();

	public String printJointMoveSelector();

}
