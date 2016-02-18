package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSMoveStats;

public interface MoveChoiceStrategy {

	public MCTSMoveStats chooseBestMove(InternalPropnetMCTSNode initialNode);

}
