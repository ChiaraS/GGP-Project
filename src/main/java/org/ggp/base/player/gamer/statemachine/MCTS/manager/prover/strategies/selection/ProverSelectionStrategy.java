package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.PnStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverMCTSJointMove;

public interface ProverSelectionStrategy extends PnStrategy {

	/**
	 * This method selects the next move to visit in the given tree node.
	 * Note that the method assumes that the given state is a non-terminal
	 * state for each player there is at least one legal move in the state.
	 *
	 * @param currentNode the node for which to select an action to visit.
	 * @return the selected move.
	 */
	public ProverMCTSJointMove select(MctsNode currentNode);


}
