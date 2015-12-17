package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public interface BackpropagationStrategy {

	public void update(InternalPropnetDUCTMCTreeNode node, List<InternalPropnetMove> jointMove, int[] goals);

}
