package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.structure.Move;

public abstract class MoveEvaluator extends SearchManagerComponent{

	public MoveEvaluator(GameDependentParameters gameDependentParameters) {
		super(gameDependentParameters);
	}

	/**
	 * Computes the value of the move that will be used by the selection strategy to choose the best move.
	 * This value could be for example computed as the UCT value fo the move.
	 *
	 * @param theNode the MCTS tree node for which we want to select the best move.
	 * @param theMove the move in the node for which we currently want to compute the value that will be
	 * used for the selection of the best move.
	 * @param roleIndex the index of the role that performs the move for which we are computing the value.
	 * @param theMoveStats the statistics collected during search for the move we want to evaluate.
	 * @return the value of the move.
	 */
	public abstract double computeMoveValue(MCTSNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats);

	public abstract String getEvaluatorParameters();

	public abstract String printEvaluator();


}
