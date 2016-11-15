package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public class EvoGRAVEAfterSimulation extends AfterSimulationStrategy {

	private GRAVEAfterSimulation graveAfterSimulation;

	private EvoAfterSimulation evoAfterSimulation;

	public EvoGRAVEAfterSimulation(GameDependentParameters gameDependentParameters, Random random, Properties properties, SharedReferencesCollector sharedReferencesCollector, GRAVEAfterSimulation graveAfterSimulation, EvoAfterSimulation evoAfterSimulation) {

		super(gameDependentParameters, random, properties, sharedReferencesCollector);

		this.graveAfterSimulation = graveAfterSimulation;

		this.evoAfterSimulation = evoAfterSimulation;

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
	public void afterSimulationActions(SimulationResult simulationResult){

		this.graveAfterSimulation.afterSimulationActions(simulationResult);

		this.evoAfterSimulation.afterSimulationActions(simulationResult);

	}

	@Override
	public String getComponentParameters() {
		return "(SUB_AFTER_SIM_STRATEGY = " + this.graveAfterSimulation.printComponent() + ", SUB_AFTER_SIM_STRATEGY = " + this.evoAfterSimulation.printComponent() + ")";
	}

}
