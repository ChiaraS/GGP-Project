package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.ProgressiveHistoryGraveSelection;


public class ProgressiveHistoryAfterMove extends AfterMoveStrategy {

	private ProgressiveHistoryGraveSelection phSelection;

	public ProgressiveHistoryAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.phSelection = sharedReferencesCollector.getProgressiveHistoryGraveSelection();
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
	public String getComponentParameters(String indentation) {
		return null;
	}

	@Override
	public void afterMoveActions() {

		this.phSelection.resetCurrentRootAmafStats();

	}

}
