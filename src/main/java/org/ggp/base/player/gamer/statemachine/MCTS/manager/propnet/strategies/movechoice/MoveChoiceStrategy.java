package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice;

import org.ggp.base.player.gamer.statemachine.MCS.manager.propnet.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;

public interface MoveChoiceStrategy extends Strategy{

	public CompleteMoveStats chooseBestMove(MCTSNode initialNode);

}
