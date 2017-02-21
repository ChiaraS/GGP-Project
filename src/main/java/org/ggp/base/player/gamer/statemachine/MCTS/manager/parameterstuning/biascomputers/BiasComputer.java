package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.biascomputers;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public abstract class BiasComputer extends SearchManagerComponent {

	public BiasComputer(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	public abstract double computeMoveBias(double moveNormalizedAvgValue, double moveVisits, double penalty);

}
