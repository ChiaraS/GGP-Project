package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSCompleteMoveStats;

public interface MoveChoiceStrategy {

	public MCTSCompleteMoveStats chooseBestMove(InternalPropnetMCTSNode initialNode);

}
