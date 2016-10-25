package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove.AfterMoveStrategy;

public class PnEvoAfterMove implements AfterMoveStrategy {

	private SingleParameterEvolutionManager evolutionManager;

	public PnEvoAfterMove(SingleParameterEvolutionManager evolutionManager) {

		this.evolutionManager = evolutionManager;
	}

	@Override
	public String getStrategyParameters() {
		return this.evolutionManager.printEvolutionManager();
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[AFTER_MOVE_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[AFTER_MOVE_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

	@Override
	public void afterMoveActions() {

		this.evolutionManager.logIndividualsState();

	}

}
