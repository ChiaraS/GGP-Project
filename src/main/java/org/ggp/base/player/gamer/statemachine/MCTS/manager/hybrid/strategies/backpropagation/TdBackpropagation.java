package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.td.GlobalExtremeValues;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SequDecMctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.tddecoupled.TdDecoupledMctsNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.MachineState;

public abstract class TdBackpropagation extends BackpropagationStrategy {

	/**
	 * Strategy parameters.
	 */

	protected double qPlayout;

	protected double lambda;

	protected double gamma;

	protected GlobalExtremeValues globalExtremeValues;

	/**
	 * Simulation parameters that must be reset after each simulation is concluded
	 * and before starting the backpropagation for the next simulation.
	 */

	protected double[] deltaSum;

	protected double[] qNext;


	public TdBackpropagation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.qPlayout = Double.parseDouble(gamerSettings.getPropertyValue("BackpropagationStrategy.qPlayout"));
		this.lambda = Double.parseDouble(gamerSettings.getPropertyValue("BackpropagationStrategy.lambda"));
		this.gamma = Double.parseDouble(gamerSettings.getPropertyValue("BackpropagationStrategy.gamma"));

		double defaultGlobalMinValue = Double.parseDouble(gamerSettings.getPropertyValue("MoveEvaluator.defaultGlobalMinValue"));
		double defaultGlobalMaxValue = Double.parseDouble(gamerSettings.getPropertyValue("MoveEvaluator.defaultGlobalMaxValue"));
		this.globalExtremeValues = new GlobalExtremeValues(defaultGlobalMinValue, defaultGlobalMaxValue);
		this.globalExtremeValues.setGlobalMinValues(null);
		this.globalExtremeValues.setGlobalMaxValues(null);
		sharedReferencesCollector.setGlobalExtremeValues(globalExtremeValues);

		this.deltaSum = null;
		this.qNext = null;

		sharedReferencesCollector.setTdBackpropagation(this);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent(){

		this.globalExtremeValues.setGlobalMinValues(null);
		this.globalExtremeValues.setGlobalMaxValues(null);

		this.deltaSum = null;
		this.qNext = null;

	}

	@Override
	public void setUpComponent(){

		this.globalExtremeValues.setGlobalMinValues(new double[this.gameDependentParameters.getNumRoles()]);
		this.globalExtremeValues.setGlobalMaxValues(new double[this.gameDependentParameters.getNumRoles()]);

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			this.globalExtremeValues.getGlobalMinValues()[i] = Double.MAX_VALUE;
			this.globalExtremeValues.getGlobalMaxValues()[i] = -Double.MAX_VALUE;
		}

		this.deltaSum = new double[this.gameDependentParameters.getNumRoles()];
		this.qNext = new double[this.gameDependentParameters.getNumRoles()];

		this.resetSimulationParameters();

	}

	@Override
	public void update(MctsNode currentNode, MachineState currentState,
			MctsJointMove jointMove, SimulationResult simulationResult) {

		if(currentNode instanceof TdDecoupledMctsNode && jointMove instanceof SequDecMctsJointMove){
			this.decUpdate((TdDecoupledMctsNode)currentNode, currentState, (SequDecMctsJointMove)jointMove, simulationResult);
		}else{
			throw new RuntimeException("TdBackpropagation-update(): no method implemented to manage backpropagation for node type (" + currentNode.getClass().getSimpleName() + ") and joint move type (" + jointMove.getClass().getSimpleName() + ").");
		}

	}

	protected void decUpdate(TdDecoupledMctsNode currentNode, MachineState currentState, SequDecMctsJointMove jointMove, SimulationResult simulationResult){

		currentNode.incrementTotVisits();

		DecoupledMctsMoveStats[][] moves = currentNode.getMoves();
		int[] movesIndices = jointMove.getMovesIndices();

		DecoupledMctsMoveStats currentMoveStat;

		int[] returnValuesForRoles = this.getReturnValuesForRolesInPlayout(simulationResult); // Here the index is useless so we set it to 0

		/*
		System.out.print("R = [ ");
		for(int i = 0; i < returnValuesForRoles.length; i++){
			System.out.print(returnValuesForRoles[i] + " ");
		}
		System.out.println("]");
		*/

		double qCurrent;
		double delta;
		double alpha;

		double newScore;

		//System.out.print("S = [ ");
		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){

			currentMoveStat = moves[i][movesIndices[i]];

			if(currentMoveStat.getVisits() == 0){
				qCurrent = 0.0;
			}else{
				qCurrent = currentMoveStat.getScoreSum()/((double)currentMoveStat.getVisits());
			}

			delta = ((double)returnValuesForRoles[i]) + this.gamma * this.qNext[i] - qCurrent;
			this.deltaSum[i] = this.lambda * this.gamma * this.deltaSum[i] + delta;

			currentMoveStat.incrementVisits();
			alpha = 1.0/((double)currentMoveStat.getVisits());

			newScore = (qCurrent + alpha * this.deltaSum[i]);

			/*
			if(newScore < 0.0){
				GamerLogger.logError("MctsManager", "Computed negative score when backpropagating: " + newScore);

				//newScore = 0.0;
			}
			*/

			//System.out.print(newScore + " ");

			currentMoveStat.setScoreSum(newScore*((double)currentMoveStat.getVisits())); // Note that the statistics memorize the total sum of move values, thus we must multiply the new expected value by the number of visits of the move.

			if(newScore > currentNode.getMaxStateActionValueForRole(i)){
				currentNode.setMaxStateActionValueForRole(newScore, i);

				// Note: this check is here because if the new score is lower than the maximum value of
				// the current node then it's also lower than the maximum overall value and no update
				// would be needed.
				if(newScore > this.globalExtremeValues.getGlobalMaxValues()[i]){
					this.globalExtremeValues.getGlobalMaxValues()[i] = newScore;
				}
			}

			if(newScore < currentNode.getMinStateActionValueForRole(i)){
				currentNode.setMinStateActionValueForRole(newScore, i);

				// Note: this check is here because if the new score is higher than the mminimum value of
				// the current node then it's also higher than the minimum overall value and no update
				// would be needed.
				if(newScore < this.globalExtremeValues.getGlobalMinValues()[i]){
					this.globalExtremeValues.getGlobalMinValues()[i] = newScore;
				}
			}

			this.qNext[i] = qCurrent;
		}

		//System.out.println("]");

	}

	@Override
	public void processPlayoutResult(MctsNode leafNode,	MachineState leafState,
			SimulationResult simulationResult) {

		int playoutLength = simulationResult.getPlayoutLength();

		if(playoutLength <= 0){ // This method should be called only if the playout was actually performed, thus the length must be at least 1!
			GamerLogger.logError("MctsManager", "Playout length equals 0 when processing the playout result for TD backpropagation. Probably a wrong combination of strategies has been set or there is something wrong in the code!");
			throw new RuntimeException("Playout length equals 0.");
		}

		int[] returnValuesForRoles;
		double delta;
		//double qCurrent = this.qPlayout; Redundant! Basically qCurrent = qPlayout for the whole backpropagation in the playout part of the simulation

		// Update deltaSum for each non-terminal episode in the playout.
		for(int i = 0; i < playoutLength-1; i++){

			returnValuesForRoles = this.getReturnValuesForRolesInPlayout(simulationResult);

			for(int j = 0; j < this.gameDependentParameters.getNumRoles(); j++){

				delta = ((double)returnValuesForRoles[j]) + this.gamma * this.qNext[j] - this.qPlayout;

				this.deltaSum[j] = this.lambda * this.gamma * this.deltaSum[j] + delta;

				this.qNext[j] = this.qPlayout; // Would be enough to do this only for the 1st iteration of the cycle

			}
		}
	}

	@Override
	public String getComponentParameters() {
		return "Q_PLAYOUT = " + this.qPlayout + ", LAMBDA = " + this.lambda + ", GAMMA = " + this.gamma + ", DEFAUL_GLOBAL_MIN_VALUE = " + this.globalExtremeValues.getDefaultGlobalMinValue() + ", DEFAUL_GLOBAL_MAX_VALUE = " + this.globalExtremeValues.getDefaultGlobalMaxValue();
	}

	public void resetSimulationParameters(){

		for(int i = 0; i < this.deltaSum.length; i++){
			this.deltaSum[i] = 0.0;
			this.qNext[i] = 0.0;
		}
	}

	public abstract int[] getReturnValuesForRolesInPlayout(SimulationResult simulationResult);

}
