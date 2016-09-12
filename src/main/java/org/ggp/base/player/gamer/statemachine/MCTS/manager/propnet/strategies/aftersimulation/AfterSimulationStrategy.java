package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;

public interface AfterSimulationStrategy extends Strategy {

	public void afterSimulationActions(int[] goals);

}
