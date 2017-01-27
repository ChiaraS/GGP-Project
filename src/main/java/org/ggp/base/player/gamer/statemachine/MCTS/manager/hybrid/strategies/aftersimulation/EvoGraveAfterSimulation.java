package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public class EvoGraveAfterSimulation extends AfterSimulationStrategy {

	private GraveAfterSimulation graveAfterSimulation;

	private EvoAfterSimulation evoAfterSimulation;

	public EvoGraveAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.graveAfterSimulation = new GraveAfterSimulation(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, "");

		this.evoAfterSimulation = new EvoAfterSimulation(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, "");

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
	public String getComponentParameters(String indentation) {
		return indentation + "AFTER_SIM_STRATEGY_1 = " + this.graveAfterSimulation.printComponent(indentation + "  ") + indentation + "AFTER_SIM_STRATEGY_2 = " + this.evoAfterSimulation.printComponent(indentation + "  ");
	}

}
