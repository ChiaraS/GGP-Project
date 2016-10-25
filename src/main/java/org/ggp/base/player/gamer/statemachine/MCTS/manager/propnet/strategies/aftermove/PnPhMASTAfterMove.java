package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove.AfterMoveStrategy;

public class PnPhMASTAfterMove implements AfterMoveStrategy {

	private PnMASTAfterMove mastAfterMove;

	private PnProgressiveHistoryAfterMove phAfterMove;

	public PnPhMASTAfterMove(PnMASTAfterMove mastAfterMove, PnProgressiveHistoryAfterMove phAfterMove) {

		this.mastAfterMove = mastAfterMove;

		this.phAfterMove = phAfterMove;

	}

	@Override
	public String getStrategyParameters() {
		return "AFTER_MOVE_1 = " + this.mastAfterMove.printStrategy() + ", AFTER_MOVE_2 = " + this.phAfterMove.printStrategy();
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

		this.mastAfterMove.afterMoveActions();

		this.phAfterMove.afterMoveActions();

	}

}
