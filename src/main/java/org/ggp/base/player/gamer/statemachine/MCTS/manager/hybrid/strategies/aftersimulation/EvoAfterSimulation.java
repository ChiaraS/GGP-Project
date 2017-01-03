package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public class EvoAfterSimulation extends AfterSimulationStrategy {

	private SingleParameterEvolutionManager evolutionManager;

	public EvoAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.evolutionManager = sharedReferencesCollector.getSingleParameterEvolutionManager();
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	@Override
	public void afterSimulationActions(SimulationResult simulationResult) {

		int[] goals;

		// We have to check if the EvolutionManager is evolving parameters only for the playing role
		// or for all roles and update the fitness with appropriate goals.
		if(this.evolutionManager.getNumPopulations() == 1){

			goals = new int[1];
			goals[0] = simulationResult.getTerminalGoals()[this.gameDependentParameters.getMyRoleIndex()];

		}else{

			goals = simulationResult.getTerminalGoals();

		}

		this.evolutionManager.updateFitness(goals);

	}

	@Override
	public String getComponentParameters(String indentation) {

			return indentation + "EVOLUTION_MANAGER = " + this.evolutionManager.printEvolutionManager(indentation + "  ");

	}

}
