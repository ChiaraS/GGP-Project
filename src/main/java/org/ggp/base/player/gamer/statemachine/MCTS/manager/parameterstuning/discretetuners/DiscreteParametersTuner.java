package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.discretetuners;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.DiscreteParametersManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ParametersManager;
import org.ggp.base.util.logging.GamerLogger;

public abstract class DiscreteParametersTuner extends ParametersTuner {

	protected DiscreteParametersManager discreteParametersManager;

	protected List<CombinatorialCompactMove> allCombinations;

	public DiscreteParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.discreteParametersManager = new DiscreteParametersManager(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		sharedReferencesCollector.setDiscreteParametersManager(this.discreteParametersManager);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector){
		super.setReferences(sharedReferencesCollector);
		this.discreteParametersManager.setReferences(sharedReferencesCollector);
		this.allCombinations = this.discreteParametersManager.getAllLegalParametersCombinations();
	}

	@Override
	public void clearComponent() {
		super.clearComponent();
		this.discreteParametersManager.clearComponent();
	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();
		this.discreteParametersManager.setUpComponent();
	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "DISCRETE_PARAMETERS_MANAGER = " + this.discreteParametersManager.printComponent(indentation + "  ") +
				indentation + "NUM_COMBINATORIAL_MOVES = " + (this.allCombinations != null ? this.allCombinations.size() : 0);

		String superParams = super.getComponentParameters(indentation);

		if(superParams != null){
			return  params + superParams;
		}else{
			return params;
		}

	}

	protected String getLogOfCombinations(int[][] combinations){

		String globalParamsOrder = this.getGlobalParamsOrder();
		String toLog = "";

		if(this.tuneAllRoles){
			for(int roleProblemIndex = 0; roleProblemIndex < this.gameDependentParameters.getNumRoles(); roleProblemIndex++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleProblemIndex)) + ";PARAMS=;" + globalParamsOrder + ";SELECTED_COMBINATION=;[ ");
				if(combinations != null && combinations[roleProblemIndex] != null){
					for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){
						toLog += this.discreteParametersManager.getPossibleValues(paramIndex)[combinations[roleProblemIndex][paramIndex]] + " ";
					}
				}else{
					for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){
						toLog += null + " ";
					}
				}
				toLog += "];\n";
			}
		}else{ // Tuning only my role
			toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(this.gameDependentParameters.getMyRoleIndex())) + ";PARAMS=;" + globalParamsOrder + ";SELECTED_COMBINATION=;[ ");
			if(combinations != null && combinations[0] != null){
				for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){
					toLog += this.discreteParametersManager.getPossibleValues(paramIndex)[combinations[0][paramIndex]] + " ";
				}
			}else{
				for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){
					toLog += null + " ";
				}
			}
			toLog += "];\n";
		}

		return toLog;
	}

	@Override
	public ParametersManager getParametersManager() {
		return this.discreteParametersManager;
	}

	/**
	 * Sets parameter values for the tuned roles, randomizing the values of all tuned parameters for the other roles
	 * if only my role is being tuned.
	 *
	 * @param paramValues
	 */
	protected void setParametersValues(int[][] paramValues) {

		// If all roles are being tuned or if only my role is tuned but I don't want to model the opponent randomly,
		// set the parameter values as they are.
		if(this.tuneAllRoles || !this.randomOpponents) {
			this.discreteParametersManager.setParametersValues(paramValues);
		}else { // Otherwise set the parameters for all non-tuned roles to random values.

			// If we are tuning only my role, but the given parameter values are longer than 1 there is something wrong.
			if(paramValues.length != 1) {
				GamerLogger.logError("ParametersTuner", "DiscreteParametersTuner - Trying to randomize opponents' parameters when tuning " + paramValues.length + " roles instead of 1 (i.e. my role)!");
				throw new RuntimeException("DiscreteParametersTuner - Trying to randomize opponents' parameters when tuning " + paramValues.length + " roles instead of 1 (i.e. my role)!");
			}

			int[][] paramsForAllRoles = new int[this.gameDependentParameters.getNumRoles()][this.discreteParametersManager.getNumTunableParameters()];

			int nextComboIndex;

			for(int roleIndex = 0; roleIndex < paramsForAllRoles.length; roleIndex++) {
				if(roleIndex == this.gameDependentParameters.getMyRoleIndex()) {
					paramsForAllRoles[roleIndex] = paramValues[0];
				}else {
					nextComboIndex = this.random.nextInt(this.allCombinations.size());
					paramsForAllRoles[roleIndex] = this.allCombinations.get(nextComboIndex).getIndices();
				}
			}
			this.discreteParametersManager.setParametersValues(paramsForAllRoles);
		}

	}

	/**
	 * Sets the best parameter values for the tuned roles, resetting to the default value all tuned parameters for the other roles
	 * if only my role is being tuned.
	 *
	 * @param paramValues
	 */
	protected void setBestParametersValues(int[][] paramValues) {

		// If all roles are being tuned or if only my role is tuned but I don't want to model the opponent randomly,
		// set the parameter values as they are.
		if(this.tuneAllRoles || !this.randomOpponents) {
			this.discreteParametersManager.setParametersValues(paramValues);
		}else { // Otherwise set the parameters for all non-tuned roles to random values.

			// If we are tuning only my role, but the given parameter values are longer than 1 there is something wrong.
			if(paramValues.length != 1) {
				GamerLogger.logError("ParametersTuner", "DiscreteParametersTuner - Trying to reset opponents' parameters when tuning " + paramValues.length + " roles instead of 1 (i.e. my role)!");
				throw new RuntimeException("DiscreteParametersTuner - Trying to reset opponents' parameters when tuning " + paramValues.length + " roles instead of 1 (i.e. my role)!");
			}

			this.discreteParametersManager.resetOpponentsParametersToDefaultValues();
			this.discreteParametersManager.setParametersValues(paramValues);
		}

	}

	/**
	 * Sets values of a single parameter for the tuned roles, randomizing the value of ALL the tunable parameters
	 * for the other roles if only my role is being tuned. Note that we randomize ALL tunable parameters for other
	 * roles even if we are tuning one parameter at a time for our role. This choice has been made such that tuning
	 * one parameter for my role still takes into account all possible combinations of parameters for the opponent,
	 * increasing the number of possible opponent models that the tuning takes into account.
	 *
	 * @param paramValues
	 */
	protected void setSingleParameterValues(int[] paramValues, int paramIndex) {

		// If all roles are being tuned or if only my role is tuned but I don't want to model the opponent randomly,
		// set the parameter values as they are.
		if(this.tuneAllRoles || !this.randomOpponents) {
			this.discreteParametersManager.setSingleParameterValues(paramValues, paramIndex);
		}else { // Otherwise set the parameters for all non-tuned roles to random values.

			// If we are tuning only my role, but the given parameter values are longer than 1 there is something wrong.
			if(paramValues.length != 1) {
				GamerLogger.logError("ParametersTuner", "DiscreteParametersTuner - Trying to randomize opponents' parameters when tuning " + paramValues.length + " roles instead of 1 (i.e. my role)!");
				throw new RuntimeException("DiscreteParametersTuner - Trying to randomize opponents' parameters when tuning " + paramValues.length + " roles instead of 1 (i.e. my role)!");
			}

			int[][] paramsForAllRoles = new int[this.gameDependentParameters.getNumRoles()][this.discreteParametersManager.getNumTunableParameters()];

			int nextComboIndex;

			for(int roleIndex = 0; roleIndex < paramsForAllRoles.length; roleIndex++) {
				if(roleIndex == this.gameDependentParameters.getMyRoleIndex()) {
					paramsForAllRoles[roleIndex] = this.discreteParametersManager.getCurrentParamValueIndicesForRole(roleIndex);
					paramsForAllRoles[roleIndex][paramIndex] = paramValues[0];
				}else {
					nextComboIndex = this.random.nextInt(this.allCombinations.size());
					paramsForAllRoles[roleIndex] = this.allCombinations.get(nextComboIndex).getIndices();
				}
			}
			this.discreteParametersManager.setParametersValues(paramsForAllRoles);
		}

	}

	/**
	 * Sets the best value of a single parameter for the tuned roles, resetting to the default value ALL the tunable
	 * parameters for the other roles if only my role is being tuned.
	 *
	 * @param paramValues
	 */
	protected void setBestSingleParameterValues(int[] paramValues, int paramIndex) {

		// If all roles are being tuned or if only my role is tuned but I don't want to model the opponent randomly,
		// set the parameter values as they are.
		if(this.tuneAllRoles || !this.randomOpponents) {
			this.discreteParametersManager.setSingleParameterValues(paramValues, paramIndex);
		}else { // Otherwise set the parameters for all non-tuned roles to random values.

			// If we are tuning only my role, but the given parameter values are longer than 1 there is something wrong.
			if(paramValues.length != 1) {
				GamerLogger.logError("ParametersTuner", "DiscreteParametersTuner - Trying to reset opponents' parameters when tuning " + paramValues.length + " roles instead of 1 (i.e. my role)!");
				throw new RuntimeException("DiscreteParametersTuner - Trying to reset opponents' parameters when tuning " + paramValues.length + " roles instead of 1 (i.e. my role)!");
			}

			this.discreteParametersManager.resetOpponentsParametersToDefaultValues();
			this.discreteParametersManager.setSingleParameterValues(paramValues, paramIndex);

		}

	}

}
