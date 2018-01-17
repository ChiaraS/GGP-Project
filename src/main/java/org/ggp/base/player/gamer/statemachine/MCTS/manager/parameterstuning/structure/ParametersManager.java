package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parametersorders.ParametersOrder;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

public abstract class ParametersManager extends SearchManagerComponent {

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
	protected ParametersOrder initialParametersOrder;

	/**
	 * Keep track of the indices of the parameters for which we have to check the value to
	 * know if certain combinations of values are feasible.
	 */
	protected int indexOfK;
	protected int indexOfRef;


	public ParametersManager(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

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
		//Do nothing
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
	 * Returns the name of the parameter at position paramIndex in the list of tunableParameters.
	 *
	 * @param paramIndex
	 * @return
	 */
	public abstract String getName(int paramIndex);

	/**
	 * Returns the number of parameters being tuned.
	 *
	 * @return
	 */
	public abstract int getNumTunableParameters();


	@Override
	public String getComponentParameters(String indentation) {

		String params = "";

		params += indentation + "INITIAL_PARAMETERS_ORDER = " + this.initialParametersOrder.printComponent(indentation + "   ");

		params += indentation + "index_of_K = " + this.indexOfK;

		params += indentation + "index_of_Ref = " + this.indexOfRef;

		return params;

	}

	public int getIndexOfK() {
		return this.indexOfK;
	}


	public int getIndexOfRef() {
		return this.indexOfRef;
	}


}
