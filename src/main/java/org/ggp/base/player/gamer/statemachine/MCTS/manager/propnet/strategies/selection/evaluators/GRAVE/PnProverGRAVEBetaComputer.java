package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;

public class PnProverGRAVEBetaComputer extends PnProverBetaComputer {

	private double bias;

	public PnProverGRAVEBetaComputer(double bias) {

		this.bias = bias;

	}

	@Override
	public double computeBeta(MoveStats theMoveStats, MoveStats theAmafMoveStats,
			int nodeVisits) {

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
	public void setNewValues(double[] newValue) {

		this.bias = newValue[0];

	}

	@Override
	public void setNewValuesFromIndices(int[] newValuesIndices) {
		// TODO Auto-generated method stub

	}

	@Override
	public int[] getPossibleValuesLengths() {
		// TODO Auto-generated method stub
		return null;
	}
}
