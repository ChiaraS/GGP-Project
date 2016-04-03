package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;

public interface ExpansionStrategy extends Strategy{

	public boolean expansionRequired(MCTSNode node);

	public MCTSJointMove expand(MCTSNode node);

}
