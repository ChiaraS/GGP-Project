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
	 * This method defines how to order the parameters given as input. Different tuners might need to reorder
	 * the parameters in different ways and/or at different moments in time (e.g. the hierarchical single
	 * MAB tuner needs to impose a certain order on the parameters at the moment when the player is created
	 * and can use this class to either order them randomly, or in a predefined order specified in the
	 * settings. The sequential tuner, instead, might need to impose an order on the parameters to know in
	 * which predefined order to tune them singularly.
	 */
	public abstract <T extends TunableParameter> void imposeOrder(List<T> tunableParameters);

}
