package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.UCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.structure.Move;

/**
 * Experiment: compute the grave value as a weighted combination of UCT value and AMAF average
 * (i.e. ((1-beta) * UCT_move + beta * AMAF_move) instead of
 * (((1-beta) * AVG_move) + (beta * AMAF_move) + c * sqrt(log(nodeVisits)/moveVisits))).
 * Doesn't really have good results, DON'T USE.
 *
 * @author C.Sironi
 *
 */
public class InvGRAVEEvaluator extends UCTEvaluator{

	/**
	 * Reference to the AMAF statistics of the ancestor of this node that has enough visits
	 * to make the statistics reliable. This reference will be updated every time the current
	 * node being checked has enough visits to use its own AMAF statistics.
	 */
	private Map<Move, MoveStats> amafStats;

	private BetaComputer betaComputer;

	public InvGRAVEEvaluator(double c, double defaultValue, BetaComputer betaComputer, int numRoles) {
		super(c, defaultValue, numRoles);
		this.betaComputer = betaComputer;
		this.amafStats = null;
		//this.bias = bias;
	}

	public void setAmafStats(Map<Move, MoveStats> amafStats){
		this.amafStats = amafStats;
	}

	public Map<Move, MoveStats> getAmafStats(){
		return this.amafStats;
	}

	@Override
	public double computeMoveValue(MCTSNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats) {

		if(this.amafStats == null){

			//System.out.println("null amaf");

			//System.out.println("returning " + super.computeMoveValue(theNode, theMove, theMoveStats));

			return super.computeMoveValue(theNode, theMove, roleIndex, theMoveStats);
		}

		MoveStats moveAmafStats = this.amafStats.get(theMove);

		if(moveAmafStats == null || moveAmafStats.getVisits() == 0){

			//System.out.println("no stats for move");

			//System.out.println("returning " + super.computeMoveValue(theNode, theMove, theMoveStats));

			return super.computeMoveValue(theNode, theMove, roleIndex, theMoveStats);
		}

		double amafScore = moveAmafStats.getScoreSum();
		double amafVisits = moveAmafStats.getVisits();

		double uct = super.computeMoveValue(theNode, theMove, roleIndex, theMoveStats);

		double amafAvg = (amafScore / amafVisits) / 100.0;

		double beta = this.betaComputer.computeBeta(theMoveStats, moveAmafStats, theNode.getTotVisits());

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
