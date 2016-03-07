package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice;

import org.ggp.base.player.gamer.statemachine.MCS.manager.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;

public interface MoveChoiceStrategy extends Strategy{

	public CompleteMoveStats chooseBestMove(InternalPropnetMCTSNode initialNode);

}
