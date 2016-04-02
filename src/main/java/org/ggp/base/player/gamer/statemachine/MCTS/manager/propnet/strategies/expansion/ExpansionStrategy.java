package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.PnMCTSNode;

public interface ExpansionStrategy extends Strategy{

	public boolean expansionRequired(PnMCTSNode node);

	public MCTSJointMove expand(PnMCTSNode node);

}
