package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.rescalers;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

/**
 * This class re-scales a value x in R using a function that maps the interval [min, max]
 * to the interval [newMin, newMax].
 *
 * @author c.sironi
 *
 */
public abstract class ValueRescaler extends SearchManagerComponent {

	public ValueRescaler(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	public abstract double mapToInterval(double x, double min, double max, double newMin, double newMax);

}
