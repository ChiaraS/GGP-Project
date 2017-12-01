package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.RandomSelector;

/**
 * This class collects all parameters that are shared by all SimLimitedLsiProblemRepresentations
 *
 */
public class ProblemRepParameters {

	/**
	 * Random selector used to select random values for the parameters when completing combinatorial moves.
	 */
	private RandomSelector randomSelector;

	/**
	 * Number of combinations to be generated for the evaluation phase.
	 */
	private int numCandidatesToGenerate;

	/**
	 * Maximum number of samples to be used for the generation phase.
	 * Can be re-set dynamically if we are computing it at runtime trying to estimate the total number of available samples.
	 */
	private int dynamicNumGenSamples;

	/**
	 * Maximum number of samples to be used for the evaluation phase.
	 * Can be re-set dynamically if we are computing it at runtime trying to estimate the total number of available samples.
	 */
	private int dynamicNumEvalSamples;

	/**
	 * True if when receiving the reward for a certain combinatorial action we want to use it to update the stats
	 * of all unit actions that form the combinatorial action. False if we want to use it only to update the stats
	 * for the only unit action that wasn't generated randomly when creating the combinatorial action.
	 */
	private boolean updateAll;

	public ProblemRepParameters(RandomSelector randomSelector, int numCandidatesToGenerate, boolean updateAll) {
		this.randomSelector = randomSelector;
		this.numCandidatesToGenerate = numCandidatesToGenerate;
		this.dynamicNumGenSamples = -1;
		this.dynamicNumEvalSamples = -1;
		this.updateAll = updateAll;
	}

	public int getDynamicNumGenSamples() {
		return dynamicNumGenSamples;
	}

	public void setDynamicNumGenSamples(int dynamicNumGenSamples) {
		this.dynamicNumGenSamples = dynamicNumGenSamples;
	}

	public int getDynamicNumEvalSamples() {
		return this.dynamicNumEvalSamples;
	}

	public void setDynamicNumEvalSamples(int dynamicNumEvalSamples) {
		this.dynamicNumEvalSamples = dynamicNumEvalSamples;
	}

	public RandomSelector getRandomSelector() {
		return randomSelector;
	}

	public int getNumCandidatesToGenerate() {
		return numCandidatesToGenerate;
	}

	public boolean getUpdateAll() {
		return this.updateAll;
	}

}
