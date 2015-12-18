package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;

public interface SelectionStrategy {

	public DUCTJointMove select(InternalPropnetDUCTMCTreeNode currentNode);

}
