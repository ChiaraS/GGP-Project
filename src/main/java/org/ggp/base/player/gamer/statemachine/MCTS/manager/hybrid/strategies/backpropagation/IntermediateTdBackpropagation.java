package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SequDecMctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.tddecoupled.TdDecoupledMctsNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.MachineState;

public class IntermediateTdBackpropagation extends TdBackpropagation {

	/**
	 * Index of the episode being currently considered in the computation of the backpropagation values.
	 * Corresponds to (simulationLength - episode depth in the game tree).
	 */
	protected int goalsIndex;

	public IntermediateTdBackpropagation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.goalsIndex = 0;

	}

	@Override
	public void clearComponent() {
		this.goalsIndex = 0;
	}

	@Override
	protected void decUpdate(TdDecoupledMctsNode currentNode, MachineState currentState, SequDecMctsJointMove jointMove, SimulationResult simulationResult){

		simulationResult.addGoals(this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(currentState));

		super.decUpdate(currentNode, currentState, jointMove, simulationResult);

	}


	@Override
	public int[] getReturnValuesForRolesInPlayout(SimulationResult simulationResult) {

		List<int[]> allIntermediateGoals = simulationResult.getIntermediateGoals();

		if(allIntermediateGoals == null || allIntermediateGoals.size() <= this.goalsIndex + 1){ // Intermediate goals are needed and the ones at indices (this.goalsIndex) and (this.goalsIndex+1) must be available, this the length of the list of intermediate goals must be at least this.goalsIndex+2
			GamerLogger.logError("MctsManager", "Necessary intermediate goals not found in the simulation result when processing the result for intermediate TD backpropagation. Probably a wrong combination of strategies has been set!");
			throw new RuntimeException("Necessary intermediate goals not found in the simulation result.");
		}

		int[] intermediateReturnValues = new int[this.gameDependentParameters.getNumRoles()];
		int[] nextStateGoals = allIntermediateGoals.get(this.goalsIndex);
		int[] currentStateGoals = allIntermediateGoals.get(this.goalsIndex+1);

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){

			intermediateReturnValues[i] = nextStateGoals[i] - currentStateGoals[i];

		}

		this.goalsIndex++;

		return intermediateReturnValues;

	}

	@Override
	public void resetSimulationParameters(){

		super.resetSimulationParameters();

		this.goalsIndex = 0;

	}

}
