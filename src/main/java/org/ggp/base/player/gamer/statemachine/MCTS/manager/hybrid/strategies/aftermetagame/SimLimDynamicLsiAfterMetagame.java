package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermetagame;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.SimLimitedLsiParametersTuner;

public class SimLimDynamicLsiAfterMetagame extends AfterMetagameStrategy {

	private SimLimitedLsiParametersTuner simLimitedLsiParametersTuner;

	public SimLimDynamicLsiAfterMetagame(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// Do nothing
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.simLimitedLsiParametersTuner = sharedReferencesCollector.getSimLimitedLsiParametersTuner();
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
		this.simLimitedLsiParametersTuner.estimateTotalNumberOfSamples();
	}

	@Override
	public String getComponentParameters(String indentation) {
		return null;
	}

}
