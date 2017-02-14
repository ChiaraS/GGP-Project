package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parametersorders;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;

public class RandomOrder extends ParametersOrder {

	public RandomOrder(GameDependentParameters gameDependentParameters,	Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
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
	public String getComponentParameters(String indentation) {
		return null;
	}

	@Override
	public void imposeInitialOrderForPlayer(List<TunableParameter> tunableParameters) {

		// Randomize the order of the parameters (needed by the HierarchicalSingleMabTuner)
		Collections.shuffle(tunableParameters);

	}
}
