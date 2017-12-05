package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftergame;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.SimLimitedLsiParametersTuner;

public class SimLimLsiTunerAfterGame extends AfterGameStrategy {

	private SimLimitedLsiParametersTuner simLimitedLsiParametersTuner;

	private boolean logSamplesDistributionAfterGame;

	public SimLimLsiTunerAfterGame(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.logSamplesDistributionAfterGame = gamerSettings.getBooleanPropertyValue("AfterGameStrategy" + id + ".logSamplesDistributionAfterGame");
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
	public void afterGameActions(List<Integer> terminalGoals) {
		if(this.logSamplesDistributionAfterGame) {
			this.simLimitedLsiParametersTuner.logSamplesDistribution();
		}
	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "SIM_LIMITED_LSI_PARAMETERS_TUNER = " + this.simLimitedLsiParametersTuner.getClass().getSimpleName() +
				 indentation + "LOG_SAMPLES_DISTRIBUTION_AFTER_GAME = " + this.logSamplesDistributionAfterGame;
	}

}
