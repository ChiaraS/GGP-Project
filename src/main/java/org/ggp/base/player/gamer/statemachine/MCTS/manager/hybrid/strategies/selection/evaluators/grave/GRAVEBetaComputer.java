package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;

public class GRAVEBetaComputer extends BetaComputer {

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

		double amafVisits = theAmafMoveStats.getVisits();
		double moveVisits = theMoveStats.getVisits();

		return (amafVisits / (amafVisits + moveVisits + (this.bias * amafVisits * moveVisits)));
	}

	@Override
	public String getBetaComputerParameters() {
		return "BIAS = " + this.bias;
	}

	@Override
	public void setNewValue(double[] newValue) {

		this.bias = newValue[0];

	}

}
