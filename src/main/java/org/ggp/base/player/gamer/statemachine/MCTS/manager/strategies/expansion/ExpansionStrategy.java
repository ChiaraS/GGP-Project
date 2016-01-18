package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTreeNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCTDUCTJointMove;

public interface ExpansionStrategy {

	public boolean expansionRequired(InternalPropnetMCTreeNode node);

	public SUCTDUCTJointMove expand(InternalPropnetMCTreeNode node);

}
