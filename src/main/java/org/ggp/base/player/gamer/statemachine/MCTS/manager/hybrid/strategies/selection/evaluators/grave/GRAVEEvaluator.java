package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.UCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.structure.Move;

public class GRAVEEvaluator extends UCTEvaluator{

	/**
	 * Reference to the AMAF statistics of the ancestor of this node that has enough visits
	 * to make the statistics reliable. This reference will be updated every time the current
	 * node being checked has enough visits to use its own AMAF statistics.
	 */
	private Map<Move, MoveStats> closerAmafStats;

	private BetaComputer betaComputer;

	private double defaultExploration;

	public GRAVEEvaluator(double c, double defaultValue, BetaComputer betaComputer, double defaultExploration) {
		super(c, defaultValue);
		this.betaComputer = betaComputer;
		this.closerAmafStats = null;
		this.defaultExploration = defaultExploration;
	}

	@Override
	protected double computeExploitation(MCTSNode theNode, Move theMove, MoveStats theMoveStats){

		int nodeVisits = theNode.getTotVisits();

		double uctExploitation = super.computeExploitation(theNode, theMove, theMoveStats);

		double amafExploitation = -1.0;

		MoveStats moveAmafStats = null;

		if(this.closerAmafStats != null){

			moveAmafStats = this.closerAmafStats.get(theMove);

			if(moveAmafStats != null && moveAmafStats.getVisits() != 0){
				double amafVisits = moveAmafStats.getVisits();
				double amafScore = moveAmafStats.getScoreSum();
				amafExploitation = (amafScore / amafVisits) / 100.0;
			}

		}

		if(uctExploitation == -1){
			return amafExploitation;
		}

		if(amafExploitation == -1){
			return uctExploitation;
		}

		double beta = this.betaComputer.computeBeta(theMoveStats, moveAmafStats, nodeVisits);

		//System.out.println("uct = " + uctExploitation);
		//System.out.println("amaf = " + amafExploitation);
		//System.out.println("beta = " + beta);

		//System.out.println("returning = " + (((1.0 - beta) * uct) + (beta * amafAvg)));

		if(beta == -1){
			return -1.0;
		}else{
			//System.out.println("returning exploitation = " + (((1.0 - beta) * uctExploitation) + (beta * amafExploitation)));
			return (((1.0 - beta) * uctExploitation) + (beta * amafExploitation));
		}

	}

	@Override
	protected double computeExploration(MCTSNode theNode, MoveStats theMoveStats){

		double exploration = super.computeExploration(theNode, theMoveStats);

		if(exploration != -1){
			//System.out.println("returning exploration = " + exploration);
			return exploration;
		}else{
			//System.out.println("returning default exploration = " + this.defaultExploration);
			return this.defaultExploration;
		}

	}

	public void setCloserAmafStats(Map<Move, MoveStats> closerAmafStats){
		this.closerAmafStats = closerAmafStats;
	}

	public Map<Move, MoveStats> getCloserAmafStats(){
		return this.closerAmafStats;
	}

	@Override
	public String getEvaluatorParameters() {
		String params = super.getEvaluatorParameters();

		if(params != null){
			return params + ", " + this.betaComputer.printBetaComputer() + ", DEFAULT_EXPLORATION = " + this.defaultExploration;
		}else{
			return this.betaComputer.printBetaComputer() + ", DEFAULT_EXPLORATION = " + this.defaultExploration;
		}
	}

}
