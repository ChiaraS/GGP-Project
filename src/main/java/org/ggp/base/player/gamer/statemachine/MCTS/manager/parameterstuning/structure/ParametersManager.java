package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parametersorders.ParametersOrder;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

public class ParametersManager extends SearchManagerComponent {

	/**
	 * List of the parameters that we are tuning.
	 * They also specify their name, possible values, (optional) penalty, etc...
	 */
	private List<TunableParameter> tunableParameters;

	/**
	 * String representation of the boolean expression that specifies the constraints
	 * that a combinations of parameters values must satisfy to be feasible.
	 */
	private String valuesConstraints;

	/**
	 * This ParametersOrder is used to order the parameters right after the creation of a new player
	 * and before such player starts playing any game.
	 */
	private ParametersOrder initialParametersOrder;

	public ParametersManager(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.tunableParameters = null;

		this.valuesConstraints = gamerSettings.getPropertyValue("ParametersManager.valuesConstraints");

		try {
			this.initialParametersOrder = (ParametersOrder) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.PARAMETERS_ORDER.getConcreteClasses(),
					gamerSettings.getPropertyValue("ParametersManager.initialParametersOrderType"))).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating ParametersOrder " + gamerSettings.getPropertyValue("ParametersManager.initialParametersOrderType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.tunableParameters = sharedReferencesCollector.getTheParametersToTune();

		if(this.tunableParameters == null || this.tunableParameters.size() == 0){
			GamerLogger.logError("SearchManagerCreation", "TunerBeforeSimulation - Initialization with null or empty list of tunable parameters!");
			throw new RuntimeException("ParametersTuner - Initialization with null or empty list of tunable parameters!");
		}

		this.initialParametersOrder.imposeOrder(this.tunableParameters);
	}

	@Override
	public void clearComponent() {
		this.initialParametersOrder.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.initialParametersOrder.setUpComponent();
	}

	/**
	 * For the parameter at position paramIndex in the list of tunable parameters returns
	 * an array where each entry is true if the corresponding value is feasible for the
	 * given parameter and false otherwise. Feasibility of a value depends on the values
	 * set so far for the other parameters.
	 *
	 * @param paramIndex index of the parameter for which we want to know which values are
	 * feasible.
	 * @param otherParamsValueIndices indices of the values that have been set for the other
	 * parameters (if no value has been set yet, the index will be -1). The order of these
	 * indices must be the same as the order if the tunableParameters (i.e. at position x
	 * in otherParamsValueIndices we must find the index of the value set for the parameter
	 * in position x in tunableParameters).
	 * @return an array where an entry is true if the corresponding value for the parameter
	 * is feasible given the values set for other parameters, false otherwise.
	 */
	public boolean[] getValuesFeasibility(int paramIndex, int[] otherParamsValueIndices){

		if(otherParamsValueIndices[paramIndex] != -1){
			GamerLogger.logError("ParametersTuner", "ParametersManager - Asking feasible values for parameter " + this.tunableParameters.get(paramIndex).getName() +
					" that is already set to the value " + this.tunableParameters.get(paramIndex).getPossibleValues()[otherParamsValueIndices[paramIndex]] + ".");
			throw new RuntimeException("ParametersManager - Asking feasible values for parameter that is already set!");
		}

		boolean[] feasibility = new boolean[this.tunableParameters.get(paramIndex).getPossibleValues().length];

		for(int i = 0; i < feasibility.length; i++){
			otherParamsValueIndices[paramIndex] = i;
			feasibility[i] = this.isValid(otherParamsValueIndices);
		}

		// Restore unset value for the parameter
		otherParamsValueIndices[paramIndex] = -1;

		return feasibility;

	}

	public boolean isValid(int[] partialCombination){
		return true; // TODO: add check!
	}

	/**
	 * Returns the name of the parameter at position paramIndex in the list of tunableParameters.
	 *
	 * @param paramIndex
	 * @return
	 */
	public String getName(int paramIndex){
		return this.tunableParameters.get(paramIndex).getName();
	}

	/**
	 * Returns the number of parameters being tuned.
	 *
	 * @return
	 */
	public int getNumTunableParameters(){
		return this.tunableParameters.size();
	}

	/**
	 * Returns the number of possible values of the parameter at position paramIndex in
	 * the list of tunableParameters.
	 *
	 * @param paramIndex
	 * @return
	 */
	public int getNumPossibleValues(int paramIndex){
		return this.tunableParameters.get(paramIndex).getPossibleValues().length;
	}


	/**
	 * Returns the possible values of the parameter at position paramIndex in the list of
	 * tunableParameters. Note that the return type is a string because we only need the
	 * values for logging purposes and otherwise we would need to know if such values
	 * are of type int or double.
	 *
	 * @param paramIndex
	 * @return
	 */
	public String[] getPossibleValues(int paramIndex){
		return this.tunableParameters.get(paramIndex).getPossibleValues();
	}

	/**
	 *  Get for each parameter the number of possible values. This in needed to create
	 */
	// a local MAB for each parameter.

	public int[] getNumPossibleValuesForAllParams(){
		int[] numPossibleValues = new int[this.getNumTunableParameters()];
		for(int paramIndex = 0; paramIndex < numPossibleValues.length; paramIndex++){
			numPossibleValues[paramIndex] = this.getNumPossibleValues(paramIndex);
		}
		return numPossibleValues;
	}

	/**
	 * Returns the penalty associated with the possible values of the parameter at position
	 * paramIndex in the list of tunableParameters.
	 *
	 * @param paramIndex
	 * @return
	 */
	public double[] getPossibleValuesPenalty(int paramIndex){
		return this.tunableParameters.get(paramIndex).getPossibleValuesPenalty();
	}

	public void setParametersValues(int[][] valuesIndicesPerRole){

		int paramIndex = 0;

		// If we are tuning only for my role...
		if(valuesIndicesPerRole.length == 1){
			for(TunableParameter p : this.tunableParameters){
				p.setMyRoleNewValue(this.gameDependentParameters.getMyRoleIndex(), valuesIndicesPerRole[0][paramIndex]);
				paramIndex++;
			}
		}else{ //If we are tuning for all roles...

			int[] newValuesIndices;

			for(TunableParameter p : this.tunableParameters){

				//System.out.print(c.getClass().getSimpleName() + ": [ ");

				newValuesIndices = new int[valuesIndicesPerRole.length]; // valuesIndicesPerRole.length equals the number of roles for which we are tuning

				for(int roleIndex = 0; roleIndex < newValuesIndices.length; roleIndex++){
					newValuesIndices[roleIndex] = valuesIndicesPerRole[roleIndex][paramIndex];
					//System.out.print(newValuesIndices[roleIndex] + " ");
				}

				//System.out.println("]");

				p.setAllRolesNewValues(newValuesIndices);

				paramIndex++;
			}
		}
	}

	@Override
	public String getComponentParameters(String indentation) {
		// TODO Auto-generated method stub
		return null;
	}

}
