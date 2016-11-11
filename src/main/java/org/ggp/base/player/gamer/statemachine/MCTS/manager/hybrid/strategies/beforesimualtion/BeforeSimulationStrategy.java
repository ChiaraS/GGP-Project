package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;

public abstract class BeforeSimulationStrategy extends Strategy {

	public BeforeSimulationStrategy(GameDependentParameters gameDependentParameters) {
		super(gameDependentParameters);
	}

	public abstract void beforeSimulationActions();

}
