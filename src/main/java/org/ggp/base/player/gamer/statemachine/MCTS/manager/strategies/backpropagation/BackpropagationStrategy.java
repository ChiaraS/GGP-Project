package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCTDUCTJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.InternalPropnetDUCTMCTreeNode;

public interface BackpropagationStrategy {

	public void update(InternalPropnetDUCTMCTreeNode node, SUCTDUCTJointMove ductJointMove, int[] goals);

}
