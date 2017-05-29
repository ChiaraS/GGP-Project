package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.SequentialParametersTuner;

/**
 * Only use this class when tuning sequentially AND changing parameter after the search for every move.
 *
 * @author C.Sironi
 *
 */
public class SequentialTunerAfterMove extends AfterMoveStrategy {

	private SequentialParametersTuner parametersTuner;

	public SequentialTunerAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.parametersTuner = (SequentialParametersTuner) sharedReferencesCollector.getParametersTuner();
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
	public void afterMoveActions() {
		this.parametersTuner.changeTunedParameter();
	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "PARAMETERS_TUNER = " + this.parametersTuner.getClass().getSimpleName();
	}

}
