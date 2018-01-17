package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.DiscreteTunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;

public class DiscreteParametersManager extends ParametersManager {

	/**
	 * List of the parameters that we are tuning.
	 * They also specify their name, possible values, (optional) penalty, etc...
	 */
	private List<DiscreteTunableParameter> tunableParameters;



	public DiscreteParametersManager(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.tunableParameters = null;

	}


	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		super.setReferences(sharedReferencesCollector);

		this.tunableParameters = sharedReferencesCollector.getTheDiscreteParametersToTune();

		if(this.tunableParameters == null || this.tunableParameters.size() == 0){
			GamerLogger.logError("SearchManagerCreation", "ParametersManager - Initialization with null or empty list of tunable parameters!");
			throw new RuntimeException("ParametersManager - Initialization with null or empty list of tunable parameters!");
		}

		this.initialParametersOrder.imposeOrder(this.tunableParameters);


		this.indexOfK = -1;
		this.indexOfRef = -1;

		int i = 0;
		for(TunableParameter t : this.tunableParameters){
			if(t.getName().equals("K")){
				this.indexOfK = i;
			}else if(t.getName().equals("Ref")){
				this.indexOfRef = i;
			}
			i++;
		}
	}

	@Override
	public void clearComponent() {
		super.clearComponent();
	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();
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

		boolean[] feasibility = new boolean[this.tunableParameters.get(paramIndex).getNumPossibleValues()];

		for(int i = 0; i < feasibility.length; i++){
			otherParamsValueIndices[paramIndex] = i;
			feasibility[i] = this.isValid(otherParamsValueIndices);
		}

		// Restore unset value for the parameter
		otherParamsValueIndices[paramIndex] = -1;

		return feasibility;

	}

	/**
	 * For the parameter at position paramIndex in the list of tunable parameters, given a set of
	 * indices of possible values for the parameter, returns the subset of indices of possible
	 * values that are feasible, given the values set so far for the other parameters
	 * (otherParamsValueIndices).
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
	public Set<Move> getValuesFeasibility(int paramIndex, Set<Move> valuesIndices, int[] otherParamsValueIndices){

		if(otherParamsValueIndices[paramIndex] != -1){
			GamerLogger.logError("ParametersTuner", "ParametersManager - Asking feasible values for parameter " + this.tunableParameters.get(paramIndex).getName() +
					" that is already set to the value " + this.tunableParameters.get(paramIndex).getPossibleValues()[otherParamsValueIndices[paramIndex]] + ".");
			throw new RuntimeException("ParametersManager - Asking feasible values for parameter that is already set!");
		}

		Set<Move> feasibility = new HashSet<Move>();

		for(Move m : valuesIndices){
			if(! (m instanceof CombinatorialCompactMove)) {
				GamerLogger.logError("ParametersTuner", "ParametersManager - Asking feasible values for parameter " + this.tunableParameters.get(paramIndex).getName() +
						", but cannot retrieve the index of the value because the Move is not of type CombinatorialCompactMove.");
				throw new RuntimeException("ParametersManager - Asking feasible values for parameter " + this.tunableParameters.get(paramIndex).getName() +
						", but cannot retrieve the index of the value because the Move is not of type CombinatorialCompactMove.");
			}
			int[] indices = ((CombinatorialCompactMove) m).getIndices();
			// Note that we are expecting a single index for the parameter value
			if(indices.length != 1) {
				GamerLogger.logError("ParametersTuner", "ParametersManager - Asking feasible values for parameter " + this.tunableParameters.get(paramIndex).getName() +
						", but there are " + indices.length + " indices for the value of the considered parameter instead of a single index.");
				throw new RuntimeException("ParametersManager - Asking feasible values for parameter " + this.tunableParameters.get(paramIndex).getName() +
						", but there are " + indices.length + " indices for the value of the considered parameter instead of a single index.");
			}
			otherParamsValueIndices[paramIndex] = indices[0];
			if(this.isValid(otherParamsValueIndices)){
				feasibility.add(m);
			}
		}

		// Restore unset value for the parameter
		otherParamsValueIndices[paramIndex] = -1;

		return feasibility;

	}

	/**
	 * For the parameter at position paramIndex in the list of tunable parameters returns
	 * a list with the indices of all values that are feasible for the parameter given the
	 * values set so far for the other parameters.
	 *
	 * @param paramIndex index of the parameter for which we want to know which values are
	 * feasible.
	 * @param otherParamsValueIndices indices of the values that have been set for the other
	 * parameters (if no value has been set yet, the index will be -1). The order of these
	 * indices must be the same as the order if the tunableParameters (i.e. at position x
	 * in otherParamsValueIndices we must find the index of the value set for the parameter
	 * in position x in tunableParameters).
	 * @return a list with the indices of all values that are feasible for the parameter
	 * given the values set so far for the other parameters.
	 */
	public List<Integer> getFeasibleValues(int paramIndex, int[] otherParamsValueIndices){

		if(otherParamsValueIndices[paramIndex] != -1){
			GamerLogger.logError("ParametersTuner", "ParametersManager - Asking feasible values for parameter " + this.tunableParameters.get(paramIndex).getName() +
					" that is already set to the value " + this.tunableParameters.get(paramIndex).getPossibleValues()[otherParamsValueIndices[paramIndex]] + ".");
			throw new RuntimeException("ParametersManager - Asking feasible values for parameter that is already set!");
		}

		List<Integer> feasibleValues = new ArrayList<Integer>();

		for(int i = 0; i < this.tunableParameters.get(paramIndex).getNumPossibleValues(); i++){
			otherParamsValueIndices[paramIndex] = i;
			if(this.isValid(otherParamsValueIndices)){
				feasibleValues.add(new Integer(i));
			}
		}

		// Restore unset value for the parameter
		otherParamsValueIndices[paramIndex] = -1;

		return feasibleValues;

	}

	/* JSEval
	public boolean isValid(int[] partialCombination){

		if(this.valuesConstraintsScript != null){
			ScriptEngine engine = this.valuesConstraintsScript.getEngine();
			for(int paramIndex = 0; paramIndex < this.tunableParameters.size(); paramIndex++){
				engine.put("indexOf"+this.tunableParameters.get(paramIndex).getName(), partialCombination[paramIndex]);
				if(partialCombination[paramIndex] != -1){
					engine.put(this.tunableParameters.get(paramIndex).getName(), this.tunableParameters.get(paramIndex).getPossibleValue(partialCombination[paramIndex]));
				}else{
					engine.getBindings(ScriptContext.ENGINE_SCOPE).remove(this.tunableParameters.get(paramIndex).getName());
				}
			}
			try {
				return (boolean) this.valuesConstraintsScript.eval();
			} catch (ScriptException e) {
				GamerLogger.logError("ParametersTuner", "ParametersManager - Error when evaluating validity of combinations of values with indices " +
						Arrays.toString(partialCombination) + ".");
				throw new RuntimeException("ParametersManager - Error when evaluating validity of combinations of values!");
			}
		}else{
			return true;
		}
	}
	*/

	public boolean isValid(int[] partialCombination){
		// If we are not tuning at least one among the parameters K and Ref any combination is valid.
		if(this.indexOfK == -1 || this.indexOfRef == -1){
			return true;
		}
		// At least one among the values of the parameters K and Ref has not been set yet.
		if(partialCombination[this.indexOfK] == -1 || partialCombination[this.indexOfRef] == -1){
			return true;
		}
		// We are tuning both K and Ref and a value has already been set in the partialCombination
		// for both of them --> we must check if the combination is feasible or not.
		double valueForK = this.tunableParameters.get(this.indexOfK).getPossibleValue(partialCombination[this.indexOfK]);
		double valueForRef = this.tunableParameters.get(this.indexOfRef).getPossibleValue(partialCombination[this.indexOfRef]);
		return ((valueForK == 0.0 && valueForRef == -1.0) || (valueForK != 0.0 && valueForRef != -1.0));
	}

	/**
	 * Returns the name of the parameter at position paramIndex in the list of tunableParameters.
	 *
	 * @param paramIndex
	 * @return
	 */
	@Override
	public String getName(int paramIndex){
		return this.tunableParameters.get(paramIndex).getName();
	}

	/**
	 * Returns the number of parameters being tuned.
	 *
	 * @return
	 */
	@Override
	public int getNumTunableParameters(){
		return this.tunableParameters.size();
	}

	public int[] getCurrentValuesIndicesForParam(int paramIndex){
		return this.tunableParameters.get(paramIndex).getCurrentValuesIndices();
	}

	/**
	 * Returns the number of possible values of the parameter at position paramIndex in
	 * the list of tunableParameters.
	 *
	 * @param paramIndex
	 * @return
	 */
	public int getNumPossibleValues(int paramIndex){
		return this.tunableParameters.get(paramIndex).getNumPossibleValues();
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
	 *  a local MAB for each parameter.
	 */

	public int[] getNumPossibleValuesForAllParams(){
		int[] numPossibleValues = new int[this.getNumTunableParameters()];
		for(int paramIndex = 0; paramIndex < numPossibleValues.length; paramIndex++){
			numPossibleValues[paramIndex] = this.getNumPossibleValues(paramIndex);
		}
		return numPossibleValues;
	}

	public int getTotalNumPossibleValues(){
		int totNumPossibleValues = 0;
		for(int paramIndex = 0; paramIndex < this.getNumTunableParameters(); paramIndex++){
			totNumPossibleValues += this.getNumPossibleValues(paramIndex);
		}
		return totNumPossibleValues;
	}

	/**
	 * Returns the penalty associated with the possible values of the parameter at position
	 * paramIndex in the list of tunableParameters.
	 *
	 * @param paramIndex
	 * @return the penalty associated with the possible values of the parameter at position
	 * paramIndex in the list of tunableParameters if specified, null otherwise.
	 */
	public double[] getPossibleValuesPenalty(int paramIndex){
		return this.tunableParameters.get(paramIndex).getPossibleValuesPenalty();
	}

	public void setParametersValues(int[][] valuesIndicesPerRole){

		int paramIndex = 0;

		// If we are tuning only for my role...
		if(valuesIndicesPerRole.length == 1){
			for(DiscreteTunableParameter p : this.tunableParameters){
				p.setMyRoleNewValue(this.gameDependentParameters.getMyRoleIndex(), valuesIndicesPerRole[0][paramIndex]);
				paramIndex++;
			}
		}else{ //If we are tuning for all roles...

			int[] newValuesIndices;

			for(DiscreteTunableParameter p : this.tunableParameters){

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

	public void setSingleParameterValues(int[] valuesIndicesPerRole, int paramIndex){
		// If we are tuning only for my role...
		if(valuesIndicesPerRole.length == 1){
			this.tunableParameters.get(paramIndex).setMyRoleNewValue(this.gameDependentParameters.getMyRoleIndex(),
					valuesIndicesPerRole[0]);
		}else{ //If we are tuning for all roles...

			this.tunableParameters.get(paramIndex).setAllRolesNewValues(valuesIndicesPerRole);
		}
	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = "";

		if(this.tunableParameters != null){

			String tunableParametersString = "[ ";

			for(TunableParameter p : this.tunableParameters){

				tunableParametersString += indentation + "  TUNABLE_PARAMETER = " + p.getParameters(indentation + "  ");

			}

			tunableParametersString += "\n]";

			params += indentation + "TUNABLE_PARAMETERS = " + tunableParametersString;
		}else{
			params += indentation + "TUNABLE_PARAMETERS = null";
		}

		if(superParams != null){
			return  params + superParams;
		}else{
			return params;
		}

	}

	public List<CombinatorialCompactMove> getAllLegalParametersCombinations(){
		List<CombinatorialCompactMove> combinatorialMoves = new ArrayList<CombinatorialCompactMove>();
		int[] partialCombo = new int[this.tunableParameters.size()];
		for(int i = 0; i < partialCombo.length; i++){
			partialCombo[i] = -1;
		}
		this.crossProduct(0, partialCombo, combinatorialMoves);

		return combinatorialMoves;
	}

    private void crossProduct(int paramIndex, int[] partialCombo, List<CombinatorialCompactMove> combinatorialMoves){

		//System.out.println("ParamIndex = " + paramIndex + ", Combo = " + Arrays.toString(partialCombo));

        if (paramIndex == this.getNumTunableParameters()) {
        	//System.out.println("Adding");
        	combinatorialMoves.add(new CombinatorialCompactMove(this.copyArray(partialCombo)));
            //System.out.println("Returning");
        } else {
        	boolean atLeastOneFeasibleCombo = false;
            for(int i = 0; i < this.getNumPossibleValues(paramIndex); i++) {

            	//System.out.println(i);

            	//System.out.println("Possibe values = " + this.parametersManager.getNumPossibleValues(paramIndex));
            	partialCombo[paramIndex] = i;
            	if(this.isValid(partialCombo)){
            		atLeastOneFeasibleCombo = true;
            		//System.out.println("PRE - ParamIndex = " + paramIndex + ", Combo = " + Arrays.toString(partialCombo));
            		this.crossProduct(paramIndex+1, partialCombo, combinatorialMoves);
            		//System.out.println("POST - ParamIndex = " + paramIndex + ", Combo = " + Arrays.toString(partialCombo));
            	}
            	//System.out.println("PREPRE - ParamIndex = " + paramIndex + ", Combo = " + Arrays.toString(partialCombo));
            	partialCombo[paramIndex] = -1;
            	//System.out.println("POSTPOST - ParamIndex = " + paramIndex + ", Combo = " + Arrays.toString(partialCombo));
            }
            if(!atLeastOneFeasibleCombo){
            	String partialComboString = "[ ";
            	for(int j = 0; j < paramIndex; j++){
            		partialComboString += this.getName(j) + "=" + this.getPossibleValues(j)[partialCombo[j]];
            	}
            	partialComboString += "]";
            	GamerLogger.logError("ParametersTuner", "ParametersManager - No valid value detected for parameter " +
            			this.getName(paramIndex) + "for the following partial combination: " + partialComboString + "!");
				throw new RuntimeException("ParametersManager - No valid value detected for a parameter when computing all combinatorial moves!");
            }
        }
    }

    private int[] copyArray(int[] array){
    	int[] newArray = new int[array.length];
    	for(int i = 0; i < array.length; i++){
    		newArray[i] = array[i];
    	}
    	return newArray;
    }

	/**
	 * Computes the move penalty of a combinatorial move (combination of parameter values) as the
	 * average of the move penalty of each of the unit moves (parameter values) that form the
	 * combinatorial move.
	 * If a unit move has no penalty specified, the value of 0 will be considered in the average.
	 * Note that the actual use of the moves penalty depends on the bias computer. Even if specified,
	 * the moves penalties might not be used if no BiasComputer is present in the TunerSelector.
	 *
	 * @param parameterValuesIndices
	 * @return
	 */
	public double computeCombinatorialMovePenalty(int[] parameterValuesIndices){

		double penaltySum = 0.0;

		for(int paramIndex = 0; paramIndex < parameterValuesIndices.length; paramIndex++){
			if(this.getPossibleValuesPenalty(paramIndex) != null){
				penaltySum += this.getPossibleValuesPenalty(paramIndex)[parameterValuesIndices[paramIndex]];
			}
		}

		return penaltySum/parameterValuesIndices.length;
	}

}
