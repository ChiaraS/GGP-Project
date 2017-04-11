package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class LSIParametersTuner extends TwoPhaseParametersTuner {

	/**
	 * Number of samples (i.e. simulations) that will be dedicated to the generation of
	 * candidate combinatorial actions (i.e. combinations of parameters) that will be
	 * evaluated in the subsequent phase.
	 */
	private int numGenSamples;

	/**
	 * Number of samples (i.e. simulations) that will be dedicated to the evaluation of
	 * the generated combinatorial actions (i.e. combinations of parameters) before
	 * committing to a single combinatorial action (i.e. combination of parameters).
	 */
	private int numEvalSamples;

	/**
	 * Counts the total number of samples taken so far.
	 */
	private int totalSamplesCounter;

	public LSIParametersTuner(GameDependentParameters gameDependentParameters, Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setNextCombinations() {

		/*
		if(generation){

			//

		}else if(evaluation){

		}else{
			return best;
		}
		*/

	}

	@Override
	public void setBestCombinations() {
		// TODO Auto-generated method stub

		this.stopTuning();
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
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isMemorizingBestCombo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancelMemorizedBestCombo() {
		// TODO Auto-generated method stub

	}

}
