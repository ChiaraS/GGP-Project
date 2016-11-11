package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;

public abstract class ExpansionStrategy extends Strategy {

	public ExpansionStrategy(GameDependentParameters gameDependentParameters) {
		super(gameDependentParameters);
	}

	public abstract boolean expansionRequired(MCTSNode node);

	public abstract MCTSJointMove expand(MCTSNode node);

}
