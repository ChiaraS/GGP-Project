package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.ProgressiveHistoryGRAVESelection;

public class ProgressiveHistoryAfterMove implements AfterMoveStrategy {

	private ProgressiveHistoryGRAVESelection phSelection;

	public ProgressiveHistoryAfterMove(ProgressiveHistoryGRAVESelection phSelection) {
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
