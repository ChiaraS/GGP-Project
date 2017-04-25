package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.util.statemachine.structure.Move;

public class ProgressiveHistoryGraveEvaluator extends GraveEvaluator {

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

	public ProgressiveHistoryGraveEvaluator(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.currentRootAmafStats = null; // Before ever starting any selection we have no reference to any statistic

		this.w = gamerSettings.getDoublePropertyValue("MoveEvaluator.w");

	}

	@Override
	public void clearComponent(){
		super.clearComponent();
		this.currentRootAmafStats = null;
	}

	@Override
	public void setUpComponent(){
		super.setUpComponent();
		this.currentRootAmafStats = null;
	}

	@Override
	public double computeMoveValue(MctsNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats, int parentVisits) {

		// This should never happen because we should set a new reference before performing the search at every game step.
		if(this.currentRootAmafStats == null){
			throw new RuntimeException("ProgressiveHistoryGraveEvaluator has no reference to the AMAF statistics of the current root.");
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

		return (super.computeMoveValue(theNode, theMove, roleIndex, theMoveStats, parentVisits) + progressiveHistory);

	}

	public void setCurrentRootAmafStats(Map<Move, MoveStats> currentRootAmafStats){
		this.currentRootAmafStats = currentRootAmafStats;
	}

	public Map<Move, MoveStats> getCurrentRootAmafStats(){
		return this.currentRootAmafStats;
	}

	@Override
	public String getComponentParameters(String indentation) {
		String params = super.getComponentParameters(indentation);

		if(params != null){
			return params + indentation + "W = " + this.w + indentation + "current_root_amaf_stats = " + (this.currentRootAmafStats == null ? "null" : this.currentRootAmafStats.size() + " entries");
		}else{
			return indentation + "W = " + this.w + indentation + "current_root_amaf_stats = " + (this.currentRootAmafStats == null ? "null" : this.currentRootAmafStats.size() + " entries");
		}
	}

}
