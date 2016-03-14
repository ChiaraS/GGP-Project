package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.Strategy;

public interface AfterSimulationStrategy extends Strategy {

	public void afterSimulationActions();

}
