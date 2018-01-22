package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.discretetuners;

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
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.DynamicComboSamplesEstimator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.ProblemRepParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.SimLimitedLsiProblemRepresentation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.SimLimitedLsiProblemRepresentation.Phase;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.reflection.ProjectSearcher;

public class SimLimitedLsiParametersTuner extends DiscreteParametersTuner {

	/**
	 * Number of combinations evaluated so far.
	 */
	private int sampledCombos;

	/**
	 * Estimator of the total number of combinations that LSI will be able to sample during the game.
	 * If null we will use the default number of total samples.
	 */
	private DynamicComboSamplesEstimator estimator;

	/**
	 * Default value for the total number of samples to be used by LSI.
	 * This default value is used when we don't want to estimate the value dynamically or
	 * when we want to estimate the value dynamically but something goes wrong and we cannot
	 * compute the estimate.
	 */
	private int defaultNumTotalSamples;

	/**
	 * Percentage (i.e. in [0, 1])
	 */
	private double genSamplesPercentage;

	/**
	 * Given the statistics collected so far for each combination, selects the best one among them.
	 */
	private TunerSelector bestCombinationSoFarSelector;

	/**
	 * Parameters that are shared by all role problems.
	 */
	private ProblemRepParameters problemRepParameters;

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

		this.sampledCombos = 0;

		if(gamerSettings.getBooleanPropertyValue("ParametersTuner.dynamicSamples")) {
			this.estimator = new DynamicComboSamplesEstimator(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		}

		this.defaultNumTotalSamples = gamerSettings.getIntPropertyValue("ParametersTuner.defaultNumTotalSamples");

		this.genSamplesPercentage = gamerSettings.getDoublePropertyValue("ParametersTuner.genSamplesPercentage");
		// NOTE: for now the version of LSI that sets the samples for the two phases dynamically at runtime cannot handle
		// the case when we have 0 available samples for the generation phase, because the first samples used for the generation
		// phase are also used to compute the expected total number of samples.
		// TODO: extend this class to deal with the fact that we might set genSamplesPercentage=0 because we want to skip the
		// generation phase and perform only sequential halving on randomly generated combinations.
		// NOTE that when we are not using a dynamic computation of the expected total number of samples (i.e. dynamicSamples==false)
		// this version of LSI can already deal with this.genSamplesPercentage==0
		if(this.genSamplesPercentage <= 0 || this.genSamplesPercentage > 1) {
			GamerLogger.logError("SearchManagerCreation", "Error when creating SimLimitedLsiParametersTuner. The value of genSamplesPercentage must be in (0,1].");
			throw new RuntimeException("SearchManagerCreation - Error when creating SimLimitedLsiParametersTuner. The value of genSamplesPercentage must be in (0,1].");
		}

		/**
		// If the settings specify both a fixed number of generation samples and a fixed number
		// of evaluation samples read those values,...
		if(gamerSettings.specifiesProperty("ParametersTuner.numGenSamples") && gamerSettings.specifiesProperty("ParametersTuner.numEvalSamples")) {
			this.numGenSamples = gamerSettings.getIntPropertyValue("ParametersTuner.numGenSamples");
			this.numEvalSamples = gamerSettings.getIntPropertyValue("ParametersTuner.numEvalSamples");
			this.genSamplesPercentage = -1;
		}else {
			// ...otherwise read the percentage of samples that must be used for the generation phase.
			this.numGenSamples = -1;
			this.numEvalSamples = -1;
			this.genSamplesPercentage = gamerSettings.getDoublePropertyValue("ParametersTuner.genSamplesPercentage");
			// NOTE: for now the version of LSI that sets the samples for the two phases dynamically at runtime cannot handle
			// the case when we have 0 available samples for the generation phase, because the first samples used for the generation
			// phase are also used to compute the expected total number of samples.
			// TODO: extend this class to deal with the fact that we might set genSamplesPercentage=0 because we want to skip the
			// generation phase and perform only sequential halving on randomly generated combinations.
			if(this.genSamplesPercentage <= 0 || this.genSamplesPercentage > 1) {
				GamerLogger.logError("SearchManagerCreation", "Error when creating SimLimitedLsiParametersTuner. The value of genSamplesPercentage must be in (0,1].");
				throw new RuntimeException("SearchManagerCreation - Error when creating SimLimitedLsiParametersTuner. The value of genSamplesPercentage must be in (0,1].");
			}

		}*/

		this.problemRepParameters = new ProblemRepParameters(new RandomSelector(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, ""),
				gamerSettings.getIntPropertyValue("ParametersTuner.numCandidatesToGenerate"),
				gamerSettings.getBooleanPropertyValue("ParametersTuner.updateAll"));

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

		sharedReferencesCollector.setSimLimitedLsiParametersTuner(this);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		super.setReferences(sharedReferencesCollector);

		if(this.estimator != null) {
			this.estimator.setReferences(sharedReferencesCollector);
		}

		this.bestCombinationSoFarSelector.setReferences(sharedReferencesCollector);

		this.problemRepParameters.getRandomSelector().setReferences(sharedReferencesCollector);

	}

	@Override
	public void setUpComponent() {

		super.setUpComponent();

		this.sampledCombos = 0;

		if(this.estimator != null) {
			this.estimator.setUpComponent();
		}

		this.bestCombinationSoFarSelector.setUpComponent();

		this.problemRepParameters.getRandomSelector().setUpComponent();

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		if(!this.reuseBestCombos || this.bestCombinations == null || this.bestCombinations.length != numRolesToTune){

			if(this.estimator != null) {
				// Set the generation samples to infinity...
				this.problemRepParameters.setDynamicNumGenSamples(Integer.MAX_VALUE);
				// ...and the eval samples to -1 to indicate that they haven't been estimated yet
				this.problemRepParameters.setDynamicNumEvalSamples(Integer.MAX_VALUE);
			}else{
				int numGenSamples = (int) Math.round(this.defaultNumTotalSamples * this.genSamplesPercentage);
				this.problemRepParameters.setDynamicNumGenSamples(numGenSamples);
				this.problemRepParameters.setDynamicNumEvalSamples(this.defaultNumTotalSamples - numGenSamples);
			}

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

				this.roleProblems[roleProblemIndex] = new SimLimitedLsiProblemRepresentation(this.discreteParametersManager, this.random, this.problemRepParameters);
			}

			this.selectedCombinations = new int[numRolesToTune][this.discreteParametersManager.getNumTunableParameters()];

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

		this.sampledCombos = 0;

		if(this.estimator != null) {
			this.estimator.clearComponent();
		}

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

		this.discreteParametersManager.setParametersValues(selectedCombinations);

		if(foundAllBest){
			// Log the combination that we are selecting as best
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(this.selectedCombinations, this.isIntermediate));
			this.stopTuning();
		}

	}

	/**
	 * This method exists to be called by the TunerBeforeSimulation strategy when the tuning has already been stopped.
	 * With this method we can continue counting all the samples that we actually had available during the game even
	 * if the tuner has already been stopped due to underestimating (dynamically or by the predefined settings) the
	 * total number of combinations that we would have been able to sample.
	 * Note that this is a quick fix to ensure that we can count and log the actual number of samples that we should
	 * have used for the game to exploit all available samples.
	 */
	public void increaseSampledCombos() {
		this.sampledCombos++;
	}

	@Override
	public void setBestCombinations() {

		if(this.isMemorizingBestCombo()){
			// Log the combination that we are selecting as best
			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(this.bestCombinations, this.isIntermediate));

			this.discreteParametersManager.setParametersValues(this.bestCombinations);
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
					for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){
						toLog += this.discreteParametersManager.getPossibleValues(paramIndex)[combinations[roleProblemIndex][paramIndex]] + " ";
					}
				}else{
					for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){
						toLog += null + " ";
					}
				}
				toLog += "];FINAL=;" + !isIntermediate[roleProblemIndex] + ";\n";
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
			toLog += "];FINAL=;" + !isIntermediate[0] + ";\n";
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
							this.discreteParametersManager.getValuesFeasibility(paramIndex, indices),
							// If for a parameter no penalties are specified, a penalty of 0 is assumed for all of the values.
							(this.discreteParametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.discreteParametersManager.getPossibleValuesPenalty(paramIndex) : new double[this.discreteParametersManager.getNumPossibleValues(paramIndex)]),
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

		this.discreteParametersManager.setParametersValues(selectedCombinations);

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

		this.sampledCombos++;
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
								";PARAM=;" + this.discreteParametersManager.getName(paramIndex) + ";UNIT_MOVE=;" + this.discreteParametersManager.getPossibleValues(paramIndex)[paramValueIndex] +
								";PENALTY=;" + (this.discreteParametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.discreteParametersManager.getPossibleValuesPenalty(paramIndex)[paramValueIndex] : 0) +
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
							theValues += (this.discreteParametersManager.getPossibleValues(paramIndex)[theValuesIndices.getIndices()[paramIndex]] + " ");
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

	/**
	 * Logs data about how many samples have been considered and how they have been
	 * divided among the generation and evaluation phase.
	 */
	public void logSamplesDistribution() {

		int estimatedTotalSamples;

		if(this.estimator != null && this.estimator.getEstimatedTotalSamples() != -1) {
			estimatedTotalSamples = this.estimator.getEstimatedTotalSamples();
		}else{
			estimatedTotalSamples = this.defaultNumTotalSamples;
		}

		String toLog = "Estimated game length;Actual game length;Actual search time;Combinations/second;Estimated available samples;Actual available samples;";

		if(this.estimator != null) {
			toLog += ("\n" + this.estimator.getEstimatedGameLength() + ";" + this.gameDependentParameters.getGameStep() + ";" +
					+ this.gameDependentParameters.getActualPlayClock() + ";" +
					this.estimator.getSampledCombosPerSecond() + ";" + estimatedTotalSamples + ";" +
					this.sampledCombos + ";");
		}else {
			toLog += ("\n-1;" + this.gameDependentParameters.getGameStep() + ";" + this.gameDependentParameters.getActualPlayClock() + ";-1;" + this.defaultNumTotalSamples + ";" + this.sampledCombos + ";");
		}

		GamerLogger.log(FORMAT.CSV_FORMAT, "SamplesEstimates", toLog);

		////////////////////////////////////////////////////

		toLog = "Role;Estimated available samples;Used samples;Estimated generation samples;Used generation samples;Estimated evaluation samples;Used evaluation samples;Defined num candidates to generate;Actual num generated candidates;LSI terminated;";

		if(this.tuneAllRoles){
			for(int roleProblemIndex = 0; roleProblemIndex < this.gameDependentParameters.getNumRoles(); roleProblemIndex++){
				toLog += ("\n" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleProblemIndex)) + ";" +
						estimatedTotalSamples + ";" + (this.roleProblems[roleProblemIndex].getActualNumGenSamples() + this.roleProblems[roleProblemIndex].getActualNumEvalSamples()) + ";" +
						this.problemRepParameters.getDynamicNumGenSamples() + ";" + this.roleProblems[roleProblemIndex].getActualNumGenSamples() + ";" +
						this.problemRepParameters.getDynamicNumEvalSamples() + ";" + this.roleProblems[roleProblemIndex].getActualNumEvalSamples() + ";" +
						this.problemRepParameters.getNumCandidatesToGenerate() + ";" +
						(this.roleProblems[roleProblemIndex].getGeneratedCandidatesStats() != null ? this.roleProblems[roleProblemIndex].getGeneratedCandidatesStats().size() : "0") + ";" +
						!this.isIntermediate[roleProblemIndex] + ";");
			}
		}else{ // Tuning only my role
			toLog += ("\n" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(this.gameDependentParameters.getMyRoleIndex())) +
					estimatedTotalSamples + ";" + (this.roleProblems[0].getActualNumGenSamples() + this.roleProblems[0].getActualNumEvalSamples()) + ";" +
					this.problemRepParameters.getDynamicNumGenSamples() + ";" + this.roleProblems[0].getActualNumGenSamples() + ";" +
					this.problemRepParameters.getDynamicNumEvalSamples() + ";" + this.roleProblems[0].getActualNumEvalSamples() + ";" +
					!this.isIntermediate[0] + ";");
		}

		GamerLogger.log(FORMAT.CSV_FORMAT, "SamplesUsagePerRole", toLog);

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

		String params =	indentation + "sampled_combos = " + this.sampledCombos +
				indentation + "ESTIMATOR = " + (this.estimator != null ? this.estimator.printComponent(indentation + "  ") : "null") +
				indentation + "DEFAULT_NUM_TOTAL_SAMPLES = " + this.defaultNumTotalSamples +
				indentation + "GEN_SAMPLES_PERCENTAGE = " + this.genSamplesPercentage +
				indentation + "BEST_COMBINATION_SO_FAR_SELECTOR = " + this.bestCombinationSoFarSelector.printComponent(indentation + "  ") +
				indentation + "RANDOM_SELECTOR = " + this.problemRepParameters.getRandomSelector().printComponent(indentation + "  ") +
				indentation + "DYNAMIC_NUM_GEN_SAMPLES = " + this.problemRepParameters.getDynamicNumGenSamples() +
				indentation + "UPDATE_ALL = " + this.problemRepParameters.getUpdateAll() +
				indentation + "DYNAMIC_NUM_EVAL_SAMPLES = " + this.problemRepParameters.getDynamicNumEvalSamples() +
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

	public void estimateTotalNumberOfSamples() {
		if(this.estimator != null) {
			this.estimator.estimateTotalSamples(this.sampledCombos);
			// Check if the estimated total number of samples has been computed correctly (!=-1)
			if(this.estimator.getEstimatedTotalSamples() != -1) {
				// NOTE that by computing the number of samples for generation and evaluation now, it might happen that
				// the number of samples taken for the generation phase so far is already higher than the estimated total
				// available number of generation samples. We can't do anything about this, after setting the new, just
				// computed number of generation samples each role problem will figure out that it's time to stop the
				// generation phase.
				int numGenSamples = (int) Math.round(this.estimator.getEstimatedTotalSamples() * this.genSamplesPercentage);
				this.problemRepParameters.setDynamicNumGenSamples(numGenSamples);
				this.problemRepParameters.setDynamicNumEvalSamples(this.estimator.getEstimatedTotalSamples() - numGenSamples);
			}else { // If not, split the default number of total samples among generation and evaluation phase
				int numGenSamples = (int) Math.round(this.defaultNumTotalSamples * this.genSamplesPercentage);
				this.problemRepParameters.setDynamicNumGenSamples(numGenSamples);
				this.problemRepParameters.setDynamicNumEvalSamples(this.defaultNumTotalSamples - numGenSamples);
			}
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
