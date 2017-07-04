package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;

public class TunerAfterSimulation extends AfterSimulationStrategy {

	private ParametersTuner parametersTuner;

	public TunerAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.parametersTuner = sharedReferencesCollector.getParametersTuner();
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

		// NOTE: for now the TunerAfterSimulation strategy only deals with single playouts.
		// TODO: adapt this class to deal with iterations for which multiple playouts are performed and thus we have
		// more than one simulationResult.

		if(simulationResult.length != 1){
			GamerLogger.logError("AfterSimulationStrategy", "TunerAfterSimulation - Detected multiple playouts results. TD backpropagation not able to deal with multiple playout results. Probably a wrong combination of strategies has been set or there is something wrong in the code!");
			throw new RuntimeException("Detected multiple playouts results.");
		}

		if(this.parametersTuner.isTuning()){
			/*
			int[] goals;

			// We have to check if the ParametersTuner is tuning parameters only for the playing role
			// or for all roles and update the statistics with appropriate goals.
			if(this.parametersTuner.getNumIndependentCombinatorialProblems() == 1){

				goals = new int[1];
				goals[0] = simulationResult.getTerminalGoals()[this.gameDependentParameters.getMyRoleIndex()];

			}else if(this.parametersTuner.getNumIndependentCombinatorialProblems() == this.gameDependentParameters.getNumRoles()){

				goals = simulationResult.getTerminalGoals();

			}else{
				throw new RuntimeException("TunerAfterSimulation-afterSimulationActions(): combinatorial tuner is tuning for the wrong number of roles.");
			}*/

			this.parametersTuner.updateStatistics(simulationResult[0].getTerminalGoals());
		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		// Only the component that creates the tuner prints its content
		//return indentation + "PARAMETERS_TUNER = " + this.parametersTuner.printParametersTuner(indentation + "  ");

		// Here we only print the name
		return indentation + "PARAMETERS_TUNER = " + this.parametersTuner.getClass().getSimpleName();

	}


}
