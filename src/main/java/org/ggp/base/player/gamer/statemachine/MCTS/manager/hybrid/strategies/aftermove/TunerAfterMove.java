package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;

public class TunerAfterMove extends AfterMoveStrategy {

	private ParametersTuner parametersTuner;

	private double paramStatsDecreaseFactor;

	private boolean log;

	public TunerAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.paramStatsDecreaseFactor = gamerSettings.getDoublePropertyValue("AfterMoveStrategy.paramStatsDecreaseFactor");

		this.log = gamerSettings.getBooleanPropertyValue("AfterMoveStrategy.log");
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.parametersTuner = sharedReferencesCollector.getParametersTuner();
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

		if(log){
			this.parametersTuner.logStats();
		}
		this.parametersTuner.decreaseStatistics(this.paramStatsDecreaseFactor);
		if(log){
			this.parametersTuner.logStats();
		}

	}

	@Override
	public String getComponentParameters(String indentation) {
		// Only the component that creates the tuner prints its content
		//return indentation + "PARAMETERS_TUNER = " + this.parametersTuner.printParametersTuner(indentation + "  ");

		// Here we only print the name
		return indentation + "PARAMS_STATS_DECREASE_FACTOR = " + this.paramStatsDecreaseFactor +
				indentation + "PARAMETERS_TUNER = " + this.parametersTuner.getClass().getSimpleName();
	}

}
