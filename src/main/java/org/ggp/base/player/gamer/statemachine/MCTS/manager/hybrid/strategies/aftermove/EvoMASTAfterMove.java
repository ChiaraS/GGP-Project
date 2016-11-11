package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;


public class EvoMASTAfterMove extends AfterMoveStrategy {

	private MASTAfterMove mastAfterMove;

	private EvoAfterMove evoAfterMove;

	public EvoMASTAfterMove(GameDependentParameters gameDependentParameters, MASTAfterMove mastAfterMove, EvoAfterMove evoAfterMove){

		super(gameDependentParameters);

		this.mastAfterMove = mastAfterMove;

		this.evoAfterMove = evoAfterMove;

	}

	@Override
	public void afterMoveActions(){

		this.mastAfterMove.afterMoveActions();

		this.evoAfterMove.afterMoveActions();

	}

	@Override
	public String getStrategyParameters() {
		return "(SUB_AFTER_MOVE_STRATEGY = " + this.mastAfterMove.printStrategy() + ", SUB_AFTER_SIM_STRATEGY = " + this.evoAfterMove.printStrategy() + ")";
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

}
