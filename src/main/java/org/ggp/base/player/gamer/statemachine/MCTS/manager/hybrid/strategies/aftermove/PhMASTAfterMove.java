package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;


public class PhMASTAfterMove extends AfterMoveStrategy {

	private MASTAfterMove mastAfterMove;

	private ProgressiveHistoryAfterMove phAfterMove;

	public PhMASTAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

		this.mastAfterMove = new MASTAfterMove(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

		this.phAfterMove = new ProgressiveHistoryAfterMove(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.mastAfterMove.setReferences(sharedReferencesCollector);
		this.phAfterMove.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		this.mastAfterMove.clearComponent();
		this.phAfterMove.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.mastAfterMove.setUpComponent();
		this.phAfterMove.setUpComponent();
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
