package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.PnMCTSNode;

public interface BackpropagationStrategy extends Strategy {

	public void update(PnMCTSNode node, MCTSJointMove jointMove, int[] goals);

}
