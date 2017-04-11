package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;

public class TunerAfterGame extends AfterGameStrategy {

	private ParametersTuner parametersTuner;

	public TunerAfterGame(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);
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
	public void afterGameActions(List<Integer> terminalGoals) {
		// If the tuner was still tuning we let it set and memorize the best combination of parameters values
		if(this.parametersTuner.isTuning()){
			this.parametersTuner.setBestCombinations();
		}
		if(terminalGoals.get(this.gameDependentParameters.getMyRoleIndex()).intValue() != 100.0){
			this.parametersTuner.cancelMemorizedBestCombo();
		}
		this.parametersTuner.logStats();
	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "PARAMETERS_TUNER = " + this.parametersTuner.getClass().getSimpleName();
	}

}
