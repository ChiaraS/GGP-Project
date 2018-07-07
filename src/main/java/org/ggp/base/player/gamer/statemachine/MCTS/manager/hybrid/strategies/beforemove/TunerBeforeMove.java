package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforemove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;

public class TunerBeforeMove extends BeforeMoveStrategy {

	private ParametersTuner parametersTuner;

	private double paramStatsDecreaseFactor;

	private boolean logAfterDecrease;

	public TunerBeforeMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		//this.paramStatsDecreaseFactor = gamerSettings.getDoublePropertyValue("AfterMoveStrategy" + componentID + ".paramStatsDecreaseFactor");
		//this.log = gamerSettings.getBooleanPropertyValue("AfterMoveStrategy" + componentID + ".log");

		this.paramStatsDecreaseFactor = gamerSettings.getDoublePropertyValue("BeforeMoveStrategy" + id + ".paramStatsDecreaseFactor");

		this.logAfterDecrease = gamerSettings.getBooleanPropertyValue("BeforeMoveStrategy" + id + ".logAfterDecrease");
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
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.parametersTuner = sharedReferencesCollector.getParametersTuner();
	}

	@Override
	public void beforeMoveActions() {

		if(this.paramStatsDecreaseFactor != 1.0){
			this.parametersTuner.decreaseStatistics(this.paramStatsDecreaseFactor);
			if(logAfterDecrease){
				this.parametersTuner.logStats();
			}
		}

	}

	@Override
	public String getComponentParameters(String indentation) {
		// Only the component that creates the tuner prints its content
		//return indentation + "PARAMETERS_TUNER = " + this.parametersTuner.printParametersTuner(indentation + "  ");

		// Here we only print the name
		return indentation + "PARAMS_STATS_DECREASE_FACTOR = " + this.paramStatsDecreaseFactor +
				indentation + "PARAMETERS_TUNER = " + this.parametersTuner.getClass().getSimpleName() +
				indentation + "LOG_AFTER_DECREASE = " + logAfterDecrease;
	}

}
