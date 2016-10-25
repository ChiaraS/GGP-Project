package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.PnUCTEvaluator;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

/**
 * Experiment: compute the grave value as a weighted combination of UCT value and AMAF average
 * (i.e. ((1-beta) * UCT_move + beta * AMAF_move) instead of
 * (((1-beta) * AVG_move) + (beta * AMAF_move) + c * sqrt(log(nodeVisits)/moveVisits))).
 * Doesn't really have good results, DON'T USE.
 *
 * @author C.Sironi
 *
 */
public class PnInvGRAVEEvaluator extends PnUCTEvaluator {

	/**
	 * Reference to the AMAF statistics of the ancestor of this node that has enough visits
	 * to make the statistics reliable. This reference will be updated every time the current
	 * node being checked has enough visits to use its own AMAF statistics.
	 */
	private Map<InternalPropnetMove, MoveStats> amafStats;

	private PnBetaComputer betaComputer;

	public PnInvGRAVEEvaluator(double c, double defaultValue, PnBetaComputer betaComputer) {
		super(c, defaultValue);
		this.betaComputer = betaComputer;
		this.amafStats = null;
		//this.bias = bias;
	}

	public void setAmafStats(Map<InternalPropnetMove, MoveStats> amafStats){
		this.amafStats = amafStats;
	}

	public Map<InternalPropnetMove, MoveStats> getAmafStats(){
		return this.amafStats;
	}

	@Override
	public double computeMoveValue(int allMoveVisits,
			InternalPropnetMove theMove, MoveStats theMoveStats) {

		if(this.amafStats == null){

			//System.out.println("null amaf");

			//System.out.println("returning " + super.computeMoveValue(theNode, theMove, theMoveStats));

			return super.computeMoveValue(allMoveVisits, theMove, theMoveStats);
		}

		MoveStats moveAmafStats = this.amafStats.get(theMove);

		if(moveAmafStats == null || moveAmafStats.getVisits() == 0){

			//System.out.println("no stats for move");

			//System.out.println("returning " + super.computeMoveValue(theNode, theMove, theMoveStats));

			return super.computeMoveValue(allMoveVisits, theMove, theMoveStats);
		}

		double amafScore = moveAmafStats.getScoreSum();
		double amafVisits = moveAmafStats.getVisits();

		double uct = super.computeMoveValue(allMoveVisits, theMove, theMoveStats);

		double amafAvg = (amafScore / amafVisits) / 100.0;

		double beta = this.betaComputer.computeBeta(theMoveStats, moveAmafStats, allMoveVisits);

		//System.out.println("uct = " + uct);
		//System.out.println("amafAvg = " + amafAvg);
		//System.out.println("beta = " + beta);

		//System.out.println("returning = " + (((1.0 - beta) * uct) + (beta * amafAvg)));

		return (((1.0 - beta) * uct) + (beta * amafAvg));

	}

	@Override
	public String getEvaluatorParameters() {
		String params = super.getEvaluatorParameters();

		if(params != null){
			return params + ", " + this.betaComputer.printBetaComputer();
		}else{
			return this.betaComputer.printBetaComputer();
		}
	}

}
