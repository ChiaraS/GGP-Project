package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
import org.ggp.base.util.logging.GamerLogger;

public class ParametersManager extends SearchManagerComponent {

	/**
	 * List of the parameters that we are tuning.
	 * They also specify their name, possible values, (optional) penalty, etc...
	 */
	protected List<TunableParameter> tunableParameters;

	public ParametersManager(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.tunableParameters = sharedReferencesCollector.getTheParametersToTune();
	}

	@Override
	public void clearComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getComponentParameters(String indentation) {
		// TODO Auto-generated method stub
		return null;
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
	 * Returns the penalty associated with the possible values of the parameter at position
	 * paramIndex in the list of tunableParameters.
	 *
	 * @param paramIndex
	 * @return
	 */
	public double[] getPossibleValuesPenalty(int paramIndex){
		return this.tunableParameters.get(paramIndex).getPossibleValuesPenalty();
	}

}
