package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class SinglePopEvoParametersTuner extends ParametersTuner {

	public SinglePopEvoParametersTuner(
			GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings,
				sharedReferencesCollector);
		// TODO Auto-generated constructor stub
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
	public int getNumIndependentCombinatorialProblems() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateStatistics(int[] goals) {
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
	public boolean isMemorizingBestCombo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void memorizeBestCombinations() {
		// TODO Auto-generated method stub

	}

}
