package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.evaluators.GRAVE;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;

public class GRAVEBetaComputer implements BetaComputer {

	private double bias;

	public GRAVEBetaComputer(double bias) {
		this.bias = bias;
	}

	@Override
	public double computeBeta(MoveStats theMoveStats,
			MoveStats theAmafMoveStats, int nodeVisits) {

		if(theAmafMoveStats == null){
			return -1.0;
		}

		return (theAmafMoveStats.getVisits() / (theAmafMoveStats.getVisits() + theMoveStats.getVisits() + (this.bias * theAmafMoveStats.getVisits() * theMoveStats.getVisits())));
	}

	@Override
	public String getBetaComputerParameters() {
		return "BIAS = " + this.bias;
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
