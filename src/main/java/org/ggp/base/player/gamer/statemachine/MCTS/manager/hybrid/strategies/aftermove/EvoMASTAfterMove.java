package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;


public class EvoMASTAfterMove extends AfterMoveStrategy {

	private MASTAfterMove mastAfterMove;

	private EvoAfterMove evoAfterMove;

	public EvoMASTAfterMove(GameDependentParameters gameDependentParameters, Random random, Properties properties, SharedReferencesCollector sharedReferencesCollector, MASTAfterMove mastAfterMove, EvoAfterMove evoAfterMove){

		super(gameDependentParameters, random, properties, sharedReferencesCollector);

		this.mastAfterMove = mastAfterMove;

		this.evoAfterMove = evoAfterMove;

	}

	@Override
	public void afterMoveActions(){

		this.mastAfterMove.afterMoveActions();

		this.evoAfterMove.afterMoveActions();

	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	@Override
	public String getComponentParameters() {
		return "(SUB_AFTER_MOVE_STRATEGY = " + this.mastAfterMove.printComponent() + ", SUB_AFTER_SIM_STRATEGY = " + this.evoAfterMove.printComponent() + ")";
	}

}
