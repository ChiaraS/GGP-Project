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

	/********************************** ATTENTION! **********************************
	 * The use of JavaScript to evaluate ANY possible boolean expression that models
	 * constraints on the feasible values for the combinations of parameters is too
	 * slow! The use of JavaScript allows to specify in the configurations file any
	 * boolean expression on the parameters values, keeping generality of the player,
	 * but the evaluation slows down the performance too much (the speed is halved in
	 * breakthrough and becomes 1/4 in tic-tac-toe). The code that allows to use the
	 * JavaScript test of the value is commented out specifying the tag "JSEval". To
	 * restore the use of JavaScript remove all the comments with this tag and comment
	 * conflicting methods.
	 *
	 * To solve the mentioned problem the constraints on the parameters are just hard-
	 * coded in the method isValid() of this class. These constraints are specified for
	 * the parameters K and Ref. If such parameters are being tuned then the constraints
	 * will be checked, otherwise any combination will be considered valid. If more
	 * parameters are added to the tunable parameters, make sure to include the check
	 * of any constraint on their value in the isValid() method of this class.
	 *
	 * Also note that whenever tuning both Ref and K the possible values for the Ref
	 * parameter must be extended to include the value -1 (i.e. don't set any value for
	 * the parameter because it won't influence the search). However, remember that when
	 * the Ref parameter is being tuned but K isn't, this value must be removed by the
	 * set of possible values!!!
	 */

	/**
	 * String representation of the boolean expression that specifies the constraints
	 * that a combinations of parameters values must satisfy to be feasible.
	 *
	 * NOTES on the format of the constraints:
	 * - the constraints must be specified as a valid JavaScript boolean expression.
	 * - the only feasible variables that can be used in the expression are the names of
	 *   the parameters being tuned (e.g. K, Ref, C,... - upper and lower case matter!)
	 *   and the variables of the form indexOf[parameterName] (e.g. indexOfK, indexOfRef,...).
	 *   When using the name of a parameter in the expression it means that its value will be
	 *   considered at the moment of evaluating the expression. For example if the boolean
	 *   expression is (K == 0) and is evaluated for the configuration [K, Ref, C]=[0, 100, 0.8]
	 *   the it will return true because (0 == 0).
	 *   When using indexOf[parameterName] in the expression it means that the index of the value
	 *   (in the list of possible values) that is currently set for the parameter will be considered.
	 *   Specifying indexOf[parameterName] in the constraints it's mostly useful to check if in
	 *   the considered combination of parameters a given parameter has already been set to a value
	 *   or not. If a parameter X has not been set then indexOfX==-1.
	 *   For example the following expression:
	 *   ((indexOfK==-1)||(indexOfRef==-1)||((K==0)&&(Ref==-1))||((K!=0)&&(Ref!=-1)))
	 *   will accept all combinations of values where K has not been set yet or where Ref has not been
	 *   set yet. If both K and Ref have been set, then it will accept all combinations that have both
	 *   K==0 and Ref==-1 or K!=0 and Ref!=-1.
	 */
	/* JSEval
	private String valuesConstraints;
	*/

	/**
	 * Compiled version of the valuesConstraints seen as a JavaScript script.
	 */
	/* JSEval
	private CompiledScript valuesConstraintsScript;
	*/

	/**
	 * This ParametersOrder is used to order the parameters right after the creation of a new player
	 * and before such player starts playing any game.
	 */
	private ParametersOrder initialParametersOrder;

	/**
	 * Keep track of the indices of the parameters for which we have to check the value to
	 * know if certain combinations of values are feasible.
	 */
	private int indexOfK;
	private int indexOfRef;

	public ParametersManager(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.tunableParameters = null;

		/* JSEval
		if(gamerSettings.specifiesProperty("ParametersManager.valuesConstraints")){
			this.valuesConstraints = gamerSettings.getPropertyValue("ParametersManager.valuesConstraints");
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");

			try {
				this.valuesConstraintsScript = ((Compilable)engine).compile(this.valuesConstraints);
			} catch (ScriptException e) {
				GamerLogger.logError("SearchManagerCreation", "Error when creating parameters values constraints " + gamerSettings.getPropertyValue("ParametersManager.valuesConstraints") + ".");
				GamerLogger.logStackTrace("SearchManagerCreation", e);
				throw new RuntimeException(e);
			}
		}
		*/

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
			GamerLogger.logError("SearchManagerCreation", "ParametersManager - Initialization with null or empty list of tunable parameters!");
			throw new RuntimeException("ParametersManager - Initialization with null or empty list of tunable parameters!");
		}

		this.initialParametersOrder.imposeOrder(this.tunableParameters);

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

		boolean[] feasibility = new boolean[this.tunableParameters.get(paramIndex).getNumPossibleValues()];

		for(int i = 0; i < feasibility.length; i++){
			otherParamsValueIndices[paramIndex] = i;
			feasibility[i] = this.isValid(otherParamsValueIndices);
		}

		// Restore unset value for the parameter
		otherParamsValueIndices[paramIndex] = -1;

		return feasibility;

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

		params += indentation + "INITIAL_PARAMETERS_ORDER = " + this.initialParametersOrder.printComponent(indentation + "   ");

		params += indentation + "index_of_K = " + this.indexOfK;

		params += indentation + "index_of_Ref = " + this.indexOfRef;

		return params;
	}

}
