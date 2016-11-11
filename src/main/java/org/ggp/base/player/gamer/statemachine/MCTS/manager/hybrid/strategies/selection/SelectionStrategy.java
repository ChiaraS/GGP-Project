package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;

public abstract class SelectionStrategy extends Strategy {

	public SelectionStrategy(GameDependentParameters gameDependentParameters) {
		super(gameDependentParameters);
	}

	/**
	 * This method selects the next move to visit in the given tree node.
	 * Note that the method assumes that the given state is a non-terminal
	 * state for each player there is at least one legal move in the state.
	 *
	 * @param currentNode the node for which to select an action to visit.
	 * @return the selected move.
	 */
	public abstract MCTSJointMove select(MCTSNode currentNode);

}
