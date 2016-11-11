package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;


public abstract class Strategy extends SearchManagerComponent{

	public Strategy(GameDependentParameters gameDependentParameters){
		super(gameDependentParameters);
	}

	/**
	 * Creates a string representing the strategy parameters and their values
	 * to be used for logging purposes.
	 *
	 * @return a string representing the strategy parameters and their values.
	 */
	public abstract String getStrategyParameters();

	/**
	 * Creates a string representing the exact name of the strategy and the parameters
	 * it is using as returned by getStrategyParameters().
	 *
	 * @return a string representing the exact name of the strategy and the parameters
	 * it is using.
	 */
	public abstract String printStrategy();

	/**
	 * Clears all the parameters and references to object that are game dependent (e.g.
	 * the reference to the state machine, the number of roles, statistics collected so
	 * far, ecc...).
	 *
	 * BE CAREFUL!: when clearing objects, don't change the reference to them (except for
	 * the state machine), but only clear their content, since they might be shared on
	 * purpose with other strategies. If possible make the reference FINAL.
	 */
	//public void clearStrategy();

	/**
	 *
	 */
	//public void resetStrategy(AbstractStateMachine theNewMachine, int newNumRoles, int newMyRoleIndex);

}
