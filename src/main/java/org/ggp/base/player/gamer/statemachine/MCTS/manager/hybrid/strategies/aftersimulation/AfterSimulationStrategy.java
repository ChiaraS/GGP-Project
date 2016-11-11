package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public abstract class AfterSimulationStrategy extends Strategy{

	public AfterSimulationStrategy(GameDependentParameters gameDependentParameters) {

		super(gameDependentParameters);

	}

	public abstract void afterSimulationActions(SimulationResult simulationResult);

}
