package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.UctEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.Move;

public class GraveEvaluator extends UctEvaluator{

	/**
	 * Reference to the AMAF statistics of the ancestor of this node that has enough visits
	 * to make the statistics reliable. This reference will be updated every time the current
	 * node being checked has enough visits to use its own AMAF statistics.
	 *
	 * Note: each role has its own reference to the closest ancestor with enough number of visits
	 * because each role might have a different value for the threshold on the number of visits
	 * (i.e. the parameter "ref").
	 */
	private List<Map<Move, MoveStats>> closestAmafStats;

	private BetaComputer betaComputer;

	/**
	 * Default value of the exploration part of the UCB formula.
	 * Used when the move has no visits but we have the AMAF value for it.
	 * This evaluator has the following behavior when computing the value of a move:
	 * - if moveVisits == 0 && AMAF(move) == null: fpu
	 * - if moveVisits == 0 && AMAF(move) != null: AMAF(move) + defaultExploration
	 * - if moveVisits != 0 && AMAF(move) == null: UCT(move) + UCT_EXPLORATION(move)
	 * - if moveVisits != 0 && AMAF(move) != null: (1-beta)*UCT_EXPLOITATION(move) + beta*AMAF(move) + UCT_EXPLORATION(move)
	 */
	private double defaultExploration;

	public GraveEvaluator(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.closestAmafStats = null;

		try {
			this.betaComputer = (BetaComputer) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.BETA_COMPUTERS.getConcreteClasses(),
					gamerSettings.getPropertyValue("MoveEvaluator.betaComputerType"))).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating BetaComputer " + gamerSettings.getPropertyValue("MoveEvaluator.betaComputerType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		this.defaultExploration = gamerSettings.getDoublePropertyValue("MoveEvaluator.defaultExplorationValue");
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);
		this.betaComputer.setReferences(sharedReferencesCollector);
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
	protected double computeExploitation(MctsNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats){

		int nodeVisits = theNode.getTotVisits()[roleIndex];

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
	protected double computeExploration(MctsNode theNode, int roleIndex, MoveStats theMoveStats, int parentVisits){

		double exploration = super.computeExploration(theNode, roleIndex, theMoveStats, parentVisits);

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
	public String getComponentParameters(String indentation) {
		String params = super.getComponentParameters(indentation);

		String closestAmafStatsString;

		if(this.closestAmafStats != null){
			closestAmafStatsString = "[ ";

			for(Map<Move,MoveStats> stats : this.closestAmafStats){
				closestAmafStatsString += (stats == null ? "null " : stats.size() + "entries ");
			}

			closestAmafStatsString += "]";

		}else{
			closestAmafStatsString = "null";
		}

		if(params != null){
			return params + indentation + "BETA_COMPUTER = " + this.betaComputer.printComponent(indentation + "  ") + indentation + "DEFAULT_EXPLORATION = " + this.defaultExploration + indentation + "closest_amaf_stats = " + closestAmafStatsString;
		}else{
			return indentation + "BETA_COMPUTER = " + this.betaComputer.printComponent(indentation + "  ") + indentation + "DEFAULT_EXPLORATION = " + this.defaultExploration + indentation + "closest_amaf_stats = " + closestAmafStatsString;
		}
	}

}
