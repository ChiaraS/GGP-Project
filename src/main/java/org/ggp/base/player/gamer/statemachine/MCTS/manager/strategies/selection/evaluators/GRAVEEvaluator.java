package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.evaluators;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.PnMCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class GRAVEEvaluator implements MoveEvaluator {

	/**
	 * Reference to the AMAF statistics of the ancestor of this node that has enough visits
	 * to make the statistics reliable. This reference will be updated every time the current
	 * node being checked has enough visits to use its own AMAF statistics.
	 */
	private Map<InternalPropnetMove, MoveStats> amafStats;

	private double bias;

	public GRAVEEvaluator(double bias) {
		this.amafStats = null;
		this.bias = bias;
	}

	public void setAmafStats(Map<InternalPropnetMove, MoveStats> amafStats){
		this.amafStats = amafStats;
	}

	@Override
	public double computeMoveValue(PnMCTSNode theNode,
			InternalPropnetMove theMove, MoveStats theMoveStats) {

		double moveVisits = theMoveStats.getVisits();
		double moveScore = theMoveStats.getScoreSum();
		double amafVisits = 0.0;
		double amafScore = 0.0;

		if(this.amafStats != null){
			MoveStats moveAmaf = this.amafStats.get(theMove);
			if(moveAmaf != null){
				amafVisits = moveAmaf.getVisits();
				amafScore = moveAmaf.getScoreSum();
			}
		}

		if(moveVisits == 0){
			if(amafVisits == 0){
				// If the move hasn't been explored ever (i.e. not in this node nor in any descendant of the node
				// whose AMAF table we are using), return the maximum value to encourage exploration of this move.
				return Double.MAX_VALUE;
			}else{
				// If average cannot be computed, return only AMAF average.
				return ((amafScore / amafVisits) / 100.0);
			}
		}else{
			if(amafVisits == 0){
				// If the AMAF average cannot be computed, return only the move average.
				return ((moveScore / moveVisits) / 100.0);
			}else{
				double beta = amafVisits / (amafVisits + moveVisits + (this.bias * amafVisits * moveVisits));
				return ((1.0 - beta) * ((moveScore / moveVisits) / 100.0)) + (beta * ((amafScore / amafVisits) / 100.0));
			}
		}
	}

	@Override
	public String getEvaluatorParameters() {
		return "BIAS = " + this.bias;
	}

	@Override
	public String printEvaluator() {
		String params = this.getEvaluatorParameters();

		if(params != null){
			return "(EVALUATOR_TYPE = " + this.getClass().getSimpleName() + ", " + params + ")";
		}else{
			return "(EVALUATOR_TYPE = " + this.getClass().getSimpleName() + ")";
		}
	}

}
