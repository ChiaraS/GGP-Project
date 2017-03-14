package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public abstract class ParametersTuner extends SearchManagerComponent{

	protected boolean tuneAllRoles;

	/**
	 * Names of the classes being considered, i.e. names of the parameters being tuned.
	 */
	protected String[] classesNames;

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

	/**
	 * Needed only for logging. Given this values we could avoid keeping as parameter the classesLenght,
	 * because the length of each class can be deduced from here.
	 */
	protected String[][] classesValues;

	/**
	 * For each class, for each unit move in the class, this array specifies the penalty.
	 * NOTE: the penalty values must be specified for either all or none of the classes,
	 * otherwise an exception must be thrown.
	 * If there is no penalty specified in the gamers settings for any of the classes,
	 * then this pointer will be null.
	 */
	protected double[][] unitMovesPenalty;

	public ParametersTuner(GameDependentParameters gameDependentParameters,
			Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.tuneAllRoles = gamerSettings.getBooleanPropertyValue("ParametersTuner.tuneAllRoles");

	}

	public ParametersTuner(ParametersTuner toCopy) {
		super(toCopy);

		this.tuneAllRoles = toCopy.isTuningAllRoles();

		this.classesNames = null;

		this.classesLength = null;

		this.classesValues = null;

		this.unitMovesPenalty = null;

	}

	public void setClassesAndPenalty(String[] classesNames, int[] classesLength, String[][] classesValues, double[][] unitMovesPenalty) {
		this.classesNames = classesNames;
		this.classesLength = classesLength;
		this.classesValues = classesValues;
		this.unitMovesPenalty = unitMovesPenalty;
	}

	/**
	 * Computes the move penalty of a combinatorial move as the average of the move penalty
	 * of each of the unit moves that form the combinatorial move.
	 *
	 * @param combinatorialMove
	 * @return
	 */
	public double computeCombinatorialMovePenalty(int[] combinatorialMove){

		if(this.unitMovesPenalty == null){
			return -1.0;
		}
		// If unitMovesPenalty is not null we assume that the penalty value will be specified for each unit move of each class.

		double penaltySum = 0.0;

		for(int i = 0; i < combinatorialMove.length; i++){
			penaltySum += this.unitMovesPenalty[i][combinatorialMove[i]];
		}

		return penaltySum/combinatorialMove.length;
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

		if(this.unitMovesPenalty != null){
			String unitMovesPenaltyString = "[ ";

			for(int i = 0; i < this.unitMovesPenalty.length; i++){

				unitMovesPenaltyString += this.unitMovesPenalty[i] + " ";

			}

			unitMovesPenaltyString += "]";

			params += indentation + "UNIT_MOVES_PENALTY = " + unitMovesPenaltyString;
		}else{
			params += indentation + "UNIT_MOVES_PENALTY = null";
		}

		return params;

	}

	public abstract int[][] selectNextCombinations();

	public abstract int[][] getBestCombinations();

	public abstract int getNumIndependentCombinatorialProblems();

	public abstract void updateStatistics(int[] rewards);

	public abstract void logStats();

    /**
     * Ths method keeps factor*oldStatistic statistics. Factor should be in the interval [0,1].
     *
     * @param factor
     */
    public abstract void decreaseStatistics(double factor);

	public boolean isTuningAllRoles(){
		return this.tuneAllRoles;
	}

}
