package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.PnStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnMCTSJointMove;

public interface PnExpansionStrategy extends PnStrategy{

	public boolean expansionRequired(MctsNode node);

	public PnMCTSJointMove expand(MctsNode node);

}
