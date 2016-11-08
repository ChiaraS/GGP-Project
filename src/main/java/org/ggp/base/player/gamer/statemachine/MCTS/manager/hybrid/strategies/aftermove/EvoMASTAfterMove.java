package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;


public class EvoMASTAfterMove implements AfterMoveStrategy {

	private MASTAfterMove mastAfterMove;

	private EvoAfterMove evoAfterMove;

	public EvoMASTAfterMove(MASTAfterMove mastAfterMove, EvoAfterMove evoAfterMove){

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
