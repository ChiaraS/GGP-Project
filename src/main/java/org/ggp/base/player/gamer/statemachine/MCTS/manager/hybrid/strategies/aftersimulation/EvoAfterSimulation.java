package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;

public class EvoAfterSimulation extends AfterSimulationStrategy {

	private SingleParameterEvolutionManager evolutionManager;

	public EvoAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

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
	public void afterSimulationActions(SimulationResult[] simulationResult) {

		// NOTE: for now the EvoAfterSimulation strategy only deals with single playouts.
		// TODO: adapt this class to deal with iterations for which multiple playouts are performed and thus we have
		// more than one simulationResult.

		if(simulationResult.length != 1){
			GamerLogger.logError("AfterSimulationStrategy", "TunerAfterSimulation - Detected multiple playouts results. TD backpropagation not able to deal with multiple playout results. Probably a wrong combination of strategies has been set or there is something wrong in the code!");
			throw new RuntimeException("Detected multiple playouts results.");
		}

		double[] goals;

		// We have to check if the EvolutionManager is evolving parameters only for the playing role
		// or for all roles and update the fitness with appropriate goals.
		if(this.evolutionManager.getNumPopulations() == 1){

			goals = new double[1];
			goals[0] = simulationResult[0].getTerminalGoalsIn0_100()[this.gameDependentParameters.getMyRoleIndex()];

		}else{

			goals = simulationResult[0].getTerminalGoalsIn0_100();

		}

		this.evolutionManager.updateFitness(goals);

	}

	@Override
	public String getComponentParameters(String indentation) {

		// Only the component that creates the manager prints its content
		//return indentation + "EVOLUTION_MANAGER = " + this.saesManager.printEvolutionManager(indentation + "  ");

		// Here we only print the name
		return indentation + "EVOLUTION_MANAGER = " + this.evolutionManager.getClass().getSimpleName();

	}

}
