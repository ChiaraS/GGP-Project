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
	 */
	private int numGenSamples;

	/**
	 * Maximum number of samples to be used for the evaluation pahse.
	 */
	private int numEvalSamples;

	/**
	 * True if when receiving the reward for a certain combinatorial action we want to use it to update the stats
	 * of all unit actions that form the combinatorial action. False if we want to use it only to update the stats
	 * for the only unit action that wasn't generated randomly when creating the combinatorial action.
	 */
	private boolean updateAll;

	public ProblemRepParameters(RandomSelector randomSelector, int numCandidatesToGenerate,
			int numGenSamples, int numEvalSamples, boolean updateAll) {
		this.randomSelector = randomSelector;
		this.numCandidatesToGenerate = numCandidatesToGenerate;
		this.numGenSamples = numGenSamples;
		this.numEvalSamples = numEvalSamples;
		this.updateAll = updateAll;
	}

	public int getNumGenSamples() {
		return numGenSamples;
	}

	public void setNumGenSamples(int numGenSamples) {
		this.numGenSamples = numGenSamples;
	}

	public int getNumEvalSamples() {
		return this.numEvalSamples;
	}

	public void setNumEvalSamples(int numEvalSamples) {
		this.numEvalSamples = numEvalSamples;
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
