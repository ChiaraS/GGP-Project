package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.RandomSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.ProblemRepParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.SimLimitedLsiProblemRepresentation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.SimLimitedLsiProblemRepresentation.Phase;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

public class SimLimitedLsiParametersTuner extends ParametersTuner {

	/**
	 * Parameters that are shared by all role problems.
	 */
	private ProblemRepParameters problemRepParameters;

	/**
	 * Percentage (i.e. in [0, 1])
	 */
	private double genSamplesPercentage;

	/**
	 * Given the statistics collected so far for each combination, selects the best one among them.
	 */
	private TunerSelector bestCombinationSoFarSelector;

	/**
	 * Lsi problem representations for each of the roles for which the parameters are being tuned.
	 */
	private SimLimitedLsiProblemRepresentation[] roleProblems;

	/**
	 * Memorizes the last selected combination for each role.
	 */
	private int[][] selectedCombinations;

	/** When all roles are done tuning using LSI,
	 * this variable will contain the best combination for each of them.
	 */
	private int[][] bestCombinations;

	/**
	 * True if the corresponding best combination has been computed before the LSI algorithm could complete.
	 * False if it has been computed by the proper termination of the LSI algorithm. The LSI algorithm doesn't
	 * complete properly if the total number of simulations that it requires is higher than the number of
	 * simulations performed during the whole game.
	 */
	private boolean[] isIntermediate;

	public SimLimitedLsiParametersTuner(GameDependentParameters gameDependentParameters, Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		int numGenSamples;
		int numEvalSamples;
		// If the settings specify both a fixed number of generation samples and a fixed number
		// of evaluation samples use those values,...
		if(gamerSettings.specifiesProperty("ParametersTuner.numGenSamples") && gamerSettings.specifiesProperty("ParametersTuner.numEvalSamples")) {
			numGenSamples = gamerSettings.getIntPropertyValue("ParametersTuner.numGenSamples");
			numEvalSamples = gamerSettings.getIntPropertyValue("ParametersTuner.numEvalSamples");
		}else {
			// ...otherwise set both values to the max possible value and compute an estimate of the total
			// number of samples that we will be able to take during the game and divide them according to
			// the percentage that MUST be specified in the settings.
			numGenSamples = Integer.MAX_VALUE;
			numEvalSamples = Integer.MAX_VALUE;
		}

		this.problemRepParameters = new ProblemRepParameters(new RandomSelector(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, ""),
				gamerSettings.getIntPropertyValue("ParametersTuner.numCandidatesToGenerate"),
				numGenSamples, numEvalSamples, gamerSettings.getBooleanPropertyValue("ParametersTuner.updateAll"));


		// TODO: specify only the percentage of the total budget that must be used for generation. As total budget use
		// the BeforeSimulationStrategy.simBudget, if any, otherwise throw exception because the SimLimitedLsiTuner can
		// be used only if the number of simulations is limited.
		String[] tunerSelectorDetails = gamerSettings.getIDPropertyValue("ParametersTuner.bestCombinationSoFarSelectorType");

		try {
			this.bestCombinationSoFarSelector = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("ParametersTuner.bestCombinationSoFarSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		this.roleProblems = null;

		this.selectedCombinations = null;

		this.bestCombinations = null;

		this.isIntermediate = null;

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		super.setReferences(sharedReferencesCollector);

		this.problemRepParameters.getRandomSelector().setReferences(sharedReferencesCollector);

		this.bestCombinationSoFarSelector.setReferences(sharedReferencesCollector);

	}

	@Override
	public void setUpComponent() {

		super.setUpComponent();

		this.problemRepParameters.getRandomSelector().setUpComponent();

		this.bestCombinationSoFarSelector.setUpComponent();

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		if(!this.reuseBestCombos || this.bestCombinations == null || this.bestCombinations.length != numRolesToTune){

			this.roleProblems = new SimLimitedLsiProblemRepresentation[numRolesToTune];

			//int numSamplesPerValue = this.numGenSamples/this.parametersManager.getTotalNumPossibleValues(); // Same result as if we used floor(this.numGenSamples/this.parametersManager.getTotalNumPossibleValues();)

			// Make sure that we'll have at least one sample per parameter value
			//if(numSamplesPerValue == 0){
			//	numSamplesPerValue = 1;
			//}

			//Compute the exact number of generation samples that we need to use. Since all parameters values must be
			// tested for the same amount of times, this number might be lower than the one read from the settings.
			//this.numGenSamples = numSamplesPerValue * this.parametersManager.getTotalNumPossibleValues();

			//List<MyPair<CombinatorialCompactMove,Integer>> actionsToTest;

			// For each role for which we are tuning create the corresponding role problem
			for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

				/*
				// For each value x of each parameter we generate numSamplesPerValue sample combinations containing x,
				// completing the parameter combination with random values for the other parameters.
				actionsToTest = new ArrayList<MyPair<CombinatorialCompactMove,Integer>>();

				for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
					for(int valueIndex = 0; valueIndex < this.parametersManager.getNumPossibleValues(paramIndex); valueIndex++){
						for(int i = 0; i < numSamplesPerValue; i++){
							actionsToTest.add(new MyPair<CombinatorialCompactMove,Integer>(new CombinatorialCompactMove(this.randomlyCompleteCombinatorialMove(paramIndex,valueIndex)),new Integer(paramIndex)));
						}
					}
				}

				// Randomize order in which the combinations will be tested for each role so that the combinations
				// won't be tested always against the same combination for all the roles.
				Collections.shuffle(actionsToTest);
				*/

				this.roleProblems[roleProblemIndex] = new SimLimitedLsiProblemRepresentation(this.parametersManager, this.random, this.problemRepParameters);
			}

			this.selectedCombinations = new int[numRolesToTune][this.parametersManager.getNumTunableParameters()];

			this.bestCombinations = null;

			this.isIntermediate = new boolean[numRolesToTune];
		}else{
			this.roleProblems = null;
			this.selectedCombinations = null;
		}

	}

	@Override
	public void clearComponent() {

		super.clearComponent();

		this.problemRepParameters.getRandomSelector().clearComponent();

		this.bestCombinationSoFarSelector.clearComponent();

		this.roleProblems = null;

		this.selectedCombinations = null;

	}

	@Override
	public void setNextCombinations() {

		boolean foundAllBest = true;
		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
			if(this.roleProblems[roleProblemIndex].getPhase() != Phase.STOP){
				if(this.roleProblems[roleProblemIndex].getPhase() != Phase.BEST){
					foundAllBest = false;
				}
				this.selectedCombinations[roleProblemIndex] = this.roleProblems[roleProblemIndex].getNextCombination();

			}
		}

		this.parametersManager.setParametersValues(selectedCombinations);

		if(foundAllBest){
			// Log the combination that we are selecting as best
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(this.selectedCombinations, this.isIntermediate));
			this.stopTuning();
		}

	}

	@Override
	public void setBestCombinations() {

		if(this.isMemorizingBestCombo()){
			// Log the combination that we are selecting as best
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(this.bestCombinations, this.isIntermediate));

			this.parametersManager.setParametersValues(this.bestCombinations);
		}else{
			// Set best combo found so far
			this.setBestCombinationSoFar();
			//GamerLogger.logError("ParametersTuner", "LsiParametersTuner - Impossible to set best combinations! The best combinations haven't been found for all roles yet!");
			//throw new RuntimeException("LsiParametersTuner - Impossible to set best combinations! The best combinations haven't been found for all roles yet!");
		}

		this.stopTuning();

	}

	/**
	 *
	 * @param combinations the combinations to log
	 * @param isFinal true if the best combination has been computed by the complete execution of LSI,
	 * false if it has been set when the algorithm was still running, and thus is the best combination
	 * wrt the statistics collected so far.
	 * @return
	 */
	private String getLogOfCombinations(int[][] combinations, boolean[] isIntermediate){

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
				toLog += "];FINAL=;" + !isIntermediate[roleProblemIndex] + "\n";
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
			toLog += "];FINAL=;" + !isIntermediate[0] + "\n";
		}

		return toLog;
	}

	/**
	 * Depending on the phase of LSI, this method returns the best combination found so far even if the algorithm is not done yet
	 * @return
	 */
	public void setBestCombinationSoFar(){
		// For each role, we select a combination of parameters
		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			switch(this.roleProblems[roleProblemIndex].getPhase()){
			case GENERATION:
				MoveStats[][] paramsStats = this.roleProblems[roleProblemIndex].getParamsStats();
				int[] indices = new int[paramsStats.length];
				for(int i = 0; i < indices.length; i++){
					indices[i] = -1;
				}

				// Select a value for each parameter independently
				for(int paramIndex = 0; paramIndex < paramsStats.length; paramIndex++){
					int numUpdates = 0;
					for(int valueIndex = 0; valueIndex < paramsStats[paramIndex].length; valueIndex++){
						numUpdates += paramsStats[paramIndex][valueIndex].getVisits();
					}
					indices[paramIndex] = this.bestCombinationSoFarSelector.selectMove(paramsStats[paramIndex],
							this.parametersManager.getValuesFeasibility(paramIndex, indices),
							// If for a parameter no penalties are specified, a penalty of 0 is assumed for all of the values.
							(this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex) : new double[this.parametersManager.getNumPossibleValues(paramIndex)]),
							numUpdates);
				}
				this.selectedCombinations[roleProblemIndex] = indices;
				this.isIntermediate[roleProblemIndex] = true;

				break;
			case EVALUATION:
				List<CompleteMoveStats> currentCandidatesStats = new ArrayList<CompleteMoveStats>(this.roleProblems[roleProblemIndex].getGeneratedCandidatesStats().subList(0, this.roleProblems[roleProblemIndex].getNumCandidatesOfCurrentIteration()));

				Collections.sort(currentCandidatesStats,
						new Comparator<CompleteMoveStats>(){

							@Override
							public int compare(CompleteMoveStats o1, CompleteMoveStats o2) {

								double value1;
								if(o1.getVisits() == 0){
									value1 = 0;
								}else{
									value1 = o1.getScoreSum()/o1.getVisits();
								}
								double value2;
								if(o2.getVisits() == 0){
									value2 = 0;
								}else{
									value2 = o2.getScoreSum()/o2.getVisits();
								}
								// Sort from largest to smallest
								if(value1 > value2){
									return -1;
								}else if(value1 < value2){
									return 1;
								}else{
									return 0;
								}
							}

						});

				this.selectedCombinations[roleProblemIndex] = ((CombinatorialCompactMove) currentCandidatesStats.get(0).getTheMove()).getIndices();
				this.isIntermediate[roleProblemIndex] = true;
				break;
			case BEST: case STOP:
				// The best move has been found and is the first one in the list of candidates
				this.selectedCombinations[roleProblemIndex] = ((CombinatorialCompactMove) this.roleProblems[roleProblemIndex].getGeneratedCandidatesStats().get(0).getTheMove()).getIndices();
				break;
			}
		}

		this.parametersManager.setParametersValues(selectedCombinations);

		// Log the combination that we are selecting as best
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(this.selectedCombinations, this.isIntermediate));

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
			GamerLogger.logError("ParametersTuner", "LsiParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + neededRewards.length +
					") to update the role problems (" + this.roleProblems.length + ").");
			throw new RuntimeException("LsiParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			this.roleProblems[roleProblemIndex].updateStatsOfCombination(neededRewards[roleProblemIndex]);



		}
	}

	@Override
	public void logStats() {

		if(this.roleProblems != null){

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

				/*

				toLog = "";

				toLog = "\nROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex));

				toLog = globalParamsOrder + ";TESTED_PARAM;";

				List<MyPair<CombinatorialCompactMove,Integer>> combinationsToTest = this.roleProblems[roleProblemIndex].getCombinationsToTest();

				for(MyPair<CombinatorialCompactMove,Integer> combo : combinationsToTest){
					toLog += "\n" + combo.getFirst() + ";" + combo.getSecond().intValue() + ";";
				}

				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TestedCombos", toLog);
				*/

				toLog = "";

				MoveStats[][] paramsStats = this.roleProblems[roleProblemIndex].getParamsStats();

				for(int paramIndex = 0; paramIndex < paramsStats.length; paramIndex++){

					for(int paramValueIndex = 0; paramValueIndex < paramsStats[paramIndex].length; paramValueIndex++){
						//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "ROLE=;" + i + ";MAB=;LOCAL" + j + ";UNIT_MOVE=;" + k + ";VISITS=;" + localMabs[j].getMoveStats()[k].getVisits() + ";SCORE_SUM=;" + localMabs[j].getMoveStats()[k].getScoreSum() + ";AVG_VALUE=;" + (localMabs[j].getMoveStats()[k].getVisits() <= 0 ? "0" : (localMabs[j].getMoveStats()[k].getScoreSum()/((double)localMabs[j].getMoveStats()[k].getVisits()))));
						toLog += "\nROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) +
								";PARAM=;" + this.parametersManager.getName(paramIndex) + ";UNIT_MOVE=;" + this.parametersManager.getPossibleValues(paramIndex)[paramValueIndex] +
								";PENALTY=;" + (this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex)[paramValueIndex] : 0) +
								";VISITS=;" + paramsStats[paramIndex][paramValueIndex].getVisits() + ";SCORE_SUM=;" + paramsStats[paramIndex][paramValueIndex].getScoreSum() +
								";AVG_VALUE=;" + (paramsStats[paramIndex][paramValueIndex].getVisits() <= 0 ? "0" : (paramsStats[paramIndex][paramValueIndex].getScoreSum()/((double)paramsStats[paramIndex][paramValueIndex].getVisits()))) + ";";
					}
				}

				toLog += "\n";

				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "LocalParamTunerStats", toLog);

				List<CompleteMoveStats> globalInfo = this.roleProblems[roleProblemIndex].getGeneratedCandidatesStats();

				if(globalInfo != null){

					toLog = "";

					CombinatorialCompactMove theValuesIndices;
					String theValues;

					// Log at most 2000 global stats per role.
					// This will prevent files to become too big and the player to take too much time to log.
					//int numStats = 0;

					for(CompleteMoveStats stats : globalInfo){

						// Log at most 2000 global stats per role.
						// This will prevent files to become too big and the player to take too much time to log.
						//if(numStats == 2000){
						//	break;
						//}

						theValuesIndices = (CombinatorialCompactMove) stats.getTheMove();
						theValues = "[ ";
						for(int paramIndex = 0; paramIndex < theValuesIndices.getIndices().length; paramIndex++){
							theValues += (this.parametersManager.getPossibleValues(paramIndex)[theValuesIndices.getIndices()[paramIndex]] + " ");
						}
						theValues += "]";

						//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "ROLE=;" + i + ";MAB=;GLOBAL;COMBINATORIAL_MOVE=;" + entry.getKey() + ";VISITS=;" + entry.getValue().getVisits() + ";SCORE_SUM=;" + entry.getValue().getScoreSum() + ";AVG_VALUE=;" + (entry.getValue().getVisits() <= 0 ? "0" : (entry.getValue().getScoreSum()/((double)entry.getValue().getVisits()))));
						toLog += "\nROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) +
								";PARAMS=;" + globalParamsOrder + ";COMB_MOVE=;" + theValues + ";PENALTY=;0;VISITS=;" + stats.getVisits() +
								";SCORE_SUM=;" + stats.getScoreSum() + ";AVG_VALUE=;" + (stats.getVisits() <= 0 ? "0" : (stats.getScoreSum()/((double)stats.getVisits()))) + ";";

						//numStats++;
					}

					toLog += "\n";

					GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "GlobalParamTunerStats", toLog);
				}

			}
		}

	}

	@Override
	public void decreaseStatistics(double factor) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isMemorizingBestCombo() {
		return (this.reuseBestCombos && this.roleProblems == null);
	}

	@Override
	public void memorizeBestCombinations(){
		this.bestCombinations = this.selectedCombinations;
	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "RANDOM_SELECTOR = " + this.problemRepParameters.getRandomSelector().printComponent(indentation + "  ") +
				indentation + "BEST_COMBINATION_SO_FAR_SELECTOR = " + this.bestCombinationSoFarSelector.printComponent(indentation + "  ") +
				indentation + "NUM_GEN_SAMPLES = " + this.problemRepParameters.getNumGenSamples() +
				indentation + "UPDATE_ALL = " + this.problemRepParameters.getUpdateAll() +
				indentation + "NUM_EVAL_SAMPLES = " + this.problemRepParameters.getNumEvalSamples() +
				indentation + "NUM_CANDIDATES_TO_GENERATE = " + this.problemRepParameters.getNumCandidatesToGenerate() +
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

			params += indentation + "BEST_COMBINATIONS_INDICES = " + bestCombinationsString;
		}else{
			params += indentation + "BEST_COMBINATIONS = null";
		}

		if(this.isIntermediate != null){
			String intermediateString = "[ ";

			for(int i = 0; i < this.isIntermediate.length; i++){
				intermediateString += this.isIntermediate[i] + " ";
			}

			intermediateString += "]";

			params += indentation + "IS_INTERMEDIATE = " + intermediateString;
		}else{
			params += indentation + "IS_INTERMEDIATE = null";
		}

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

	/*

	public static void main(String args[]){

		List<CompleteMoveStats> a = new ArrayList<CompleteMoveStats>();

		for(int i = 0; i < 8; i++){
			a.add(new CompleteMoveStats(10, new Random().nextInt(101), new CompactMove(i)));
		}

		System.out.println();
		for(int i = 0; i < 8; i++){
			System.out.println("[" + a.get(i) + "]");
		}

		Collections.sort(a.subList(0, 4),
				new Comparator<CompleteMoveStats>(){

					@Override
					public int compare(CompleteMoveStats o1,
							CompleteMoveStats o2) {

						double value1;
						if(o1.getVisits() == 0){
							value1 = 0;
						}else{
							value1 = o1.getScoreSum()/o1.getVisits();
						}
						double value2;
						if(o2.getVisits() == 0){
							value2 = 0;
						}else{
							value2 = o2.getScoreSum()/o2.getVisits();
						}

						if(value1 > value2){
							return 1;
						}else if(value1 < value2){
							return -1;
						}else{
							return 0;
						}
					}

		});

		System.out.println();
		for(int i = 0; i < 8; i++){
			System.out.println("[" + a.get(i) + "]");
		}
	}

	*/
}
