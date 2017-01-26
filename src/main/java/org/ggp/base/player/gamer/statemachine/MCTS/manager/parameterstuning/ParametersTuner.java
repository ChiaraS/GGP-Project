package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.MultiInstanceSearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.logging.GamerLogger;

public abstract class ParametersTuner extends MultiInstanceSearchManagerComponent{

	protected boolean tuneAllRoles;

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

	public ParametersTuner(GameDependentParameters gameDependentParameters,
			Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.tuneAllRoles = gamerSettings.getBooleanPropertyValue("ParametersTuner.tuneAllRoles");

	}

	public ParametersTuner(ParametersTuner toCopy) {
		super(toCopy);

		this.tuneAllRoles = toCopy.isTuningAllRoles();

		this.classesLength = null;

	}

	public void setClassesLength(int[] classesLength) {
		if(classesLength == null || classesLength.length == 0){
			GamerLogger.logError("SearchManagerCreation", "ParametersTuner - Initialization with null or empty list of classes length. No classes of actions to combine!");
			throw new RuntimeException("ParametersTuner - Initialization with null or empty list of classes length. No classes of actions to combine!");
		}
		this.classesLength = classesLength;
	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "TUNE_ALL_ROLES = " + this.tuneAllRoles;

		if(this.classesLength != null){
			String classesLengthString = "[ ";

			for(int i = 0; i < this.classesLength.length; i++){

				classesLengthString += this.classesLength[i] + " ";

			}

			classesLengthString += "]";

			params += indentation + "CLASSES_LENGTH = " + classesLengthString;
		}else{
			params += indentation + "CLASSES_LENGTH = null";
		}

		return params;

	}

	public abstract int[][] selectNextCombinations();

	public abstract int getNumIndependentCombinatorialProblems();

	public abstract void updateStatistics(int[] rewards);

	public abstract void logStats();

	public boolean isTuningAllRoles(){
		return this.tuneAllRoles;
	}

}
