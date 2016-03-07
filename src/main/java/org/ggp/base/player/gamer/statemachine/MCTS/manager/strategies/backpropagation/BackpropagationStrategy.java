package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;

public interface BackpropagationStrategy extends Strategy {

	public void update(InternalPropnetMCTSNode node, MCTSJointMove jointMove, int[] goals);

}
