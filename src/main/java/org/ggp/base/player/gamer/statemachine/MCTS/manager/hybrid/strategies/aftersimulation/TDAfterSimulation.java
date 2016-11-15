package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.TDBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public class TDAfterSimulation extends AfterSimulationStrategy {

	private TDBackpropagation backpropagation;

	public TDAfterSimulation(GameDependentParameters gameDependentParameters, Random random, Properties properties, SharedReferencesCollector sharedReferencesCollector, TDBackpropagation backpropagation) {

		super(gameDependentParameters, random, properties, sharedReferencesCollector);

		this.backpropagation = backpropagation;
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
	public String getComponentParameters() {
		return null;
	}

	@Override
	public void afterSimulationActions(SimulationResult simulationResult) {

		this.backpropagation.resetSimulationParameters();

	}
}
