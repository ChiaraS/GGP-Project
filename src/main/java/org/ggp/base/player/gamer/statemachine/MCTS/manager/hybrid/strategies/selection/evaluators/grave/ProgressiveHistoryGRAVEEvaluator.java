package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.structure.Move;

public class ProgressiveHistoryGRAVEEvaluator extends GRAVEEvaluator {

	/**
	 * Reference to the AMAF statistics of the current root of the tree.
	 * These are the statistics of the node that corresponds to the current
	 * game state in the real game.
	 */
	private Map<Move, MoveStats> currentRootAmafStats;

	/**
	 * Parameter that determines the influence of progressive history.
	 */
	private double w;

	public ProgressiveHistoryGRAVEEvaluator(double c, double defaultValue,
			BetaComputer betaComputer, double defaultExploration, double w) {
		super(c, defaultValue, betaComputer, defaultExploration);

		this.currentRootAmafStats = null; // Before ever starting any selection we have no reference to any statistic

		this.w = w;

	}

	@Override
	public double computeMoveValue(MCTSNode theNode, Move theMove, MoveStats theMoveStats) {

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

		return (super.computeMoveValue(theNode, theMove, theMoveStats) + progressiveHistory);

	}

	public void setCurrentRootAmafStats(Map<Move, MoveStats> currentRootAmafStats){
		this.currentRootAmafStats = currentRootAmafStats;
	}

	public Map<Move, MoveStats> getCurrentRootAmafStats(){
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
