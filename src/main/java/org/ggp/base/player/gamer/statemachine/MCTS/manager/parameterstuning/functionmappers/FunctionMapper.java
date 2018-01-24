package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.functionmappers;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

/**
 * This class maps a value in R in the interval given by lowerBound and upperBound.
 *
 * @author c.sironi
 *
 */
public abstract class FunctionMapper extends SearchManagerComponent {

	public FunctionMapper(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	public abstract double mapToInterval(double upperBound, double lowerBound, double valueInR);

}
