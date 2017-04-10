package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.TwoPhaseProblemRepresentation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.FixedMab;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.IncrementalMab;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import csironi.ggp.course.utils.Pair;

public class NaiveParametersTuner extends ParametersTuner {

	private double epsilon0;

	private TunerSelector globalMabSelector;

	private TunerSelector localMabsSelector;

	/**
	 * Given the statistics of each combination, selects the best one among them.
	 */
	private TunerSelector bestCombinationSelector;

	/**
	 * If true, when selecting the best combination of parameters only the one that's
	 * the best over all the roles will be selected.
	 */
	private boolean singleBest;

	/**
	 * If true, when selecting the best combination of parameters the global MAB will be used.
	 * If false, when selecting the best combination, each parameter will be selected from the
	 * corresponding local MAB independently of the other parameters.
	 * NOTE that if the global MAB is never used (i.e. epsilon0 = 1), the value of this variable
	 * will be ignored and the local MABs will be used even if this variable is true.
	 */
	private boolean useGlobalBest;

	private TwoPhaseProblemRepresentation[] roleProblems;

	private int[][] selectedCombinations;

	public NaiveParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.epsilon0 = gamerSettings.getDoublePropertyValue("ParametersTuner.epsilon0");

		String[] tunerSelectorDetails = gamerSettings.getIDPropertyValue("ParametersTuner.globalMabSelectorType");

		try {
			this.globalMabSelector = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("ParametersTuner.globalMabSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		tunerSelectorDetails = gamerSettings.getIDPropertyValue("ParametersTuner.localMabsSelectorType");

		try {
			this.localMabsSelector = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("ParametersTuner.localMabsSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		tunerSelectorDetails = gamerSettings.getIDPropertyValue("ParametersTuner.bestCombinationSelectorType");

		try {
			this.bestCombinationSelector = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("ParametersTuner.bestCombinationSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		this.singleBest = gamerSettings.getBooleanPropertyValue("ParametersTuner.singleBest");

		this.useGlobalBest = gamerSettings.getBooleanPropertyValue("ParametersTuner.useGlobalBest");

		this.roleProblems = null;

		this.selectedCombinations = null;

	}

	/*
	@Override
	public void setClassesAndPenalty(String[] classesNames, int[] classesLength, String[][] classesValues, double[][] unitMovesPenalty) {
		super.setClassesAndPenalty(classesNames, classesLength, classesValues, unitMovesPenalty);

		this.indexOfK = -1;
		this.indexOfRef = -1;

		for(int i = 0; i < this.classesNames.length; i++){
			if(this.classesNames[i].equals("K")){
				this.indexOfK = i;
			}else if(this.classesNames[i].equals("Ref")){
				this.indexOfRef = i;
			}
		}

		if(this.indexOfK == -1 || this.indexOfRef == -1 || this.indexOfK == this.indexOfRef){
			GamerLogger.logError("SearchManagerCreation", "Error when setting the indices of K and Ref: K = " + this.indexOfK + ", ref = " + this.indexOfRef + ".");
		}

	}*/

	/*
	public NaiveParametersTuner(NaiveParametersTuner toCopy) {
		super(toCopy);

		this.epsilon0 = toCopy.getEpsilon0();*/

		/* TODO: ATTENTON! Here we just copy the reference because all tuners use the same selector!
		However, doing so, whenever the methods clearComponent() and setUpComponent() are called  on
		this and all the other copies of this tuner they will be called on the same TunerSelector
		multiple times. Now it's not a problem, since for all TunerSelectors those methods do nothing,
		but if they get changed then consider this issue!!! A solution is to deep-copy also the tuner,
		but it'll require more memory. */
		/* This is how to deep-copy it:
		try {
			this.globalMabSelector = (TunerSelector) SearchManagerComponent.getCopyConstructorForSearchManagerComponent(toCopy.getGlobalMabSelector().getClass()).newInstance(toCopy.getGlobalMabSelector());
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + toCopy.getGlobalMabSelector().getClass().getSimpleName() + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}*//*
		this.globalMabSelector = toCopy.getGlobalMabSelector();
		this.localMabsSelector = toCopy.getLocalMabSelector();
		this.bestCombinationSelector = toCopy.getBestCombinationSelector();
	}*/

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);
		this.globalMabSelector.setReferences(sharedReferencesCollector);
		this.localMabsSelector.setReferences(sharedReferencesCollector);
		this.bestCombinationSelector.setReferences(sharedReferencesCollector);
	}

	@Override
	public void setUpComponent() {

		super.setUpComponent();

		this.globalMabSelector.setUpComponent();

		this.localMabsSelector.setUpComponent();

		this.bestCombinationSelector.setUpComponent();

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		// If we must re-use the previously computed best combos and we have the same number of roles being tuned,
		// don't re-initialize the roleProblems because we won't need them.
		if(!this.reuseBestCombos || this.selectedCombinations == null || this.selectedCombinations.length != numRolesToTune){
			// Create a two phase representation of the combinatorial problem for each role
			this.roleProblems = new TwoPhaseProblemRepresentation[numRolesToTune];
			for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
				roleProblems[roleProblemIndex] = new TwoPhaseProblemRepresentation(this.parametersManager.getNumPossibleValuesForAllParams());
			}

			this.selectedCombinations = new int[numRolesToTune][this.parametersManager.getNumTunableParameters()];
		}

	}

	@Override
	public void clearComponent() {

		super.clearComponent();

		this.globalMabSelector.clearComponent();
		this.localMabsSelector.clearComponent();
		this.bestCombinationSelector.clearComponent();

		this.roleProblems = null;
		// At the end of one game, this parameters memorizes the best combos found during tuning.
		if(!this.reuseBestCombos){
			this.selectedCombinations = null; // Reset only if we don't need them in the next game
		}
	}

	@Override
	public void setNextCombinations() {

		// For each role, we check the corresponding naive problem and select a combination of parameters
		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
			// TODO: the strategy that selects if to use the global or the local mabs is hard-coded.
			// Can be refactored to be customizable.
			if(this.roleProblems[roleProblemIndex].getGlobalMab().getNumUpdates() > 0 &&
					this.random.nextDouble() >= this.epsilon0){// Exploit

				this.selectedCombinations[roleProblemIndex] = this.exploit(this.roleProblems[roleProblemIndex].getGlobalMab());

			}else{ //Explore

				this.selectedCombinations[roleProblemIndex] = this.explore(this.roleProblems[roleProblemIndex].getLocalMabs());

			}
		}

		this.parametersManager.setParametersValues(this.selectedCombinations);

	}

	/**
	 * Given the global MAB, selects one of the combinatorial moves in the MAB according to
	 * the strategy specified for the global MAB.
	 *
	 * @param globalMab
	 * @return
	 */
	private int[] exploit(IncrementalMab globalMab){
		Move m = this.globalMabSelector.selectMove(globalMab.getMovesInfo(), globalMab.getNumUpdates());
		return ((CombinatorialCompactMove) m).getIndices();
	}

	private int[] explore(FixedMab[] localMabs){

		int[] indices = new int[localMabs.length];
		for(int paramIndex = 0; paramIndex < localMabs.length; paramIndex++){
			indices[paramIndex] = -1; // It means that the index has not been set yet
		}

		for(int paramIndex = 0; paramIndex < localMabs.length; paramIndex++){
			indices[paramIndex] = this.localMabsSelector.selectMove(localMabs[paramIndex].getMoveStats(),
					this.parametersManager.getValuesFeasibility(paramIndex, indices),
					// If for a parameter no penalties are specified, a penalty of 0 is assumed for all of the values.
					(this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex) : new double[this.parametersManager.getNumPossibleValues(paramIndex)]),
					localMabs[paramIndex].getNumUpdates());
		}

		/*
		// Select a value for each local mab independently
		if(this.unitMovesPenalty != null){
			for(int i = 0; i < localMabs.length; i++){
				indices[i] = this.localMabsSelector.selectMove(localMabs[i].getMoveStats(), this.unitMovesPenalty[i], localMabs[i].getNumUpdates());
			}
		}else{
			for(int i = 0; i < localMabs.length; i++){
				indices[i] = this.localMabsSelector.selectMove(localMabs[i].getMoveStats(), null, localMabs[i].getNumUpdates());
			}
		}
		*/

		return indices;

	}

	// TODO: put this method in the ParametersTuner and force every tuner to implement
	// the methods setSingleBestCombination() and setMultipleBestCombinations().
	@Override
	public void setBestCombinations() {

		if(this.isMemorizingBestCombo()){
			this.setSelectedBestCombination();
		}else if(this.singleBest){
			this.setSingleBestCombination();
		}else{
			this.setMultipleBestCombinations();
		}

		this.stopTuning();

	}

	/**
	 * Sets a different best combination for each role depending on
	 * the statistics of the role.
	 */
	private void setSelectedBestCombination(){
		// Log the combination that we are selecting as best
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfSelectedCombinations());

		this.parametersManager.setParametersValues(this.selectedCombinations);
	}

	/**
	 * Sets a different best combination for each role depending on
	 * the statistics of the role.
	 */
	private void setMultipleBestCombinations(){
		// For each role, we select a combination of parameters
		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
			// If we want to use the global MAB we can only do that if it has been visited at least once
			// and so contains at least one combination.
			if(this.useGlobalBest && this.roleProblems[roleProblemIndex].getGlobalMab().getNumUpdates() > 0){
				IncrementalMab globalMab = this.roleProblems[roleProblemIndex].getGlobalMab();
				Move m = this.bestCombinationSelector.selectMove(globalMab.getMovesInfo(), globalMab.getNumUpdates());
				this.selectedCombinations[roleProblemIndex] = ((CombinatorialCompactMove) m).getIndices();
			}else{
				FixedMab[] localMabs = this.roleProblems[roleProblemIndex].getLocalMabs();
				int[] indices = new int[localMabs.length];

				// Select a value for each local mab independently
				for(int paramIndex = 0; paramIndex < localMabs.length; paramIndex++){
					indices[paramIndex] = this.bestCombinationSelector.selectMove(localMabs[paramIndex].getMoveStats(),
							this.parametersManager.getValuesFeasibility(paramIndex, indices),
							// If for a parameter no penalties are specified, a penalty of 0 is assumed for all of the values.
							(this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex) : new double[this.parametersManager.getNumPossibleValues(paramIndex)]),
							localMabs[paramIndex].getNumUpdates());
				}
				this.selectedCombinations[roleProblemIndex] = indices;
			}
		}

		// Log the combination that we are selecting as best
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfSelectedCombinations());

		this.parametersManager.setParametersValues(this.selectedCombinations);
	}

	/**
	 * Sets the same best combination for all the roles looking at all
	 * their statistics at the same time.
	 */
	private void setSingleBestCombination(){

		// Check if we want to use the global stats to compute the best configuration
		if(this.useGlobalBest){
			this.setSingleGlobalBestCombination();
		}else{
			this.setSingleLocalBestCombination();
		}

	}

	private void setSingleGlobalBestCombination(){
		int totNumUpdates = 0; // All updates
		List<Map<Move,Pair<MoveStats,Double>>> allGlobalStats = new ArrayList<Map<Move,Pair<MoveStats,Double>>>(); // Maps for each role

		// Aggregate numUpdates and all available MoveStats of all roles
		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
			totNumUpdates += this.roleProblems[roleProblemIndex].getGlobalMab().getNumUpdates();
			allGlobalStats.add(this.roleProblems[roleProblemIndex].getGlobalMab().getMovesInfo());
		}

		// Make sure that at least for one role we have at least one statistic in the global stats
		if(totNumUpdates == 0){
			this.setSingleLocalBestCombination();
			return;
		}

		Pair<Integer,Move> theBestCombo = this.bestCombinationSelector.selectMove(allGlobalStats, totNumUpdates);

		// Set selectedCombinations and prepare the message to log with the combination that has been selected as best.
		String toLog = "";
		ExplicitRole role;
		String globalParamsOrder = this.getGlobalParamsOrder();
		String parametersValues = "";
		String originalRole = "";
		if(this.tuneAllRoles){
			for(int roleProblemIndex = 0; roleProblemIndex < this.selectedCombinations.length; roleProblemIndex++){
				this.selectedCombinations[roleProblemIndex] = ((CombinatorialCompactMove) theBestCombo.getSecond()).getIndices();
				role = this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleProblemIndex));
				parametersValues = "[ ";
				originalRole = "[ ";
				for(int paramIndex = 0; paramIndex < this.selectedCombinations[roleProblemIndex].length; paramIndex++){
					parametersValues += (this.parametersManager.getPossibleValues(paramIndex)[this.selectedCombinations[roleProblemIndex][paramIndex]] + " ");
					if(roleProblemIndex == theBestCombo.getFirst().intValue()){
						originalRole += "T ";
					}else{
						originalRole += "F ";
					}
				}
				parametersValues += "]";
				originalRole += "]";
				toLog += ("ROLE=;" + role + ";PARAMS=;" + globalParamsOrder + ";SELECTED_COMBINATION=;" + parametersValues + ";ORIGINAL_ROLE=;" + originalRole + ";\n");
			}
		}else{ // Tuning only my role
			this.selectedCombinations[0] = ((CombinatorialCompactMove) theBestCombo.getSecond()).getIndices();
			role = this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(this.gameDependentParameters.getMyRoleIndex()));
			parametersValues = "[ ";
			originalRole = "[ ";
			for(int paramIndex = 0; paramIndex < this.selectedCombinations[0].length; paramIndex++){
				parametersValues += (this.parametersManager.getPossibleValues(paramIndex)[this.selectedCombinations[0][paramIndex]] + " ");
				if(0 == theBestCombo.getFirst().intValue()){
					originalRole += "T ";
				}else{
					originalRole += "F ";
				}
			}
			parametersValues += "]";
			originalRole += "]";
			toLog += ("ROLE=;" + role + ";PARAMS=;" + globalParamsOrder + ";SELECTED_COMBINATION=;" + parametersValues + ";ORIGINAL_ROLE=;" + originalRole + ";\n");
		}

		// Log the combination that we are selecting as best
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", toLog);

		// Set
		this.parametersManager.setParametersValues(this.selectedCombinations);
	}

	private void setSingleLocalBestCombination(){

		MoveStats[][] allRolesMoveStats = new MoveStats[this.roleProblems.length][];
		int totalNumUpdates;
		int[] selectedValuesIndices = new int[this.parametersManager.getNumTunableParameters()];
		for(int i = 0; i < selectedValuesIndices.length; i++){
			selectedValuesIndices[i] = -1;
		}
		int[] selectedValuesRolesIndices = new int[this.parametersManager.getNumTunableParameters()];
		Pair<Integer,Integer> result;

		// For each parameter select the best value using stats of ALL roles
		for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
			totalNumUpdates = 0;
			for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
				allRolesMoveStats[roleProblemIndex] = this.roleProblems[roleProblemIndex].getLocalMabs()[paramIndex].getMoveStats();
				totalNumUpdates += this.roleProblems[roleProblemIndex].getLocalMabs()[paramIndex].getNumUpdates();
			}
			result = this.bestCombinationSelector.selectMove(allRolesMoveStats,
					this.parametersManager.getValuesFeasibility(paramIndex, selectedValuesIndices),
					(this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex) : new double[this.parametersManager.getNumPossibleValues(paramIndex)]),
					totalNumUpdates);
			selectedValuesRolesIndices[paramIndex] = result.getFirst();
			selectedValuesIndices[paramIndex] = result.getSecond();
		}

		// Set selectedCombinations and prepare the message to log with the combination that has been selected as best.
		String toLog = "";
		ExplicitRole role;
		String globalParamsOrder = this.getGlobalParamsOrder();
		String parametersValues = "";
		String originalRole = "";
		if(this.tuneAllRoles){
			for(int roleProblemIndex = 0; roleProblemIndex < this.selectedCombinations.length; roleProblemIndex++){
				this.selectedCombinations[roleProblemIndex] = selectedValuesIndices;
				role = this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleProblemIndex));
				parametersValues = "[ ";
				originalRole = "[ ";
				for(int paramIndex = 0; paramIndex < this.selectedCombinations[roleProblemIndex].length; paramIndex++){
					parametersValues += (this.parametersManager.getPossibleValues(paramIndex)[this.selectedCombinations[roleProblemIndex][paramIndex]] + " ");
					if(roleProblemIndex == selectedValuesRolesIndices[paramIndex]){
						originalRole += "T ";
					}else{
						originalRole += "F ";
					}
				}
				parametersValues += "]";
				originalRole += "]";
				toLog += ("ROLE=;" + role + ";PARAMS=;" + globalParamsOrder + ";SELECTED_COMBINATION=;" + parametersValues + ";ORIGINAL_ROLE=;" + originalRole + ";\n");
			}
		}else{ // Tuning only my role
			this.selectedCombinations[0] = selectedValuesIndices;
			role = this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(this.gameDependentParameters.getMyRoleIndex()));
			parametersValues = "[ ";
			originalRole = "[ ";
			for(int paramIndex = 0; paramIndex < this.selectedCombinations[0].length; paramIndex++){
				parametersValues += (this.parametersManager.getPossibleValues(paramIndex)[this.selectedCombinations[0][paramIndex]] + " ");
				if(0 == selectedValuesRolesIndices[paramIndex]){
					originalRole += "T ";
				}else{
					originalRole += "F ";
				}
			}
			parametersValues += "]";
			originalRole += "]";
			toLog += ("ROLE=;" + role + ";PARAMS=;" + globalParamsOrder + ";SELECTED_COMBINATION=;" + parametersValues + ";ORIGINAL_ROLE=;" + originalRole + ";\n");
		}

		// Log the combination that we are selecting as best
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", toLog);

		// Set
		this.parametersManager.setParametersValues(this.selectedCombinations);

	}

	private String getLogOfSelectedCombinations(){
		String globalParamsOrder = this.getGlobalParamsOrder();
		String toLog = "";

		if(this.tuneAllRoles){
			for(int roleProblemIndex = 0; roleProblemIndex < this.selectedCombinations.length; roleProblemIndex++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleProblemIndex)) + ";PARAMS=;" + globalParamsOrder + ";SELECTED_COMBINATION=;[ ");
				for(int paramIndex = 0; paramIndex < this.selectedCombinations[roleProblemIndex].length; paramIndex++){
					toLog += this.parametersManager.getPossibleValues(paramIndex)[this.selectedCombinations[roleProblemIndex][paramIndex]] + " ";
				}
				toLog += "];\n";
			}
		}else{ // Tuning only my role
			toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(this.gameDependentParameters.getMyRoleIndex())) + ";PARAMS=;" + globalParamsOrder + ";SELECTED_COMBINATION=;[ ");
			for(int paramIndex = 0; paramIndex < this.selectedCombinations[0].length; paramIndex++){
				toLog += this.parametersManager.getPossibleValues(paramIndex)[this.selectedCombinations[0][paramIndex]] + " ";
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
			GamerLogger.logError("ParametersTuner", "NaiveParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + neededRewards.length +
					") to update the role problems (" + this.roleProblems.length + ").");
			throw new RuntimeException("NaiveParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			/********** Update global MAB **********/

			/*
			// If K = 0 update statistics of a move where Ref has a default value, so that always the same move is updated.
			if(this.classesValues[this.indexOfK][this.selectedCombinations[i][this.indexOfK]].equals("0")){
				this.selectedCombinations[i][this.indexOfRef] = 0;
			}*/


			// Get the info of the combinatorial move in the global MAB
			CombinatorialCompactMove theMove = new CombinatorialCompactMove(this.selectedCombinations[roleProblemIndex]);


			Pair<MoveStats,Double> globalInfo = this.roleProblems[roleProblemIndex].getGlobalMab().getMovesInfo().get(theMove);

			// If the info doesn't exist, add the move to the MAB, computing the corresponding penalty
			if(globalInfo == null){
				globalInfo = new Pair<MoveStats,Double>(new MoveStats(), this.computeCombinatorialMovePenalty(theMove.getIndices()));
				this.roleProblems[roleProblemIndex].getGlobalMab().getMovesInfo().put(theMove, globalInfo);
			}

			// Update the stats
			globalInfo.getFirst().incrementScoreSum(neededRewards[roleProblemIndex]);
			globalInfo.getFirst().incrementVisits();

			// Increase total num updates
			this.roleProblems[roleProblemIndex].getGlobalMab().incrementNumUpdates();

			/********** Update local MABS **********/

			// Update the stats for each local MAB
			FixedMab[] localMabs = this.roleProblems[roleProblemIndex].getLocalMabs();
			for(int paramIndex = 0; paramIndex < localMabs.length; paramIndex++){

				/*
				// TODO: temporary solution! Fix with something general that works for any parameter and can be set from file.
				if(j == this.indexOfRef){ // Check if we have to avoid updating stats for Ref

					if(this.classesValues[this.indexOfK][this.selectedCombinations[i][this.indexOfK]].equals("0")){
						continue; // Don't update the MAB of Ref if K=0;
					}

				}*/

				MoveStats localStats = localMabs[paramIndex].getMoveStats()[this.selectedCombinations[roleProblemIndex][paramIndex]];
				// Update the stats
				localStats.incrementScoreSum(neededRewards[roleProblemIndex]);
				localStats.incrementVisits();

				localMabs[paramIndex].incrementNumUpdates();
			}
		}
	}

	@Override
	public void logStats() {

		if(this.roleProblems != null){

			// TODO: If the tuner was still tuning, log the most visited combo?

			//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "");
			String toLog;

			String globalParamsOrder = this.getGlobalParamsOrder();

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

				toLog = "";

				Map<Move,Pair<MoveStats,Double>> globalInfo = this.roleProblems[roleProblemIndex].getGlobalMab().getMovesInfo();

				CombinatorialCompactMove theValuesIndices;
				String theValues;

				for(Entry<Move,Pair<MoveStats,Double>> entry : globalInfo.entrySet()){

					theValuesIndices = (CombinatorialCompactMove) entry.getKey();
					theValues = "[ ";
					for(int paramIndex = 0; paramIndex < theValuesIndices.getIndices().length; paramIndex++){
						theValues += (this.parametersManager.getPossibleValues(paramIndex)[theValuesIndices.getIndices()[paramIndex]] + " ");
					}
					theValues += "]";

					//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "ROLE=;" + i + ";MAB=;GLOBAL;COMBINATORIAL_MOVE=;" + entry.getKey() + ";VISITS=;" + entry.getValue().getVisits() + ";SCORE_SUM=;" + entry.getValue().getScoreSum() + ";AVG_VALUE=;" + (entry.getValue().getVisits() <= 0 ? "0" : (entry.getValue().getScoreSum()/((double)entry.getValue().getVisits()))));
					toLog += "\nROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) + ";PARAMS=;" + globalParamsOrder + ";COMB_MOVE=;" + theValues + ";PENALTY=;" + entry.getValue().getSecond() + ";VISITS=;" + entry.getValue().getFirst().getVisits() + ";SCORE_SUM=;" + entry.getValue().getFirst().getScoreSum() + ";AVG_VALUE=;" + (entry.getValue().getFirst().getVisits() <= 0 ? "0" : (entry.getValue().getFirst().getScoreSum()/((double)entry.getValue().getFirst().getVisits()))) + ";";
				}

				toLog += "\n";

				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "GlobalParamTunerStats", toLog);

			}
		}
	}

	private String getGlobalParamsOrder(){
		String globalParamsOrder = "[ ";
		for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
			globalParamsOrder += (this.parametersManager.getName(paramIndex) + " ");
		}
		globalParamsOrder += "]";

		return globalParamsOrder;
	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "EPSILON0 = " + this.epsilon0 +
				indentation + "GLOBAL_MAB_SELECTOR = " + this.globalMabSelector.printComponent(indentation + "  ") +
				indentation + "LOCAL_MABS_SELECTOR = " + this.localMabsSelector.printComponent(indentation + "  ") +
				indentation + "BEST_COMBINATION_SELECTOR = " + this.bestCombinationSelector.printComponent(indentation + "  ") +
				indentation + "SINGLE_BEST = " + this.singleBest +
				indentation + "USE_GLOBAL_BEST = " + this.useGlobalBest +
				indentation + "num_roles_problems = " + (this.roleProblems != null ? this.roleProblems.length : 0);

		if(this.selectedCombinations != null){
			String selectedCombinationsString = "[ ";

			for(int i = 0; i < this.selectedCombinations.length; i++){

				String singleCombinationString = "[ ";
				for(int j = 0; j < this.selectedCombinations[i].length; j++){
					singleCombinationString += this.selectedCombinations[i][j] + " ";
				}
				singleCombinationString += "]";

				selectedCombinationsString += singleCombinationString + " ";

			}

			selectedCombinationsString += "]";

			params += indentation + "SELECTED_COMBINATIONS_INDICES = " + selectedCombinationsString;
		}else{
			params += indentation + "SELECTED_COMBINATIONS = null";
		}

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

	public double getEpsilon0(){
		return this.epsilon0;
	}

	public TunerSelector getGlobalMabSelector(){
		return this.globalMabSelector;
	}

	public TunerSelector getLocalMabSelector(){
		return this.localMabsSelector;
	}

	public TunerSelector getBestCombinationSelector(){
		return this.bestCombinationSelector;
	}

	@Override
	public void decreaseStatistics(double factor) {
		for(int i = 0; i < this.roleProblems.length; i++){
			this.roleProblems[i].decreaseStatistics(factor);
		}
	}

	@Override
	public int getNumIndependentCombinatorialProblems() {
		return this.roleProblems.length;
	}

	@Override
	public boolean isMemorizingBestCombo() {
		return (this.reuseBestCombos && this.roleProblems == null);
	}
}
