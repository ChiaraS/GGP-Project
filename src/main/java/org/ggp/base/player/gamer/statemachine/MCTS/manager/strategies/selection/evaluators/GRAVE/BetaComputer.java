package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.evaluators.GRAVE;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;

public interface BetaComputer {

	/**
	 * Computes the value of the weight beta used by the GRAVE move evaluator during selection.
	 *
	 * @param theMoveStats the statistics for the move being evaluated in the node.
	 * @param theAmafMoveStats the AMAF statistics for the move being evaluated.
	 * @param nodeVisits the visits of the node for which this move is being evaluated.
	 * @return the weight to be used in the computation of the value of the move.
	 */
	public double computeBeta(MoveStats theMoveStats, MoveStats theAmafMoveStats, int nodeVisits);

	public String getBetaComputerParameters();

	public String printBetaComputer();

}
