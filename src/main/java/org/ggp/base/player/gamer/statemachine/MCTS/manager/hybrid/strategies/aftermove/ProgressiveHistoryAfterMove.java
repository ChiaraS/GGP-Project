package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.ProgressiveHistoryGRAVESelection;


public class ProgressiveHistoryAfterMove extends AfterMoveStrategy {

	private ProgressiveHistoryGRAVESelection phSelection;

	public ProgressiveHistoryAfterMove(GameDependentParameters gameDependentParameters, ProgressiveHistoryGRAVESelection phSelection) {

		super(gameDependentParameters);

		this.phSelection = phSelection;
	}

	@Override
	public String getStrategyParameters() {
		return null;
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

		this.phSelection.resetCurrentRootAmafStats();

	}

}
