package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;


public class PhMASTAfterMove extends AfterMoveStrategy {

	private MASTAfterMove mastAfterMove;

	private ProgressiveHistoryAfterMove phAfterMove;

	public PhMASTAfterMove(GameDependentParameters gameDependentParameters, MASTAfterMove mastAfterMove, ProgressiveHistoryAfterMove phAfterMove) {

		super(gameDependentParameters);

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
