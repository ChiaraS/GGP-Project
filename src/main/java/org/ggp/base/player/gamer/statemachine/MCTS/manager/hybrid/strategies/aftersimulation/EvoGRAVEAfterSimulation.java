package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public class EvoGRAVEAfterSimulation extends AfterSimulationStrategy {

	private GRAVEAfterSimulation graveAfterSimulation;

	private EvoAfterSimulation evoAfterSimulation;

	public EvoGRAVEAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

		this.graveAfterSimulation = new GRAVEAfterSimulation(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

		this.evoAfterSimulation = new EvoAfterSimulation(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.graveAfterSimulation.setReferences(sharedReferencesCollector);
		this.evoAfterSimulation.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		this.graveAfterSimulation.clearComponent();
		this.evoAfterSimulation.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.graveAfterSimulation.setUpComponent();
		this.evoAfterSimulation.setUpComponent();
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