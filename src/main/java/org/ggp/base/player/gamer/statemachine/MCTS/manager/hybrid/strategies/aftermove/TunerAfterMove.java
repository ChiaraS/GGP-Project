package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;

public class TunerAfterMove extends AfterMoveStrategy {

	private ParametersTuner parametersTuner;

	public TunerAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
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
	public String getComponentParameters(String indentation) {
		// Only the component that creates the tuner prints its content
		//return indentation + "PARAMETERS_TUNER = " + this.parametersTuner.printParametersTuner(indentation + "  ");

		// Here we only print the name
		return indentation + "PARAMETERS_TUNER = " + this.parametersTuner.getClass().getSimpleName();
	}

	@Override
	public void afterMoveActions() {

		this.parametersTuner.logStats();

	}

}
