package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;

public class PnCADIABetaComputer extends PnBetaComputer{

	/**
	 * Equivalence parameter: number of node visits needed to consider as equal
	 * the UCT value and the AMAF value of a move.
	 */
	private int k;

	public PnCADIABetaComputer(int k) {
		this.k = k;
	}

	@Override
	public double computeBeta(MoveStats theMoveStats,
			MoveStats theAmafMoveStats, int nodeVisits) {

		double numerator = k;
		double denominator = ((3*nodeVisits) + this.k);
		return Math.sqrt(numerator / denominator);
	}

	@Override
	public String getBetaComputerParameters() {
		return "EQUIVALENCE_PARAMETER = " + this.k;
	}

	@Override
	public void setNewValue(double newValue){

		// TODO: fix this not-so-nice casting
		this.k = (int) newValue;

		//System.out.println(k);
	}



}
