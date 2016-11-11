package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice;

import org.ggp.base.player.gamer.statemachine.MCS.manager.propnet.PnCompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.PnStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;

public interface PnMoveChoiceStrategy extends PnStrategy{

	public PnCompleteMoveStats chooseBestMove(MCTSNode initialNode);

}
