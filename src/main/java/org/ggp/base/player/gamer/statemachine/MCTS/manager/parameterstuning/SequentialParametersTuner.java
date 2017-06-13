package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.SequentialProblemRepresentation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.FixedMab;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

public class SequentialParametersTuner extends ParametersTuner {

	/**
	 * If true, after all the parameters have been tuned once sequentially,
	 * this tuner will randomize their order before tuning them all again sequentially.
	 * If false, they will be tuned sequentially repeatedly always in the same order.
	 */
	private boolean shuffleTuningOrder;

	/**
	 * If in the interval [0.0, 1.0], indicates that the statistics of each
	 * parameter must be decayed after each tuning iteration of each parameter.
	 * Its value indicates which percentage of the statistics will be kept.
	 * (decayFactor*statValue) statistics will be kept for each parameter value.
	 * ATTENTION! This class needs to be responsible of decaying the statistics
	 * because it might be necessary to decay them during the search for a move
	 * and not at the end of the search for every move. When using the sequential
	 * tuner don't set the AfterMoveStrategy to decay the statistics. This tuner
	 * already takes care of that at the right moment.
	 */
	private double decayFactor;

	/**
	 * Selects the next value to evaluate for the currently tuned parameter.
	 */
	private TunerSelector nextValueSelector;

	/**
	 * Selects the best value to set for the currently tuned parameter.
	 */
	private TunerSelector bestValueSelector;

	/**
	 * Number of samples that should be taken for the current parameter being tuned.
	 * After maxSamplesPerParam samples the tuner will start tuning the next parameter.
	 * When maxSamplesPerParam=Integer.MAX_VALUE it means that this tuner should never
	 * change the tuned parameter on its own. The parameter should be change at the end
	 * of the search for every game step, and it's the AfterMoveStrategy that takes care
	 * of signaling when to change.
	 */
	private int maxSamplesPerParam;

	/**
	 * Number of samples taken so far for the parameter currently being tuned.
	 * This value is always reset to 0 whenever we change the parameter being tuned.
	 */
	private int currentNumSamples;

	/**
	 * Indices of the parameters in the order in which they should be tuned.
	 */
	private List<Integer> tuningOrder;

	/**
	 * Index that keeps track of which parameter is being tuned in the tuning order.
	 * E.g.: if the tuning order is [ 2 3 0 1 ], and orderIndex=1 the parameter being
	 * tuned at the moment is 3. If we must start tuning the next parameter, then we
	 * will switch to parameter 0.
	 */
	private int orderIndex;

	private SequentialProblemRepresentation[] roleProblems;

	private int[][] selectedCombinations;

	private int[][] bestCombinations;

	/**
	 * For each role, memorize an array with value feasibility for the currently tuned
	 * parameter wrt the values set for the other parameters of the role.
	 * For each role, the values feasibility for the parameter being tuned is always the
	 * same during all the time the parameter is being tuned, because it depends on the
	 * values of the other parameters that do not change. Computing the feasibility is a
	 * somewhat expensive operation, so it is better to compute it only once when we start
	 * tuning a new parameter and memorize it here until we start tuning the next parameter.
	 */
	private boolean[][] valuesFeasibility;

	public SequentialParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.shuffleTuningOrder = gamerSettings.getBooleanPropertyValue("ParametersTuner.shuffleTuningOrder");

		this.decayFactor = gamerSettings.getDoublePropertyValue("ParametersTuner.decayFactor");

		String[] tunerSelectorDetails = gamerSettings.getIDPropertyValue("ParametersTuner.nextValueSelectorType");

		try {
			this.nextValueSelector = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("ParametersTuner.nextValueSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		tunerSelectorDetails = gamerSettings.getIDPropertyValue("ParametersTuner.bestValueSelectorType");

		try {
			this.bestValueSelector = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("ParametersTuner.bestValueSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		this.tuningOrder = null;

		this.orderIndex = -1;

		this.roleProblems = null;

		this.selectedCombinations = null;

		this.bestCombinations = null;

		this.maxSamplesPerParam = gamerSettings.getIntPropertyValue("ParametersTuner.maxSamplesPerParam");

		this.currentNumSamples = 0;

		this.valuesFeasibility = null;

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);

		super.setReferences(sharedReferencesCollector);

		this.nextValueSelector.setReferences(sharedReferencesCollector);

		this.bestValueSelector.setReferences(sharedReferencesCollector);

		this.tuningOrder = new ArrayList<Integer>();

		for(int i = 0; i < this.parametersManager.getNumTunableParameters(); i++){
			this.tuningOrder.add(new Integer(i));
		}

	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();
		this.nextValueSelector.setUpComponent();
		this.bestValueSelector.setUpComponent();

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		// We need to use the role problems if:
		// 1. We are not going to reuse the best combo of previous games
		// 2. We are going to reuse the best combo of previous games, but that has not been computed yet
		// 3. We are going to reuse the best combo of previous games, it has been computed, but its size
		// doesn't correspond to the number of roles that we have to tune.
		// (TODO: here we should check that we reuse the best combo ONLY if we are playing the exact same game
		// and not just a game with the same number of roles).
		if(!this.reuseBestCombos || this.bestCombinations == null || this.bestCombinations.length != numRolesToTune){

			// If we need to use the role problems, here we have to check if we need new ones or if we should
			// reuse previous ones that have been saved.
			// We need new ones if:
			// 1. We don't want to reuse the previous ones
			// 2. We want to reuse the previous ones, but we have none yet
			// 3. We want to reuse the previous ones, we have them but their size doesn't correspond to the number
			// of roles that we have to tune.
			// (TODO: here we should check that we reuse role problems ONLY if we are playing the exact same game
			// and not just a game with the same number of roles).
			if(!this.reuseStats || this.roleProblems == null || this.roleProblems.length != numRolesToTune){
				// Create the representation of the combinatorial problem for each role
				this.roleProblems = new SequentialProblemRepresentation[numRolesToTune];
				for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
					roleProblems[roleProblemIndex] = new SequentialProblemRepresentation(this.parametersManager.getNumPossibleValuesForAllParams());
				}

				this.selectedCombinations = new int[numRolesToTune][this.parametersManager.getNumTunableParameters()];

				// Initialize selectedCombinations with the currently set values for the parameters.
				// This is needed because when we start tuning we will only tune one parameter and we need the others
				// to be set to the correct values to compute the feasibility for the values of the tuned parameter.
				// For each parameter, we retrieve the indices of the currently set values for each role and set them.
				int[] currentValuesIndices;
				for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){

					currentValuesIndices = this.parametersManager.getCurrentValuesIndicesForParam(paramIndex);

					if(this.tuneAllRoles){
						for(int roleProblemIndex = 0; roleProblemIndex < this.selectedCombinations.length; roleProblemIndex++){
							this.selectedCombinations[roleProblemIndex][paramIndex] = currentValuesIndices[roleProblemIndex];
						}
					}else{ // Tuning only my role
						this.selectedCombinations[0][paramIndex] = currentValuesIndices[this.gameDependentParameters.getMyRoleIndex()];
					}

				}

				// TODO: is it ok to re-shuffle order and reset orderIndex and currentNumSamples only when
				// we don't re-use the statistics? In this way, when re-using the statistics the tuning starts
				// exactly where it was left (i.e. from last tuned parameter, taking only the remaining samples
				// if we are changing parameter after every maxSamplesPerParam samples. Or from the next parameter to
				// tune in the tuningOrder, if we are changing parameter after every move).
				// Should we just keep the stats, but re-start tuning from the 1st parameter in the order taking
				// all the predefined samples?
				if(this.shuffleTuningOrder){
					Collections.shuffle(this.tuningOrder);
				}

				this.orderIndex = 0;
				this.currentNumSamples = 0;

				this.valuesFeasibility = new boolean[numRolesToTune][];
				this.computeValuesFeasibilityForCurrentParam();
			}

			this.bestCombinations = null;
		}else{
			this.roleProblems = null;
			this.selectedCombinations = null;
			this.tuningOrder = null;
			this.orderIndex = -1;
			this.currentNumSamples = 0;
			this.valuesFeasibility = null;
		}

	}

	@Override
	public void clearComponent() {
		super.clearComponent();
		this.nextValueSelector.clearComponent();
		this.bestValueSelector.clearComponent();

		if(!this.reuseStats){
			this.roleProblems = null;
			this.selectedCombinations = null;
		}
	}

	private void computeValuesFeasibilityForCurrentParam(){

		// Get index of parameter currently tuned
		int paramIndex = this.tuningOrder.get(this.orderIndex);

		// Variable used to temporarily memorize the previously set index of the parameter
		// while the feasibility is being computed.
		int memIndex;

		// For each role, we compute the feasible values for the currently tuned parameter
		// wrt the values set for the other parameters for the role
		for(int roleIndex = 0; roleIndex < this.selectedCombinations.length; roleIndex++){

			memIndex = this.selectedCombinations[roleIndex][paramIndex];

			// Set to (momentarily) "undefined" the value of the parameter that we are currently tuning for the considered role
			this.selectedCombinations[roleIndex][paramIndex] = -1;

			// NOTE that we can pass directly this.selectedCombinations[roleIndex][paramIndex] to the getValuesFeasibility() function
			// because the function guarantees that at the end of its execution this.selectedCombinations[roleIndex][paramIndex] will
			// have the same values that it had when passed to the function.
			this.valuesFeasibility[roleIndex] = this.parametersManager.getValuesFeasibility(paramIndex, this.selectedCombinations[roleIndex]);

			this.selectedCombinations[roleIndex][paramIndex] = memIndex;

		}
	}

	@Override
	public void setNextCombinations() {

		// Get index of parameter currently tuned
		int paramIndex = this.tuningOrder.get(this.orderIndex);

		FixedMab currentParamMabPerRole;

		int[] newValuesOfParameter = new int[this.roleProblems.length];

		// For each role, we check the corresponding problem and select a value for the currently tuned parameter
		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			currentParamMabPerRole = this.roleProblems[roleProblemIndex].getLocalMabs()[paramIndex];

			newValuesOfParameter[roleProblemIndex] = this.nextValueSelector.selectMove(currentParamMabPerRole.getMoveStats(),
					this.valuesFeasibility[roleProblemIndex],
					// If for a parameter no penalties are specified, a penalty of 0 is assumed for all of the values.
					(this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex) : new double[this.parametersManager.getNumPossibleValues(paramIndex)]),
					currentParamMabPerRole.getNumUpdates());

			this.selectedCombinations[roleProblemIndex][paramIndex] = newValuesOfParameter[roleProblemIndex];
		}

		this.parametersManager.setSingleParameterValues(newValuesOfParameter, paramIndex);

	}

	public void changeTunedParameter(){

		// Before changing, set for each role the best value for the currently tuned parameter
		this.setBestValuesForCurrentParameter();

		// If we were tuning the last parameter in the order we must restart from the 1st parameter
		if(this.orderIndex == this.tuningOrder.size()-1){

			this.orderIndex = 0;

			// Check if we should randomize the tuning order of the parameters or keep the same
			if(this.shuffleTuningOrder){
				Collections.shuffle(this.tuningOrder);
			}

			// Check if we must decay statistics of parameter between different tuning iterations
			if(this.decayFactor < 1.0 && this.decayFactor <= 0.0){
				this.decreaseStatistics(this.decayFactor);
			}

		}else{
			// If none of the above, we just increase the orderIndex to point to the next parameter in the tuning order
			this.orderIndex++;
		}

		// Also reset the currentNumSamples.
		this.currentNumSamples = 0;

		this.computeValuesFeasibilityForCurrentParam();

	}

	@Override
	public void setBestCombinations() {

		if(this.isMemorizingBestCombo()){
			// Log the combination that we are selecting as best
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(this.bestCombinations));

			this.parametersManager.setParametersValues(this.bestCombinations);

		/*}else if(this.singleBest){
			this.setSingleBestCombination();*/
		}else{

			// Note that we need to set the best value only for the currently tuned parameter
			// because all other parameters were already set to their best value last time we
			// stopped tuning them.
			this.setBestValuesForCurrentParameter();

			// Log the combination that we are selecting as best
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(this.selectedCombinations));
		}

		this.stopTuning();

	}

	private void setBestValuesForCurrentParameter() {

		// Get index of parameter currently tuned
		int paramIndex = this.tuningOrder.get(this.orderIndex);

		FixedMab currentParamMabPerRole;

		int[] newValuesOfParameter = new int[this.roleProblems.length];

		// For each role, we check the corresponding problem and select a value for the currently tuned parameter
		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			currentParamMabPerRole = this.roleProblems[roleProblemIndex].getLocalMabs()[paramIndex];

			newValuesOfParameter[roleProblemIndex] = this.bestValueSelector.selectMove(currentParamMabPerRole.getMoveStats(),
					this.valuesFeasibility[roleProblemIndex],
					// If for a parameter no penalties are specified, a penalty of 0 is assumed for all of the values.
					(this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex) : new double[this.parametersManager.getNumPossibleValues(paramIndex)]),
					currentParamMabPerRole.getNumUpdates());

			this.selectedCombinations[roleProblemIndex][paramIndex] = newValuesOfParameter[roleProblemIndex];
		}

		this.parametersManager.setSingleParameterValues(newValuesOfParameter, paramIndex);

	}

	private String getLogOfCombinations(int[][] combinations){

		String globalParamsOrder = this.getGlobalParamsOrder();
		String toLog = "";

		if(this.tuneAllRoles){
			for(int roleProblemIndex = 0; roleProblemIndex < this.gameDependentParameters.getNumRoles(); roleProblemIndex++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleProblemIndex)) + ";PARAMS=;" + globalParamsOrder + ";SELECTED_COMBINATION=;[ ");
				if(combinations != null && combinations[roleProblemIndex] != null){
					for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
						toLog += this.parametersManager.getPossibleValues(paramIndex)[combinations[roleProblemIndex][paramIndex]] + " ";
					}
				}else{
					for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
						toLog += null + " ";
					}
				}
				toLog += "];\n";
			}
		}else{ // Tuning only my role
			toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(this.gameDependentParameters.getMyRoleIndex())) + ";PARAMS=;" + globalParamsOrder + ";SELECTED_COMBINATION=;[ ");
			if(combinations != null && combinations[0] != null){
				for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
					toLog += this.parametersManager.getPossibleValues(paramIndex)[combinations[0][paramIndex]] + " ";
				}
			}else{
				for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
					toLog += null + " ";
				}
			}
			toLog += "];\n";
		}

		return toLog;
	}

	@Override
	public void updateStatistics(int[] goals) {

		int[] neededRewards;

		// We have to check if the ParametersTuner is tuning parameters only for the playing role
		// or for all roles and update the statistics with appropriate rewards.
		if(this.tuneAllRoles){
			neededRewards = goals;
		}else{
			neededRewards = new int[1];
			neededRewards[0] = goals[this.gameDependentParameters.getMyRoleIndex()];

		}

		if(neededRewards.length != this.roleProblems.length){
			GamerLogger.logError("ParametersTuner", "SequentialParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + neededRewards.length +
					") to update the role problems (" + this.roleProblems.length + ").");
			throw new RuntimeException("SequentialParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}

		// Get index of parameter currently tuned
		int paramIndex = this.tuningOrder.get(this.orderIndex);

		FixedMab currentParamMabPerRole;

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
			// Update the stats for the MAB of the tuned parameter
			currentParamMabPerRole = this.roleProblems[roleProblemIndex].getLocalMabs()[paramIndex];

			MoveStats stats = currentParamMabPerRole.getMoveStats()[this.selectedCombinations[roleProblemIndex][paramIndex]];
			// Update the stats
			stats.incrementScoreSum(neededRewards[roleProblemIndex]);
			stats.incrementVisits();
			currentParamMabPerRole.incrementNumUpdates();
		}

		this.currentNumSamples++;

		// If maxSamplesPerParam==Integer.MAX_VALUE it means we are tuning one parameter per move, so we don't
		// need to check how many samples have been taken so far to know if we need to start tuning the next
		// parameter. After performing a move in the real game, this class will be told to start tuning the next
		// parameter by the AfterMoveStrategy.
		if(this.maxSamplesPerParam < Integer.MAX_VALUE && this.currentNumSamples == this.maxSamplesPerParam){
			this.changeTunedParameter();
		}

	}

	@Override
	public void logStats() {
		if(this.roleProblems != null){

			// TODO: If the tuner was still tuning, log the most visited combo?

			//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "");
			String toLog;

			for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

				int roleIndex;
				if(this.tuneAllRoles){
					roleIndex = roleProblemIndex;
				}else{
					roleIndex = this.gameDependentParameters.getMyRoleIndex();
				}

				toLog = "";

				FixedMab[] localMabs = this.roleProblems[roleProblemIndex].getLocalMabs();

				for(int paramIndex = 0; paramIndex < localMabs.length; paramIndex++){

					for(int paramValueIndex = 0; paramValueIndex < localMabs[paramIndex].getMoveStats().length; paramValueIndex++){
						//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "ROLE=;" + i + ";MAB=;LOCAL" + j + ";UNIT_MOVE=;" + k + ";VISITS=;" + localMabs[j].getMoveStats()[k].getVisits() + ";SCORE_SUM=;" + localMabs[j].getMoveStats()[k].getScoreSum() + ";AVG_VALUE=;" + (localMabs[j].getMoveStats()[k].getVisits() <= 0 ? "0" : (localMabs[j].getMoveStats()[k].getScoreSum()/((double)localMabs[j].getMoveStats()[k].getVisits()))));
						toLog += "\nROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) + ";PARAM=;" + this.parametersManager.getName(paramIndex) + ";UNIT_MOVE=;" + this.parametersManager.getPossibleValues(paramIndex)[paramValueIndex] + ";PENALTY=;" + (this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex)[paramValueIndex] : 0) + ";VISITS=;" + localMabs[paramIndex].getMoveStats()[paramValueIndex].getVisits() + ";SCORE_SUM=;" + localMabs[paramIndex].getMoveStats()[paramValueIndex].getScoreSum() + ";AVG_VALUE=;" + (localMabs[paramIndex].getMoveStats()[paramValueIndex].getVisits() <= 0 ? "0" : (localMabs[paramIndex].getMoveStats()[paramValueIndex].getScoreSum()/((double)localMabs[paramIndex].getMoveStats()[paramValueIndex].getVisits()))) + ";";
					}
				}

				toLog += "\n";

				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "LocalParamTunerStats", toLog);

			}
		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "SHUFFLE_TUNING_ORDER = " + this.shuffleTuningOrder +
				indentation + "DECAY_FACTOR = " + this.decayFactor +
				indentation + "NEXT_VALUE_SELECTOR = " + this.nextValueSelector.printComponent(indentation + "  ") +
				indentation + "BEST_VALUE_SELECTOR = " + this.bestValueSelector.printComponent(indentation + "  ") +
				indentation + "MAX_SAMPLES_PER_PARAM = " + this.maxSamplesPerParam +
				indentation + "current_num_samples = " + this.currentNumSamples;

		if(this.tuningOrder != null){
			String tuningOrderString = "[ ";

			for(Integer i : this.tuningOrder){

				tuningOrderString += (i.toString() + " ");

			}

			tuningOrderString += "]";

			params += indentation + "tuning_order = " + tuningOrderString;
		}else{
			params += indentation + "tuning_order = null";
		}

		params += indentation + "order_index = " + this.orderIndex +
				indentation + "num_roles_problems = " + (this.roleProblems != null ? this.roleProblems.length : 0);

		if(this.selectedCombinations != null){
			String selectedCombinationsString = "[ ";

			for(int i = 0; i < this.selectedCombinations.length; i++){

				String selectedCombinationString = "[ ";
				for(int j = 0; j < this.selectedCombinations[i].length; j++){
					selectedCombinationString += this.selectedCombinations[i][j] + " ";
				}
				selectedCombinationString += "]";

				selectedCombinationsString += selectedCombinationString + " ";

			}

			selectedCombinationsString += "]";

			params += indentation + "selected_combinations_indices = " + selectedCombinationsString;
		}else{
			params += indentation + "selected_combinations_indices = null";
		}

		if(this.bestCombinations != null){
			String bestCombinationsString = "[ ";

			for(int i = 0; i < this.bestCombinations.length; i++){

				String bestCombinationString = "[ ";
				for(int j = 0; j < this.bestCombinations[i].length; j++){
					bestCombinationString += this.bestCombinations[i][j] + " ";
				}
				bestCombinationString += "]";

				bestCombinationsString += bestCombinationString + " ";

			}

			bestCombinationsString += "]";

			params += indentation + "best_combinations_indices = " + bestCombinationsString;
		}else{
			params += indentation + "best_combinations_indices = null";
		}

		if(this.valuesFeasibility != null){
			String valuesFeasibilityString = "[ ";

			for(int i = 0; i < this.valuesFeasibility.length; i++){

				String valuesFeasibilityPerRoleString = "[ ";
				for(int j = 0; j < this.valuesFeasibility[i].length; j++){
					valuesFeasibilityPerRoleString += this.valuesFeasibility[i][j] + " ";
				}
				valuesFeasibilityPerRoleString += "]";

				valuesFeasibilityString += valuesFeasibilityPerRoleString + " ";

			}

			valuesFeasibilityString += "]";

			params += indentation + "values_feasibility = " + valuesFeasibilityString;
		}else{
			params += indentation + "values_feasibility = null";
		}



		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

	@Override
	public void decreaseStatistics(double factor) {
		for(int i = 0; i < this.roleProblems.length; i++){
			this.roleProblems[i].decreaseStatistics(factor);
		}
	}
	@Override
	public boolean isMemorizingBestCombo() {
		return (this.reuseBestCombos && this.roleProblems == null);
	}

	@Override
	public void memorizeBestCombinations() {
		this.bestCombinations = this.selectedCombinations;
	}

}
