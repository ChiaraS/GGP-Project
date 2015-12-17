package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public interface SelectionStrategy {

	public List<InternalPropnetMove> select(InternalPropnetDUCTMCTreeNode currentNode);

}
