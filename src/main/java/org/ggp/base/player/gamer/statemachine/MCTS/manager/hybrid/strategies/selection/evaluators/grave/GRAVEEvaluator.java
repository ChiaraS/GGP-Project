package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.UCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.structure.Move;

public class GRAVEEvaluator extends UCTEvaluator{

	/**
	 * Reference to the AMAF statistics of the ancestor of this node that has enough visits
	 * to make the statistics reliable. This reference will be updated every time the current
	 * node being checked has enough visits to use its own AMAF statistics.
	 *
	 * Note: each role has its own reference to the closest ancestor with enough number of visits
	 * because each role might have a different value for the threshold on the number of visits
	 * (i.e. the parameter minAMAFVisits, or as called in the paper, "ref").
	 */
	private List<Map<Move, MoveStats>> closestAmafStats;

	private BetaComputer betaComputer;

	private double defaultExploration;

	public GRAVEEvaluator(GameDependentParameters gameDependentParameters, double c, double defaultValue, BetaComputer betaComputer, double defaultExploration) {
		super(gameDependentParameters, c, defaultValue);
		this.betaComputer = betaComputer;

		this.closestAmafStats = null;

		this.defaultExploration = defaultExploration;
	}

	@Override
	public void clearComponent(){
		super.clearComponent();
		this.closestAmafStats = null;
		this.betaComputer.clearComponent();
	}

	@Override
	public void setUpComponent(){

		super.setUpComponent();

		this.closestAmafStats = new ArrayList<Map<Move, MoveStats>>(this.gameDependentParameters.getNumRoles());
		// Initialize to null the closest AMAF stats for each role
		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			this.closestAmafStats.add(null);
		}

		this.betaComputer.setUpComponent();

	}

	@Override
	protected double computeExploitation(MCTSNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats){

		int nodeVisits = theNode.getTotVisits();

		double uctExploitation = super.computeExploitation(theNode, theMove, roleIndex, theMoveStats);

		double amafExploitation = -1.0;

		MoveStats moveAmafStats = null;

		// Get the closest AMAF stats for the role for which we are selecting the move
		Map<Move, MoveStats> closestAmafStatsForRole = this.closestAmafStats.get(roleIndex);

		if(closestAmafStatsForRole != null){

			moveAmafStats = closestAmafStatsForRole.get(theMove);

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

		double beta = this.betaComputer.computeBeta(theMoveStats, moveAmafStats, nodeVisits, roleIndex);

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
	protected double computeExploration(MCTSNode theNode, int roleIndex, MoveStats theMoveStats){

		double exploration = super.computeExploration(theNode, roleIndex, theMoveStats);

		if(exploration != -1){
			//System.out.println("returning exploration = " + exploration);
			return exploration;
		}else{
			//System.out.println("returning default exploration = " + this.defaultExploration);
			return this.defaultExploration;
		}

	}

	public void setClosestAmafStats(int roleIndex, Map<Move, MoveStats> closestAmafStats){
		this.closestAmafStats.set(roleIndex, closestAmafStats);
	}

	public List<Map<Move, MoveStats>> getClosestAmafStats(){
		return this.closestAmafStats;
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
