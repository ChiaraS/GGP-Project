package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public abstract class TwoPhaseParametersTuner extends ParametersTuner {



	public TwoPhaseParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// TODO Auto-generated method stub
	}

	@Override
	public void clearComponent() {
		super.clearComponent();
	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();
	}

	@Override
	public void setNextCombinations() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setBestCombinations() {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateStatistics(int[] rewards) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logStats() {
		// TODO Auto-generated method stub

	}

	@Override
	public void decreaseStatistics(double factor) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getComponentParameters(String indentation) {

		/*
		String superParams = super.getComponentParameters(indentation);

		if(superParams != null){
			return superParams + indentation + "num_roles_problems = " + (this.roleProblems != null ? this.roleProblems.length : 0);
		}else{
			return indentation + "num_roles_problems = " + (this.roleProblems != null ? this.roleProblems.length : 0);
		}
		*/
		return null;

	}


}
