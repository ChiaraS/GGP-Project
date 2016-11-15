package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;


public class PhMASTAfterMove extends AfterMoveStrategy {

	private MASTAfterMove mastAfterMove;

	private ProgressiveHistoryAfterMove phAfterMove;

	public PhMASTAfterMove(GameDependentParameters gameDependentParameters, Random random, Properties properties, SharedReferencesCollector sharedReferencesCollector, MASTAfterMove mastAfterMove, ProgressiveHistoryAfterMove phAfterMove) {

		super(gameDependentParameters, random, properties, sharedReferencesCollector);

		this.mastAfterMove = mastAfterMove;

		this.phAfterMove = phAfterMove;

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
		return "AFTER_MOVE_1 = " + this.mastAfterMove.printComponent() + ", AFTER_MOVE_2 = " + this.phAfterMove.printComponent();
	}

	@Override
	public void afterMoveActions() {

		this.mastAfterMove.afterMoveActions();

		this.phAfterMove.afterMoveActions();

	}

}
