package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermetagame;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsTranspositionTable;

public class ResetAfterMetagame extends AfterMetagameStrategy {

	private MctsTranspositionTable transpositionTable;

	public ResetAfterMetagame(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.transpositionTable = sharedReferencesCollector.getTranspositionTable();
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
	public void afterMetagameActions() {
		this.transpositionTable.clearComponent();
	}

	@Override
	public String getComponentParameters(String indentation) {
		return null;
	}

}
