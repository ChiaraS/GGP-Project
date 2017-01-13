package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public class TunerAfterSimulation extends AfterSimulationStrategy {

	private ParametersTuner combinatorialTuner;

	public TunerAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.combinatorialTuner = sharedReferencesCollector.getParametersTuner();
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

		// We have to check if the CombinatorialTuner is tuning parameters only for the playing role
		// or for all roles and update the statistics with appropriate goals.
		if(this.combinatorialTuner.getNumIndependentCombinatorialProblems() == 1){

			goals = new int[1];
			goals[0] = simulationResult.getTerminalGoals()[this.gameDependentParameters.getMyRoleIndex()];

		}else if(this.combinatorialTuner.getNumIndependentCombinatorialProblems() == this.gameDependentParameters.getNumRoles()){

			goals = simulationResult.getTerminalGoals();

		}else{
			throw new RuntimeException("TunerAfterSimulation-afterSimulationActions(): combinatorial tuner is tuning for the wrong number of roles.");
		}

		this.combinatorialTuner.updateStatistics(goals);

	}

	@Override
	public String getComponentParameters(String indentation) {

		// Only the component that creates the tuner prints its content
		//return indentation + "COMBINATORIAL_TUNER = " + this.combinatorialTuner.printCombinatorialTuner(indentation + "  ");

		// Here we only print the name
		return indentation + "COMBINATORIAL_TUNER = " + this.combinatorialTuner.getClass().getSimpleName();

	}


}
