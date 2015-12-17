package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public interface ExpansionStrategy {

	public boolean expansionRequired(InternalPropnetDUCTMCTreeNode node);

	public List<InternalPropnetMove> expand(InternalPropnetDUCTMCTreeNode node);

}
