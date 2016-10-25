package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnMCTSJointMove;

public interface PnExpansionStrategy extends Strategy{

	public boolean expansionRequired(MCTSNode node);

	public PnMCTSJointMove expand(MCTSNode node);

}
