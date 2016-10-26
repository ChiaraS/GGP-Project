package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.GRAVE;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnBetaComputer;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public class ProverProgressiveHistoryGRAVEEvaluator extends ProverGRAVEEvaluator {

	/**
	 * Reference to the AMAF statistics of the current root of the tree.
	 * These are the statistics of the node that corresponds to the current
	 * game state in the real game.
	 */
	private Map<ExplicitMove, MoveStats> currentRootAmafStats;

	/**
	 * Parameter that determines the influence of progressive history.
	 */
	private double w;

	public ProverProgressiveHistoryGRAVEEvaluator(double c, double defaultValue,
			PnBetaComputer betaComputer, double defaultExploration, double w) {
		super(c, defaultValue, betaComputer, defaultExploration);

		this.currentRootAmafStats = null; // Before ever starting any selection we have no reference to any statistic

		this.w = w;

	}

	@Override
	public double computeMoveValue(int nodeVisits,
			ExplicitMove theMove, MoveStats theMoveStats) {

		// This should never happen because we should set a new reference before performing the search at every game step.
		if(this.currentRootAmafStats == null){
			throw new RuntimeException("ProgressiveHistoryGRAVEEvaluator has no reference to the AMAF statistics of the current root.");
		}

		// First compute the progressive history bonus for the move:

		double progressiveHistory = 0.0;

		// See if we have any statistic for the move in the current root AMAF table
		MoveStats theRootAmaf = this.currentRootAmafStats.get(theMove);

		if(theRootAmaf != null && theRootAmaf.getVisits() > 0){

			double rootAmafScore = theRootAmaf.getScoreSum();
			double rootAmafVisits = theRootAmaf.getVisits();

			double rootAmafAvg = (rootAmafScore / rootAmafVisits) / 100.0;

			double moveScore = theMoveStats.getScoreSum();
			double moveVisits = theMoveStats.getVisits();

			double moveScoreAvg = 0.0;

			if(moveVisits > 0){
				moveScoreAvg = (moveScore / moveVisits) / 100.0;
			}

			progressiveHistory = rootAmafAvg * (this.w / ( (1.0 - moveScoreAvg) * moveVisits + 1.0));
		}

		return (super.computeMoveValue(nodeVisits, theMove, theMoveStats) + progressiveHistory);

	}

	public void setCurrentRootAmafStats(Map<ExplicitMove, MoveStats> currentRootAmafStats){
		this.currentRootAmafStats = currentRootAmafStats;
	}

	public Map<ExplicitMove, MoveStats> getCurrentRootAmafStats(){
		return this.currentRootAmafStats;
	}

	@Override
	public String getEvaluatorParameters() {
		String params = super.getEvaluatorParameters();

		if(params != null){
			return params + ", W = " + this.w;
		}else{
			return "W = " + this.w;
		}
	}
}
