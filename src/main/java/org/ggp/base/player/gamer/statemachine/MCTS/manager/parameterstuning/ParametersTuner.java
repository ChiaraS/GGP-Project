package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ParametersManager;

public abstract class ParametersTuner extends SearchManagerComponent{

	protected ParametersManager parametersManager;

	/**
	 * True if the tuner is still being used to tune parameters, false otherwise.
	 * This parameter is needed when we only have a limited simulations budget to
	 * tune parameters. In this case we need to know when we are done tuning (i.e.
	 * the tuning budget expired) so that the AfterSimulationStrategy will also
	 * stop updating the statistics of the parameters.
	 */
	protected boolean tuning;

	protected boolean tuneAllRoles;

	/**
	 * True if the tuner must memorize the best combinations found after the end of the first game
	 * that has been won and then re-use them for all subsequent games.
	 */
	protected boolean reuseBestCombos;

	protected boolean reuseStats;

	/**
	 * Names of the classes being considered, i.e. names of the parameters being tuned.
	 */
	//protected String[] classesNames;

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
	//protected int[] classesLength;

	/**
	 * Needed only for logging. Given this values we could avoid keeping as parameter the classesLenght,
	 * because the length of each class can be deduced from here.
	 */
	//protected String[][] classesValues;

	/**
	 * For each class, for each unit move in the class, this array specifies the penalty.
	 * NOTE: the penalty values must be specified for either all or none of the classes,
	 * otherwise an exception must be thrown.
	 * If there is no penalty specified in the gamers settings for any of the classes,
	 * then this pointer will be null.
	 */
	//protected double[][] unitMovesPenalty;

	public ParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.parametersManager = new ParametersManager(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.tuning = true;

		this.tuneAllRoles = gamerSettings.getBooleanPropertyValue("ParametersTuner.tuneAllRoles");

		this.reuseBestCombos = gamerSettings.getBooleanPropertyValue("ParametersTuner.reuseBestCombos");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector){
		this.parametersManager.setReferences(sharedReferencesCollector);
	}

	/*
	public ParametersTuner(ParametersTuner toCopy) {
		super(toCopy);

		this.tuneAllRoles = toCopy.isTuningAllRoles();

		this.classesNames = null;

		this.classesLength = null;

		this.classesValues = null;

		this.unitMovesPenalty = null;

	}*/

	/*
	public void setClassesAndPenalty(String[] classesNames, int[] classesLength, String[][] classesValues, double[][] unitMovesPenalty) {
		this.classesNames = classesNames;
		this.classesLength = classesLength;
		this.classesValues = classesValues;
		this.unitMovesPenalty = unitMovesPenalty;
	}
	*/

	/**
	 * Computes the move penalty of a combinatorial move as the average of the move penalty
	 * of each of the unit moves that form the combinatorial move.
	 * If a unit move has no penalty specified, the value of 0 will be considered in the average.
	 * Note that the actual use of the moves penalty depends on the bias computer. Even if specified,
	 * the moves penalties might not be used if no BiasComputer is present in the TunerSelector.
	 *
	 * @param combinatorialMove
	 * @return
	 */
	public double computeCombinatorialMovePenalty(int[] combinatorialMove){

		double penaltySum = 0.0;

		for(int paramIndex = 0; paramIndex < combinatorialMove.length; paramIndex++){
			if(this.parametersManager.getPossibleValuesPenalty(paramIndex) != null){
				penaltySum += this.parametersManager.getPossibleValuesPenalty(paramIndex)[combinatorialMove[paramIndex]];
			}
		}

		return penaltySum/combinatorialMove.length;
	}

	@Override
	public void clearComponent() {
		this.parametersManager.clearComponent();
		this.tuning = false;
	}

	@Override
	public void setUpComponent() {
		this.parametersManager.setUpComponent();
		this.tuning = true;
	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "PARAMETERS_MANAGER = " + this.parametersManager.printComponent(indentation + "  ") +
				indentation + "TUNE_ALL_ROLES = " + this.tuneAllRoles +
				indentation + "REUSE_BEST_COMBOS = " + this.reuseBestCombos +
				indentation + "REUSE_STATS = " + this.reuseStats +
				indentation + "tuning = " + this.tuning;

		/*
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
		*/

		return params;

	}

	public abstract void setNextCombinations();

	public abstract void setBestCombinations();

	public abstract int getNumIndependentCombinatorialProblems();

	public abstract void updateStatistics(int[] goals);

	public abstract void logStats();

    /**
     * This method keeps factor*oldStatistic statistics. Factor should be in the interval [0,1].
     *
     * @param factor
     */
    public abstract void decreaseStatistics(double factor);

	public boolean isTuningAllRoles(){
		return this.tuneAllRoles;
	}

	protected void stopTuning(){
		this.tuning = false;
	}

	public boolean isTuning(){
		return this.tuning;
	}

	public abstract boolean isMemorizingBestCombo();

	public abstract void memorizeBestCombinations();

	protected String getGlobalParamsOrder(){
		String globalParamsOrder = "[ ";
		for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
			globalParamsOrder += (this.parametersManager.getName(paramIndex) + " ");
		}
		globalParamsOrder += "]";

		return globalParamsOrder;
	}

}
