package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.PnMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;

public interface SelectionStrategy extends Strategy{

	/**
	 * This method selects the next move to visit in the given tree node.
	 * Note that the method assumes that the given state is a non-terminal
	 * state for each player there is at least one legal move in the state.
	 *
	 * @param currentNode the node for which to select an action to visit.
	 * @return the selected move.
	 */
	public MCTSJointMove select(PnMCTSNode currentNode);

}
