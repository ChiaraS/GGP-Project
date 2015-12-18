package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;

public interface ExpansionStrategy {

	public boolean expansionRequired(InternalPropnetDUCTMCTreeNode node);

	public DUCTJointMove expand(InternalPropnetDUCTMCTreeNode node);

}
