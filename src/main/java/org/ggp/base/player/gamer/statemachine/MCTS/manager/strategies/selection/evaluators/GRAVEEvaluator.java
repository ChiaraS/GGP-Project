package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.evaluators;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.PnMCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class GRAVEEvaluator extends UCTEvaluator {

	/**
	 * Reference to the AMAF statistics of the ancestor of this node that has enough visits
	 * to make the statistics reliable. This reference will be updated every time the current
	 * node being checked has enough visits to use its own AMAF statistics.
	 */
	private Map<InternalPropnetMove, MoveStats> amafStats;

	private double bias;

	public GRAVEEvaluator(double c, double bias) {
		super(c);
		this.amafStats = null;
		this.bias = bias;
	}

	public void setAmafStats(Map<InternalPropnetMove, MoveStats> amafStats){
		this.amafStats = amafStats;
	}

	public Map<InternalPropnetMove, MoveStats> getAmafStats(){
		return this.amafStats;
	}

	@Override
	public double computeMoveValue(PnMCTSNode theNode,
			InternalPropnetMove theMove, MoveStats theMoveStats) {

		if(this.amafStats == null){

			System.out.println("null amaf");

			System.out.println("returning " + super.computeMoveValue(theNode, theMove, theMoveStats));

			return super.computeMoveValue(theNode, theMove, theMoveStats);
		}

		MoveStats moveAmafStats = this.amafStats.get(theMove);

		if(moveAmafStats == null || moveAmafStats.getVisits() == 0){

			System.out.println("no stats for move");

			System.out.println("returning " + super.computeMoveValue(theNode, theMove, theMoveStats));

			return super.computeMoveValue(theNode, theMove, theMoveStats);
		}

		double uct = super.computeMoveValue(theNode, theMove, theMoveStats);

		double amafAvg = (moveAmafStats.getScoreSum() / moveAmafStats.getVisits()) / 100.0;

		double beta = this.computeBeta(theMoveStats, moveAmafStats);

		System.out.println("uct = " + uct);
		System.out.println("amafAvg = " + amafAvg);
		System.out.println("beta = " + beta);

		System.out.println("returning = " + (((1.0 - beta) * uct) + (beta * amafAvg)));

		return (((1.0 - beta) * uct) + (beta * amafAvg));

	}

	private double computeBeta(MoveStats theMoveStats, MoveStats moveAmafStats){
		return (moveAmafStats.getVisits() / (moveAmafStats.getVisits() + theMoveStats.getVisits() + (this.bias * moveAmafStats.getVisits() * theMoveStats.getVisits())));
	}

	@Override
	public String getEvaluatorParameters() {
		String params = super.getEvaluatorParameters();

		if(params != null){
			return params + ", BIAS = " + this.bias;
		}else{
			return "BIAS = " + this.bias;
		}
	}

}
