package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

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
	public void afterSimulationActions(SimulationResult simulationResult) {

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

			this.parametersTuner.updateStatistics(simulationResult.getTerminalGoals());
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
