package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice;

import org.ggp.base.player.gamer.statemachine.MCS.manager.propnet.PnCompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;

public interface PnMoveChoiceStrategy extends Strategy{

	public PnCompleteMoveStats chooseBestMove(MCTSNode initialNode);

}
