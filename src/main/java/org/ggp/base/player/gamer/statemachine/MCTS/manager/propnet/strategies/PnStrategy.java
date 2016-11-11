package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies;

public interface PnStrategy {

	/**
	 * Creates a string representing the strategy parameters and their values
	 * to be used for logging purposes.
	 *
	 * @return a string representing the strategy parameters and their values.
	 */
	public String getStrategyParameters();

	/**
	 * Creates a string representing the exact name of the strategy and the parameters
	 * it is using as returned by getStrategyParameters().
	 *
	 * @return a string representing the exact name of the strategy and the parameters
	 * it is using.
	 */
	public String printStrategy();

}
