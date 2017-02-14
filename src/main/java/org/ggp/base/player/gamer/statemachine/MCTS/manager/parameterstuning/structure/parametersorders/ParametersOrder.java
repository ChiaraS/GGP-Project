package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parametersorders;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;

/**
 * Some parameters tuners need parameters to be ordered in a certain way.
 *
 * This class takes care of rearranging the tunable parameters for the tuners.
 *
 * @author C.Sironi
 *
 */
public abstract class ParametersOrder extends SearchManagerComponent{

	public ParametersOrder(GameDependentParameters gameDependentParameters,	Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	/**
	 * This method defines how to order the parameters right after the creation of a new player
	 * and before such player starts playing any game.
	 */
	public abstract void imposeInitialOrderForPlayer(List<TunableParameter> tunableParameters);

}
