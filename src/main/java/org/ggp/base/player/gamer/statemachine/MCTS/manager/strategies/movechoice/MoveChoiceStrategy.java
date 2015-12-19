package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public interface MoveChoiceStrategy {

	public DUCTMove chooseBestMove(InternalPropnetDUCTMCTreeNode initialNode, InternalPropnetRole myRole);

}
