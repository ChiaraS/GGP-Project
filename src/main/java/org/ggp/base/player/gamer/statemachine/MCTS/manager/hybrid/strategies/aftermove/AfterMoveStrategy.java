package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;

public abstract class AfterMoveStrategy extends Strategy {

	public AfterMoveStrategy(GameDependentParameters gameDependentParameters) {
		super(gameDependentParameters);
	}

	public abstract void afterMoveActions();

}
