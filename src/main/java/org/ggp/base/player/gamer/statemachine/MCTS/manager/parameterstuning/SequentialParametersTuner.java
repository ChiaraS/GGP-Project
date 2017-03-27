package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class SequentialParametersTuner extends ParametersTuner {

	/**
	 * If true, after all the parameters have been all tuned once sequentially,
	 * this tuner will randomize their order before tuning them all again sequentially.
	 * If false, they will be tuned sequentially repeatedly always in the same order.
	 */
	private boolean shuffleTuningOrder;

	public SequentialParametersTuner(
			GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings,
				sharedReferencesCollector);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int[][] selectNextCombinations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[][] getBestCombinations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumIndependentCombinatorialProblems() {
		// TODO Auto-generated method stub
		return 0;
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
	public void setReferences(
			SharedReferencesCollector sharedReferencesCollector) {
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

}
