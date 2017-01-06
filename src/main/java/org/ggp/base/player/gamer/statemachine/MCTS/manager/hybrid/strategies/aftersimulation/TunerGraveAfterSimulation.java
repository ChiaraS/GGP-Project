package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public class TunerGraveAfterSimulation extends AfterSimulationStrategy {

	private GraveAfterSimulation graveAfterSimulation;

	private TunerAfterSimulation tunerAfterSimulation;

	public TunerGraveAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.graveAfterSimulation = new GraveAfterSimulation(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.tunerAfterSimulation = new TunerAfterSimulation(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.graveAfterSimulation.setReferences(sharedReferencesCollector);
		this.tunerAfterSimulation.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		this.graveAfterSimulation.clearComponent();
		this.tunerAfterSimulation.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.graveAfterSimulation.setUpComponent();
		this.tunerAfterSimulation.setUpComponent();
	}

	@Override
	public void afterSimulationActions(SimulationResult simulationResult){

		this.graveAfterSimulation.afterSimulationActions(simulationResult);

		this.tunerAfterSimulation.afterSimulationActions(simulationResult);

	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "AFTER_SIM_STRATEGY_1 = " + this.graveAfterSimulation.printComponent(indentation + "  ") + indentation + "AFTER_SIM_STRATEGY_2 = " + this.tunerAfterSimulation.printComponent(indentation + "  ");
	}

}
