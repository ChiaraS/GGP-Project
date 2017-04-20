package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;
import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.RandomSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.LsiProblemRepresentation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.LsiProblemRepresentation.Phase;
import org.ggp.base.util.logging.GamerLogger;

import csironi.ggp.course.utils.MyPair;

public class LSIParametersTuner extends ParametersTuner {

	/**
	 * Random selector used to select random values for the parameters when completing combinatorial moves.
	 */
	private RandomSelector randomSelector;

	/**
	 * Number of samples (i.e. simulations) that will be dedicated to the generation of
	 * candidate combinatorial actions (i.e. combinations of parameters) that will be
	 * evaluated in the subsequent phase.
	 */
	private int numGenSamples;

	/**
	 * True if when receiving the reward for a certain combinatorial action we want to use it to update the stats
	 * of all unit actions that form the combinatorial action. False if we want to use it only to update the stats
	 * for the only unit action that wasn't generated randomly when creating the combinatorial action.
	 */
	private boolean updateAll;

	/**
	 * Number of samples (i.e. simulations) that will be dedicated to the evaluation of
	 * the generated combinatorial actions (i.e. combinations of parameters) before
	 * committing to a single combinatorial action (i.e. combination of parameters).
	 */
	private int numEvalSamples;

	/**
	 * Number of combinations to be generated for the evaluation phase.
	 */
	private int numCandidatesToGenerate;

	/**
	 * Lsi problem representations for each of the roles for which the parameters are being tuned.
	 */
	private LsiProblemRepresentation[] roleProblems;

	/**
	 * Memorizes the last selected combination for each role. When all roles are done tuning using LSI,
	 * this variable will contain the best combination for each of them.
	 */
	private int[][] selectedCombinations;

	public LSIParametersTuner(GameDependentParameters gameDependentParameters, Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.randomSelector = new RandomSelector(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, "");

		this.numGenSamples = gamerSettings.getIntPropertyValue("ParametersTuner.numGenSamples");

		this.updateAll = gamerSettings.getBooleanPropertyValue("ParametersTuner.updateAll");

		this.numEvalSamples = gamerSettings.getIntPropertyValue("ParametersTuner.numEvalSamples");

		this.numCandidatesToGenerate = gamerSettings.getIntPropertyValue("ParametersTuner.numCandidatesToGenerate");

		this.updateAll = gamerSettings.getBooleanPropertyValue("ParametersTuner.updateAll");

		this.roleProblems = null;

		this.selectedCombinations = null;

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		super.setReferences(sharedReferencesCollector);

		this.randomSelector.setReferences(sharedReferencesCollector);

	}

	@Override
	public void clearComponent() {

		super.clearComponent();

		this.randomSelector.clearComponent();

		this.roleProblems = null;

		this.selectedCombinations = null;

	}

	@Override
	public void setUpComponent() {

		super.setUpComponent();

		this.randomSelector.setUpComponent();

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		this.roleProblems = new LsiProblemRepresentation[numRolesToTune];

		int numSamplesPerValue = this.numGenSamples/this.parametersManager.getTotalNumPossibleValues(); // Same result as if we used floor(this.numGenSamples/this.parametersManager.getTotalNumPossibleValues();)

		// Make sure that we'll have at least one sample per parameter value
		if(numSamplesPerValue == 0){
			numSamplesPerValue = 1;
		}

		//Compute the exact number of generation samples that we need to use. Since all parameters values must be
		// tested for the same amount of times, this number might be lower than the one read from the settings.
		//this.numGenSamples = numSamplesPerValue * this.parametersManager.getTotalNumPossibleValues();

		List<MyPair<CombinatorialCompactMove,Integer>> actionsToTest;

		// For each role for which we are tuning create the corresponding role problem
		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

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

			this.roleProblems[roleProblemIndex] = new LsiProblemRepresentation(actionsToTest, this.parametersManager.getNumPossibleValuesForAllParams(), this.updateAll);
		}

		this.selectedCombinations = new int[numRolesToTune][];

	}

	private int[] randomlyCompleteCombinatorialMove(int paramIndex, int valueIndex){

		int[] combinatorialMove = new int[this.parametersManager.getNumTunableParameters()];
		for(int i = 0; i < combinatorialMove.length; i++){
			if(i == paramIndex){
				combinatorialMove[i] = valueIndex;
			}else{
				combinatorialMove[i] = -1;
			}
		}

		for(int i = 0; i < combinatorialMove.length; i++){
			if(i != paramIndex){
				combinatorialMove[i] = this.randomSelector.selectMove(new MoveStats[0],
						this.parametersManager.getValuesFeasibility(i, combinatorialMove), null, -1);
			}
		}

		return combinatorialMove;

	}

	@Override
	public void setNextCombinations() {

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
			this.selectedCombinations[roleProblemIndex] = this.roleProblems[roleProblemIndex].getNextCombination();
		}

		this.parametersManager.setParametersValues(selectedCombinations);

	}

	@Override
	public void setBestCombinations() {

		this.parametersManager.setParametersValues(this.selectedCombinations);

		this.stopTuning();
	}

	@Override
	public int getNumIndependentCombinatorialProblems() {
		return this.roleProblems.length;
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

			if(this.roleProblems[roleProblemIndex].getPhase() == Phase.EVALUATION && this.roleProblems[roleProblemIndex].getGeneratedCandidatesStats() == null){
				// The generation phase terminated. The candidates for the evaluation phase must be generated.
				this.generateCandidates(roleProblemIndex);
			}

		}
	}

	private void generateCandidates(int roleProblemIndex){

		double avgRewards[][];

		RandomGenerator rg = new Well19937c(); // Use this also for the rest of the player's code?

		CombinatorialCompactMove combinatorialCompactMove;

		Set<CombinatorialCompactMove> generatedCombinations;

		List<CompleteMoveStats> generatedCombinationsStats;

		avgRewards = this.computeAverageRewardsForParamValues(this.roleProblems[roleProblemIndex].getParamsStats());

		generatedCombinations = new HashSet<CombinatorialCompactMove>();

		generatedCombinationsStats = new ArrayList<CompleteMoveStats>();

		// NOTE: the pseudocode in the paper generates up to k combinations but if there are duplicates the total
		// considered combinations are less than k. This means that it is possible for the different roles to complete
		// the evaluation phase at different moments.
		for(int candidateIndex = 0; candidateIndex < this.numCandidatesToGenerate; candidateIndex++){

			combinatorialCompactMove = this.generateCandidate(avgRewards, rg);

			if(generatedCombinations.add(combinatorialCompactMove)){ // Make sure there are no duplicate combinations
				generatedCombinationsStats.add(new CompleteMoveStats(combinatorialCompactMove));
			}

		}

		this.roleProblems[roleProblemIndex].setGeneratedCandidatesStats(generatedCombinationsStats, Math.floorDiv(this.numEvalSamples, (int) Math.ceil(Math.log(this.numCandidatesToGenerate)/Math.log(2.0))));

	}

	private double[][] computeAverageRewardsForParamValues(MoveStats[][] paramValuesStats){

		// For each param value compute the average reward normalized between 0 and 1.
		double[][] avgRewards = new double[paramValuesStats.length][];

		double scoreSum;
		int visits;

		for(int paramIndex = 0; paramIndex < paramValuesStats.length; paramIndex++){

			avgRewards[paramIndex] = new double[paramValuesStats[paramIndex].length];

			for(int valueIndex = 0; valueIndex < paramValuesStats[paramIndex].length; valueIndex++){

				visits = paramValuesStats[paramIndex][valueIndex].getVisits();

				if(visits == 0){
					avgRewards[paramIndex][valueIndex] = 0.0;
				}else{
					scoreSum = paramValuesStats[paramIndex][valueIndex].getScoreSum();
					avgRewards[paramIndex][valueIndex] = (scoreSum/((double)visits))/100.0;
				}

			}

		}

		return avgRewards;
	}

	private CombinatorialCompactMove generateCandidate(double[][] avgRewards, RandomGenerator rg){

		EnumeratedDistribution<MyPair<Integer,Integer>> distribution;
		List<Pair<MyPair<Integer,Integer>,Double>> probabilities;

		MyPair<Integer,Integer> selectedSample;

		boolean[][] feasibility;

		int[] indices = new int[avgRewards.length];
		for(int paramIndex = 0; paramIndex < indices.length; paramIndex++){
			indices[paramIndex] = -1;
		}

		// Compute one of the indices of the combination until all the indices of the combination are set.
		for(int paramIndex = 0; paramIndex < avgRewards.length; paramIndex++){

			feasibility = new boolean[avgRewards.length][];

			// Compute feasibility of all parameter values wrt the current setting of indices
			for(int paramIndex2 = 0; paramIndex2 < avgRewards.length; paramIndex2++){
				if(indices[paramIndex2] == -1){
					feasibility[paramIndex2] = this.parametersManager.getValuesFeasibility(paramIndex2, indices);
				}else{
					feasibility[paramIndex2] = null; // null means that no values are feasible because we already set an index for this param value
				}
			}

			// For each value that is feasible, add the corresponding probability to the list that will be used
			// to generate the samples with the EnumeratedDistribution
			probabilities = new ArrayList<Pair<MyPair<Integer,Integer>,Double>>();

			// Compute feasibility of all parameter values wrt the current setting of indices
			for(int paramIndex2 = 0; paramIndex2 < feasibility.length; paramIndex2++){
				if(feasibility[paramIndex2] != null){
					for(int valueIndex = 0; valueIndex < feasibility[paramIndex].length; valueIndex++){
						if(feasibility[paramIndex][valueIndex]){
							probabilities.add(new Pair<MyPair<Integer,Integer>,Double>(new MyPair<Integer,Integer>(paramIndex, valueIndex), avgRewards[paramIndex][valueIndex]));
						}
					}
				}
			}

			distribution = new EnumeratedDistribution<MyPair<Integer,Integer>>(rg, probabilities);

			selectedSample = distribution.sample();

			indices[selectedSample.getFirst().intValue()] = selectedSample.getSecond().intValue();
		}

		return new CombinatorialCompactMove(indices);

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

				toLog = "\nROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex));

				toLog = globalParamsOrder + ";TESTED_PARAM;";

				List<MyPair<CombinatorialCompactMove,Integer>> combinationsToTest = this.roleProblems[roleProblemIndex].getCombinationsToTest();

				for(MyPair<CombinatorialCompactMove,Integer> combo : combinationsToTest){
					toLog = combo.getFirst() + ";" + combo.getSecond().intValue() + ";";
				}

				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TestedCombos", toLog);

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

				toLog = "";

				List<CompleteMoveStats> globalInfo = this.roleProblems[roleProblemIndex].getGeneratedCandidatesStats();

				CombinatorialCompactMove theValuesIndices;
				String theValues;

				for(CompleteMoveStats stats : globalInfo){

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
				}

				toLog += "\n";

				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "GlobalParamTunerStats", toLog);

			}
		}

	}

	@Override
	public void decreaseStatistics(double factor) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isMemorizingBestCombo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void memorizeBestCombinations() {
		// TODO Auto-generated method stub

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
