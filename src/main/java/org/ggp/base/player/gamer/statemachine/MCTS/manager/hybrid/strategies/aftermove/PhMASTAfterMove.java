package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;


public class PhMASTAfterMove extends AfterMoveStrategy {

	private MASTAfterMove mastAfterMove;

	private ProgressiveHistoryAfterMove phAfterMove;

	public PhMASTAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector, MASTAfterMove mastAfterMove, ProgressiveHistoryAfterMove phAfterMove) {

		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

		this.mastAfterMove = new MASTAfterMove(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

		this.phAfterMove = new ProgressiveHistoryAfterMove(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
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
