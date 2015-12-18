package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;

public interface BackpropagationStrategy {

	public void update(InternalPropnetDUCTMCTreeNode node, DUCTJointMove ductJointMove, int[] goals);

}
