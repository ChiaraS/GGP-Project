package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.lang.reflect.InvocationTargetException;
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
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.NaiveProblemRepresentation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.FixedMab;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.IncrementalMab;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.Move;

import csironi.ggp.course.utils.Pair;

public class NaiveParametersTuner extends ParametersTuner {

	private double epsilon0;

	private TunerSelector globalMabSelector;

	private TunerSelector localMabsSelector;

	/**
	 * Given the statistics of each combination, selects the best one among them.
	 */
	private TunerSelector bestCombinationSelector;

	private NaiveProblemRepresentation[] roleProblems;

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

		this.roleProblems = null;

		this.selectedCombinations = null;
	}

	public NaiveParametersTuner(NaiveParametersTuner toCopy) {
		super(toCopy);

		this.epsilon0 = toCopy.getEpsilon0();

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
		}*/
		this.globalMabSelector = toCopy.getGlobalMabSelector();
		this.localMabsSelector = toCopy.getLocalMabSelector();
		this.bestCombinationSelector = toCopy.getBestCombinationSelector();
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.globalMabSelector.setReferences(sharedReferencesCollector);
		this.localMabsSelector.setReferences(sharedReferencesCollector);
		this.bestCombinationSelector.setReferences(sharedReferencesCollector);
	}

	@Override
	public void setUpComponent() {

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		// Create a MAB representation of the combinatorial problem for each role
		this.roleProblems = new NaiveProblemRepresentation[numRolesToTune];

		for(int i = 0; i < this.roleProblems.length; i++){
			roleProblems[i] = new NaiveProblemRepresentation(this.classesLength);
		}

		this.selectedCombinations = new int[numRolesToTune][this.classesLength.length];

		this.globalMabSelector.setUpComponent();

		this.localMabsSelector.setUpComponent();

		this.bestCombinationSelector.setUpComponent();

	}

	@Override
	public void clearComponent() {
		this.globalMabSelector.clearComponent();
		this.localMabsSelector.clearComponent();
		this.bestCombinationSelector.clearComponent();

		this.roleProblems = null;
		this.selectedCombinations = null;
	}

	@Override
	public int[][] selectNextCombinations() {

		// For each role, we check the corresponding naive problem and select a combination of parameters
		for(int i = 0; i < this.roleProblems.length; i++){
			// TODO: the strategy that selects if to use the global or the local mabs is hard-coded.
			// Can be refactored to be customizable.
			if(this.roleProblems[i].getGlobalMab().getNumUpdates() > 0 &&
					this.random.nextDouble() >= this.epsilon0){// Exploit

				this.selectedCombinations[i] = this.exploit(this.roleProblems[i].getGlobalMab());

			}else{ //Explore

				this.selectedCombinations[i] = this.explore(this.roleProblems[i].getLocalMabs());

			}
		}

		return this.selectedCombinations;
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

		return indices;

	}

	@Override
	public int[][] getBestCombinations() {

		// For each role, we check the corresponding global MAB and select a combination of parameters
		for(int i = 0; i < this.roleProblems.length; i++){
			IncrementalMab globalMab = this.roleProblems[i].getGlobalMab();
			Move m = this.bestCombinationSelector.selectMove(globalMab.getMovesInfo(), globalMab.getNumUpdates());
			this.selectedCombinations[i] = ((CombinatorialCompactMove) m).getIndices();
		}

		return this.selectedCombinations;
	}

	@Override
	public void updateStatistics(int[] rewards) {

		if(rewards.length != this.roleProblems.length){
			GamerLogger.logError("ParametersTuner", "NaiveParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + rewards.length +
					") to update the role problems (" + this.roleProblems.length + ").");
			throw new RuntimeException("NaiveParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}

		for(int i = 0; i < this.roleProblems.length; i++){

			/********** Update global MAB **********/

			// Get the info of the combinatorial move in the global MAB
			CombinatorialCompactMove theMove = new CombinatorialCompactMove(this.selectedCombinations[i]);
			Pair<MoveStats,Double> globalInfo = this.roleProblems[i].getGlobalMab().getMovesInfo().get(theMove);

			// If the info doesn't exist, add the move to the MAB, computing the corresponding penalty
			if(globalInfo == null){
				globalInfo = new Pair<MoveStats,Double>(new MoveStats(), this.computeCombinatorialMovePenalty(theMove.getIndices()));
				this.roleProblems[i].getGlobalMab().getMovesInfo().put(theMove, globalInfo);
			}

			// Update the stats
			globalInfo.getFirst().incrementScoreSum(rewards[i]);
			globalInfo.getFirst().incrementVisits();

			// Increase total num updates
			this.roleProblems[i].getGlobalMab().incrementNumUpdates();

			/********** Update local MABS **********/

			// Update the stats for each local MAB
			FixedMab[] localMabs = this.roleProblems[i].getLocalMabs();
			for(int j = 0; j < localMabs.length; j++){
				MoveStats localStats = localMabs[j].getMoveStats()[this.selectedCombinations[i][j]];
				// Update the stats
				localStats.incrementScoreSum(rewards[i]);
				localStats.incrementVisits();

				localMabs[j].incrementNumUpdates();
			}
		}
	}

	@Override
	public void logStats() {

		//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "");
		String toLog = "";

		for(int i = 0; i < this.roleProblems.length; i++){

			Map<Move,Pair<MoveStats,Double>> globalInfo = this.roleProblems[i].getGlobalMab().getMovesInfo();

			for(Entry<Move,Pair<MoveStats,Double>> entry : globalInfo.entrySet()){
				//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "ROLE=;" + i + ";MAB=;GLOBAL;COMBINATORIAL_MOVE=;" + entry.getKey() + ";VISITS=;" + entry.getValue().getVisits() + ";SCORE_SUM=;" + entry.getValue().getScoreSum() + ";AVG_VALUE=;" + (entry.getValue().getVisits() <= 0 ? "0" : (entry.getValue().getScoreSum()/((double)entry.getValue().getVisits()))));
				toLog += "\nROLE=;" + i + ";MAB=;GLOBAL;COMB_MOVE=;" + entry.getKey() + ";PENALTY=;" + entry.getValue().getSecond() + ";VISITS=;" + entry.getValue().getFirst().getVisits() + ";SCORE_SUM=;" + entry.getValue().getFirst().getScoreSum() + ";AVG_VALUE=;" + (entry.getValue().getFirst().getVisits() <= 0 ? "0" : (entry.getValue().getFirst().getScoreSum()/((double)entry.getValue().getFirst().getVisits())));
			}

			FixedMab[] localMabs = this.roleProblems[i].getLocalMabs();

			for(int j = 0; j < localMabs.length; j++){
				for(int k = 0; k < localMabs[j].getMoveStats().length; k++){
					//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "ROLE=;" + i + ";MAB=;LOCAL" + j + ";UNIT_MOVE=;" + k + ";VISITS=;" + localMabs[j].getMoveStats()[k].getVisits() + ";SCORE_SUM=;" + localMabs[j].getMoveStats()[k].getScoreSum() + ";AVG_VALUE=;" + (localMabs[j].getMoveStats()[k].getVisits() <= 0 ? "0" : (localMabs[j].getMoveStats()[k].getScoreSum()/((double)localMabs[j].getMoveStats()[k].getVisits()))));
					toLog += "\nROLE=;" + i + ";MAB=;LOCAL" + j + ";UNIT_MOVE=;" + k + ";PENALTY=;" + this.unitMovesPenalty[j][k] + ";VISITS=;" + localMabs[j].getMoveStats()[k].getVisits() + ";SCORE_SUM=;" + localMabs[j].getMoveStats()[k].getScoreSum() + ";AVG_VALUE=;" + (localMabs[j].getMoveStats()[k].getVisits() <= 0 ? "0" : (localMabs[j].getMoveStats()[k].getScoreSum()/((double)localMabs[j].getMoveStats()[k].getVisits())));
				}
			}

			toLog += "\n";

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", toLog);

		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "EPSILON0 = " + this.epsilon0 +
				indentation + "GLOBAL_MAB_SELECTOR = " + this.globalMabSelector.printComponent(indentation + "  ") +
				indentation + "LOCAL_MABS_SELECTOR = " + this.localMabsSelector.printComponent(indentation + "  ") +
				indentation + "BEST_COMBINATION_SELECTOR = " + this.bestCombinationSelector.printComponent(indentation + "  ") +
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

	@Override
	public int getNumIndependentCombinatorialProblems() {
		return this.roleProblems.length;
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

}
