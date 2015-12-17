package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public interface MoveChoiceStrategy {

	public InternalPropnetMove chooseBestMove(InternalPropnetDUCTMCTreeNode initialNode, InternalPropnetRole myRole);

}
