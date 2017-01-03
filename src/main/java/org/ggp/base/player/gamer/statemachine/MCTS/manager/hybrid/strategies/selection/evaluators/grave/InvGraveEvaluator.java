package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave;

import java.lang.reflect.InvocationTargetException;
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

/**
 * Experiment: compute the grave value as a weighted combination of UCT value and AMAF average
 * (i.e. ((1-beta) * UCT_move + beta * AMAF_move) instead of
 * (((1-beta) * AVG_move) + (beta * AMAF_move) + c * sqrt(log(nodeVisits)/moveVisits))).
 * Doesn't really have good results, DON'T USE.
 *
 * @author C.Sironi
 *
 */
public class InvGraveEvaluator extends UctEvaluator{

	/**
	 * Reference to the AMAF statistics of the ancestor of this node that has enough visits
	 * to make the statistics reliable. This reference will be updated every time the current
	 * node being checked has enough visits to use its own AMAF statistics.
	 */
	private Map<Move, MoveStats> amafStats;

	private BetaComputer betaComputer;

	public InvGraveEvaluator(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.amafStats = null;

		try {
			this.betaComputer = (BetaComputer) SearchManagerComponent.getConstructorForSearchManagerComponent(ProjectSearcher.BETA_COMPUTERS.getConcreteClasses(),
					gamerSettings.getPropertyValue("MoveEvaluator.betaComputerType")).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating BetaComputer " + gamerSettings.getPropertyValue("MoveEvaluator.betaComputerType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);
		this.betaComputer.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent(){
		super.clearComponent();
		this.amafStats = null;
		this.betaComputer.clearComponent();
	}

	@Override
	public void setUpComponent(){
		super.setUpComponent();
		this.amafStats = null;
		this.betaComputer.setUpComponent();
	}

	public void setAmafStats(Map<Move, MoveStats> amafStats){
		this.amafStats = amafStats;
	}

	public Map<Move, MoveStats> getAmafStats(){
		return this.amafStats;
	}

	@Override
	public double computeMoveValue(MctsNode theNode, Move theMove, int roleIndex, MoveStats theMoveStats) {

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

		double beta = this.betaComputer.computeBeta(theMoveStats, moveAmafStats, theNode.getTotVisits(), roleIndex);

		//System.out.println("uct = " + uct);
		//System.out.println("amafAvg = " + amafAvg);
		//System.out.println("beta = " + beta);

		//System.out.println("returning = " + (((1.0 - beta) * uct) + (beta * amafAvg)));

		return (((1.0 - beta) * uct) + (beta * amafAvg));

	}

	@Override
	public String getComponentParameters(String indentation) {
		String params = super.getComponentParameters(indentation);

		if(params != null){
			return params + indentation + "BETA_COMPUTER = " + this.betaComputer.printComponent(indentation + "  ");
		}else{
			return indentation + "BETA_COMPUTER = " + this.betaComputer.printComponent(indentation + "  ");
		}
	}

}
