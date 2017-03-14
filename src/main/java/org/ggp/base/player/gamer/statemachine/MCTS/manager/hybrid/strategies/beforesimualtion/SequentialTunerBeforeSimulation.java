package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion;

import java.util.Collections;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class SequentialTunerBeforeSimulation extends TunerBeforeSimulation {

	/**
	 * Index of the current parameter being tuned in the list of parameters.
	 * If it's -1 it means we need to set an order on the parameters (the order in
	 * which they will be tuned) and set this index to 0 (i.e. the index of the 1st
	 * parameter to tune).
	 *
	 * In other words, when this parameter is set to -1 it means we never tuned any
	 * parameter or we just finished one repetition of tuning all parameters.
	 */
	private int currentParameterIndex;

	public SequentialTunerBeforeSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.currentParameterIndex = 0;

		sharedReferencesCollector.setSequentialTunerBeforeSimulation(this);

	}

	@Override
	public void clearComponent(){

		super.clearComponent();

		this.currentParameterIndex = 0;
	}

	@Override
	public void setUpComponent(){

		// Prepare the tuning for the first parameter

		// NOTE!!!: the first time we start tuning the first parameter, for all the other parameters that
		// we are not tuning we use the fixed value specified in the gamers settings, thus that value is
		// relevant for the sequential tuner, while it wasn't for the simultaneous tuner.

		// First we shuffle the order of the parameters
		Collections.shuffle(this.tunableParameters);

		this.currentParameterIndex = 0;

		// Set the length and the values penalty (if any) of the class of values for the next parameter to tune
		String[] classesNames = new String[1];
		int[] classesLength = new int[1];
		String[][] possibleValues = new String[1][];
		double[][] unitMovesPenalty = null;
		classesNames[0] = this.tunableParameters.get(this.currentParameterIndex).getName();
		classesLength[0] = this.tunableParameters.get(this.currentParameterIndex).getPossibleValuesLength();
		possibleValues[0] = this.tunableParameters.get(this.currentParameterIndex).getPossibleValues();
		if(this.tunableParameters.get(this.currentParameterIndex).getPossibleValuesPenalty() != null &&
				this.tunableParameters.get(this.currentParameterIndex).getPossibleValuesPenalty().length > 0){
			unitMovesPenalty = new double[1][];
			unitMovesPenalty[0] = this.tunableParameters.get(this.currentParameterIndex).getPossibleValuesPenalty();
			// NOTE that for the sequential tuner we are not checking if the penalty is specified for all or none of the
			// parameters to be tuned. It is possible that some parameters will have a specified penalty and some others
			// won't. If this happens it will cause problems, so TODO: add check to this class!
		}
		this.parametersTuner.setClassesAndPenalty(classesNames, classesLength, possibleValues, unitMovesPenalty);

		// We call it here so the parameters tuner will be directly set up with the new value for the classes length.
		// Otherwise it will be set up with the old value and we will have to re-set it here wasting time.
		super.setUpComponent();

	}


	@Override
	public void beforeSimulationActions() {

		if(this.simCountForBatch == 0){

			// Get for each role the next value to test for the parameter being tuned and set it.
			int[][] nextCombinations = this.parametersTuner.selectNextCombinations();

			this.setNewValuesForParameter(nextCombinations);
		}

		this.simCountForBatch = (this.simCountForBatch + 1)%this.batchSize;

	}

	public void startTuningNextParameter(){

		//this.parametersTuner.logStats();

		// Get the best value for the last parameter being tuned and set it.
		int[][] bestValuePerRole = this.parametersTuner.getBestCombinations();

		this.setNewValuesForParameter(bestValuePerRole);

		// Advance the index of the parameter to tune. If we were tuning the last parameter in the sequence,
		// reset the index to 0.
		this.currentParameterIndex = (this.currentParameterIndex+1)%this.tunableParameters.size();

		// Clear the tuner
		this.parametersTuner.clearComponent();

		// If we finished tuning all parameters for the last tuning round (so the index of the next parameter to tune is 0 again)
		// we can change the order in which we tune the parameters here (for now it's only possible to set it random).
		if(this.currentParameterIndex == 0){
			Collections.shuffle(this.tunableParameters);
		}

		// Set the length and the values penalty (if any) of the class of values for the next parameter to tune
		String[] classesNames = new String[1];
		int[] classesLength = new int[1];
		String[][] possibleValues = new String[1][];
		double[][] unitMovesPenalty = null;
		classesNames[0] = this.tunableParameters.get(this.currentParameterIndex).getName();
		classesLength[0] = this.tunableParameters.get(this.currentParameterIndex).getPossibleValuesLength();
		possibleValues[0] = this.tunableParameters.get(this.currentParameterIndex).getPossibleValues();
		if(this.tunableParameters.get(this.currentParameterIndex).getPossibleValuesPenalty() != null &&
				this.tunableParameters.get(this.currentParameterIndex).getPossibleValuesPenalty().length > 0){
			unitMovesPenalty = new double[1][];
			unitMovesPenalty[0] = this.tunableParameters.get(this.currentParameterIndex).getPossibleValuesPenalty();
			// NOTE that for the sequential tuner we are not checking if the penalty is specified for all or none of the
			// parameters to be tuned. It is possible that some parameters will have a specified penalty and some others
			// won't. If this happens it will cause problems, so TODO: add check to this class!
		}
		this.parametersTuner.setClassesAndPenalty(classesNames, classesLength, possibleValues, unitMovesPenalty);

		// Set up parameters tuner to tune next parameter
		this.parametersTuner.setUpComponent();

		// Reset also the counter of the simulations performed for the current batch.
		// Every time we start tuning a new parameter we also start recomputing the number
		// of simulations of the current batch used to know when to change the value that
		// the current batch of simulations is evaluating.
		this.simCountForBatch = 0;

	}

	private void setNewValuesForParameter(int[][] newValuesPerRole){

		// If we are tuning only for my role...
		if(newValuesPerRole.length == 1){
			this.tunableParameters.get(this.currentParameterIndex).setMyRoleNewValue(this.gameDependentParameters.getMyRoleIndex(), newValuesPerRole[0][0]);
		}else{ //If we are tuning for all roles...
			int[] newValuesIndices = new int[newValuesPerRole.length]; // bestValuePerRole.length equals the number of roles for which we are tuning

			for(int i = 0; i < newValuesIndices.length; i++){
				newValuesIndices[i] = newValuesPerRole[i][0];
			}

			this.tunableParameters.get(this.currentParameterIndex).setAllRolesNewValues(newValuesIndices);
		}
	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "currentParameterIndex = " + this.currentParameterIndex;

		if(superParams != null){
			return superParams + params;
		}

		return params;

	}

}
