package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class RandomSelector extends TunerSelector{

	public RandomSelector(GameDependentParameters gameDependentParameters,
			Random random, GamerSettings gamerSettings,	SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);
	}

	@Override
	public void setReferences(
			SharedReferencesCollector sharedReferencesCollector) {
		// Do nothing

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
	public int selectMove(MoveStats[] movesStats, int numUpdates) {
		return this.random.nextInt(movesStats.length);
	}

	@Override
	public String getComponentParameters(String indentation) {
		return null;
	}

}
