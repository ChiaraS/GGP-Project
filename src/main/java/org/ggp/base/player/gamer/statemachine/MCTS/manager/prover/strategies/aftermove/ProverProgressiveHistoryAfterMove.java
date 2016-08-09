package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.aftermove;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.AfterMoveStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.ProverProgressiveHistoryGRAVESelection;

public class ProverProgressiveHistoryAfterMove implements AfterMoveStrategy {

	private ProverProgressiveHistoryGRAVESelection phSelection;

	public ProverProgressiveHistoryAfterMove(ProverProgressiveHistoryGRAVESelection phSelection) {
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
