package org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning;

import org.ggp.base.util.logging.GamerLogger;

public abstract class CombinatorialTuner {

	/**
	 * Size of the classes of unit actions of the combinatorial problem.
	 *
	 * Each parameter being tuned corresponds to one class. The size of a class is the number of
	 * different values that the parameter can assume. Each unit move for a class corresponds to
	 * the assignment of one of the available values to the parameter. A combinatorial move is
	 * thus an assignment of a value to each of the parameters being tuned.
	 *
	 * Note that in this class we represent each unit move as the index in the list of possible
	 * values of the value that the move assigns to the parameter corresponding to the class.
	 * A combinatorial move is thus a list of indices, each of them indicating the index of the
	 * selected value for the corresponding parameter.
	 *
	 * Note that since we represent each unit move as an index we don't need to know he exact values
	 * that each unit move assigns to the corresponding parameter. It's sufficient to know how many
	 * parameters there are per class to know how many indices we have to deal with.
	 *
	 * Note that we either tune all the parameters only for our role or for all the roles. Tuning
	 * different parameters for different roles is not permitted.
	 *
	 */
	protected int[] classesLength;

	public void setClassesLength(int[] classesLength) {
		if(classesLength == null || classesLength.length == 0){
			GamerLogger.logError("SearchManagerCreation", "CombinatorialTuner - Initialization with null or empty list of classes length. No classes of actions to combine!");
			throw new RuntimeException("CombinatorialProblemRepresentation - Initialization with null or empty list of classes length. No classes of actions to combine!");
		}
		this.classesLength = classesLength;
	}

    /**
     * After the end of each game clear the tuner.
     */
	public abstract void clear();

	public abstract void setUp(int numRolesToTune);

	public abstract int[][] selectNextCombinations();

	public abstract String printCombinatorialTuner(String indentation);

	public abstract int getNumIndependentCombinatorialProblems();

	public abstract void updateStatistics(int[] rewards);

	public abstract void logStats();
}
