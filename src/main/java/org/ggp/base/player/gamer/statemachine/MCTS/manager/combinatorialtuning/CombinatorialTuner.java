package org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning;

import org.ggp.base.util.logging.GamerLogger;

public abstract class CombinatorialTuner {

	/**
	 * Size of the classes of unit actions of the combinatorial problem.
	 * In this case each unit action corresponds to the assignment of a given value to one
	 * of the parameters we are tuning. Each class corresponds to one of the parameters being
	 * tuned. As abstraction we represent unit actions with the index that the value they assign
	 * has in the array of available values for the parameter. A combinatorial move is thus
	 * represented as an array of indices, one for each parameter being tuned.
	 * In order to compute the index of each action of each class we only need the size of the
	 * classes since the combinatorial tuner doesn't need to know which unit action corresponds to
	 * which index. This class can be used to make decisions in a combinatorial space independently
	 * from the types of actions, as long as they are represented by an index and there is someone
	 * outside that can map indices to unit actions.
	 */
	protected int[] classesLength;

	public CombinatorialTuner(int[] classesLength) {
		if(classesLength == null || classesLength.length == 0){
			GamerLogger.logError("SearchManagerCreation", "CombinatorialTuner - Initialization with null or empty list of classes length. No classes of actions to combine!");
			throw new RuntimeException("CombinatorialTuner - Initialization with null or empty list of classes length. No classes of actions to combine!");
		}
		this.classesLength = classesLength;
	}

}
