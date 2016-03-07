package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;

public interface ExpansionStrategy extends Strategy{

	public boolean expansionRequired(InternalPropnetMCTSNode node);

	public MCTSJointMove expand(InternalPropnetMCTSNode node);

}
