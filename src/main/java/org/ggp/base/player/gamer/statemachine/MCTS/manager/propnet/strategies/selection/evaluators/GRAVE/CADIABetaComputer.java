package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;

public class CADIABetaComputer implements BetaComputer {

	/**
	 * Equivalence parameter: number of node visits needed to consider as equal
	 * the UCT value and the AMAF value of a move.
	 */
	private int k;

	public CADIABetaComputer(int k) {
		this.k = k;
	}

	@Override
	public double computeBeta(MoveStats theMoveStats,
			MoveStats theAmafMoveStats, int nodeVisits) {
		return Math.sqrt(((double)this.k) / ((double)((3*nodeVisits) + this.k)));
	}

	@Override
	public String getBetaComputerParameters() {
		return "EQUIVALENCE_PARAMETER = " + this.k;
	}

	@Override
	public String printBetaComputer() {
		String params = this.getBetaComputerParameters();

		if(params != null){
			return "(BETA_COMPUTER_TYPE = " + this.getClass().getSimpleName() + ", " + params + ")";
		}else{
			return "(BETA_COMPUTER_TYPE = " + this.getClass().getSimpleName() + ")";
		}
	}

}
