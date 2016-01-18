package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public interface MoveChoiceStrategy {

	public MCTSMove chooseBestMove(InternalPropnetDUCTMCTreeNode initialNode, InternalPropnetRole myRole);

}
