package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

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
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

import csironi.ggp.course.utils.MyPair;

public class LSIParametersTuner extends TwoPhaseParametersTuner {

    public enum Phase{
    	GENERATION, EVALUATION, STOP
    }

    /**
     * Phase of LSI.
     */
    private Phase phase;

	/**
	 * Number of samples (i.e. simulations) that will be dedicated to the generation of
	 * candidate combinatorial actions (i.e. combinations of parameters) that will be
	 * evaluated in the subsequent phase.
	 */
	private int numGenSamples;

	/**
	 * Number of samples (i.e. simulations) that will be dedicated to the evaluation of
	 * the generated combinatorial actions (i.e. combinations of parameters) before
	 * committing to a single combinatorial action (i.e. combination of parameters).
	 */
	private int numEvalSamples;

	/**
	 * Number of combinations to be created for the evaluation phase.
	 */
	private int numCandidatesToEval;

	/**
	 * Used to count the number of taken samples during different phases of the tuning
	 * (i.e. during the generation phase and each iteration of sequential halving in the
	 * evaluation phase).
	 */
	private int samplesCounter;

	/**
	 * Used to count the number of iterations of sequential halving.
	 * The maximum number will be floor(log_2(numGeneratedCandidates)).
	 */
	private int sequentialHalvingIteration;

	/**
	 * Number of samples per sequential halving step (for each iteration of sequential halving must be
	 * divided among the combinations to be evaluated).
	 */
	private int samplesPerIteration;

	/**
	 * True if when receiving the reward for a certain combinatorial action we want to use it to update the stats
	 * of all unit actions that form the combinatorial action. False if we want to use it only to update the stats
	 * for the only unit action that wasn't generated randomly when creating the combinatorial action.
	 */
	private boolean updateAll;

	/**
	 * Lsi problem representations for each of the roles for which the parameters are being tuned.
	 */
	private LsiProblemRepresentation[] roleProblems;

	/**
	 * Random selector used to select random values for the parameters when completing combinatorial moves.
	 */
	private RandomSelector randomSelector;

	public LSIParametersTuner(GameDependentParameters gameDependentParameters, Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// TODO Auto-generated constructor stub
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
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpComponent() {

		super.setUpComponent();

		this.randomSelector.setUpComponent();

		this.samplesCounter = 0;

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		this.roleProblems = new LsiProblemRepresentation[numRolesToTune];

		int numSamplesPerValue = this.numGenSamples/this.parametersManager.getTotalNumPossibleValues(); // Same result as if we used floor(this.numGenSamples/this.parametersManager.getTotalNumPossibleValues();)

		// Make sure that we''ll have at least one sample per parameter value
		if(numSamplesPerValue == 0){
			numSamplesPerValue = 1;
		}

		//Compute the exact number of generation samples that we need to use. Since all parameters values must be
		// tested for the same amount of times, this number might be lower than the one read from the settings.
		this.numGenSamples = numSamplesPerValue * this.parametersManager.getTotalNumPossibleValues();

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

			this.roleProblems[roleProblemIndex] = new LsiProblemRepresentation(actionsToTest, this.parametersManager.getNumPossibleValuesForAllParams());
		}

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

		if(this.phase == Phase.GENERATION){
			if(this.samplesCounter < this.numGenSamples){ // Continue generation phase
				this.generationPhase();
			}else{ // Generate candidates and start evaluation phase
				this.generateCandidates();
				this.samplesCounter = 0;
				this.sequentialHalvingIteration = 0;
				this.samplesPerIteration = Math.floorDiv(this.numEvalSamples, (int) Math.ceil(Math.log(this.numCandidatesToEval)/Math.log(2.0)));
				this.phase = Phase.EVALUATION;
			}
		}

		if(this.phase == Phase.EVALUATION){
			if(this.samplesCounter == 0){ //Start of sequentialHalving iteration
				if(this.sequentialHalvingIteration == Math.ceil(Math.log(this.numCandidatesToEval)/Math.log(2.0))){
					this.setBestCombinations();
					this.phase = Phase.STOP;
				}else{
					this.prepareSequentialHalvingIteration();
				}
			}
			this.evaluationPhase();
		}

	}

	private void generationPhase(){

		int[][] nextCombinations = new int[this.roleProblems.length][];

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			if(this.roleProblems[roleProblemIndex].getCombinationsToTest().size() <= this.samplesCounter){
				GamerLogger.logError("ParametersTuner", "LsiParametersTuner - Error! All the combinatorial action to test during the generation phase have been tested. The evaluation phase should have started!");
				throw new RuntimeException("LsiParametersTuner - All the combinatorial action to test during the generation phase have been tested. The evaluation phase should have started!");
			}

			nextCombinations[roleProblemIndex] = this.roleProblems[roleProblemIndex].getCombinationsToTest().get(this.samplesCounter).getFirst().getIndices();

		}

		this.parametersManager.setParametersValues(nextCombinations);

	}

	private void generateCandidates(){

		double avgRewards[][];

		RandomGenerator rg = new Well19937c(); // Use this also for the rest of the player's code?

		//CombinatorialCompactMove combinatorialCompactMove;

		//Set<CombinatorialCompactMove> generatedCombinations;

		List<CompleteMoveStats> generatedCombinationsStats;

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			avgRewards = this.computeAverageRewardsForParamValues(this.roleProblems[roleProblemIndex].getParamsStats());

			//generatedCombinations = new HashSet<CombinatorialCompactMove>();

			generatedCombinationsStats = new ArrayList<CompleteMoveStats>();

			// TODO: the pseudocode in the paper generates up to k combinations but if there are duplicates the total
			// considered combinations are less than k. Here instead we generate exactly k combinations keeping duplicates.
			// This is done to ensure that each role has the same amount of combinations to test, otherwise the different
			// roles won't finish tuning at the same time. Another way of ensuring that they will finish at the same time
			// and proceed at the same pace is to keep generating new combinations until we get k distinct ones, but this
			// can potentially end in an infinite loop if no new combinations are created that are distinct from previous ones.
			for(int candidateIndex = 0; candidateIndex < this.numCandidatesToEval; candidateIndex++){

				//combinatorialCompactMove = this.generateCandidate(avgRewards, rg);

				//if(generatedCombinations.add(combinatorialCompactMove)){ // Make sure there are no duplicate combinations
					//generatedCombinationsStats.add(new CompleteMoveStats(combinatorialCompactMove));
				//}

				generatedCombinationsStats.add(new CompleteMoveStats(this.generateCandidate(avgRewards, rg)));

			}

			this.roleProblems[roleProblemIndex].setGeneratedCombinationsStats(generatedCombinationsStats);

		}

	}

	private void prepareSequentialHalvingIteration(){

		int numCombosToTest = (int) Math.ceil(this.roleProblems[0].getGeneratedCombinationsStats().size() / (Math.pow(2, this.sequentialHalvingIteration)));

		int samplesPerCombo = Math.floorDiv(this.samplesPerIteration, numCombosToTest);

		List<Integer> evalOrder;

		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			//If it's not the first iteration, we need to order the stats from highest to lowest
			if(this.sequentialHalvingIteration > 0){
				Collections.sort(this.roleProblems[roleProblemIndex].getGeneratedCombinationsStats().subList(0,
						(int) Math.ceil(this.roleProblems[0].getGeneratedCombinationsStats().size() / (Math.pow(2, this.sequentialHalvingIteration-1)))),
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
			}

			// Prepare random order of testing for the best elements
			evalOrder = new ArrayList<Integer>();

			for(int comboIndex = 0; comboIndex < numCombosToTest; comboIndex++){
				for(int repetition = 0; repetition < samplesPerCombo; repetition++){
					evalOrder.add(new Integer(comboIndex));
				}
			}

			Collections.shuffle(evalOrder);

			this.roleProblems[roleProblemIndex].setEvalOrder(evalOrder);

		}

	}

	private void evaluationPhase(){

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
	public void setBestCombinations() {
		// TODO Auto-generated method stub

		this.stopTuning();
	}

	@Override
	public int getNumIndependentCombinatorialProblems() {
		// TODO Auto-generated method stub
		return 0;
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

		MoveStats toUpdate;

		if(this.phase == Phase.GENERATION){

			for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

				MyPair<CombinatorialCompactMove,Integer> theTestedCombo = this.roleProblems[roleProblemIndex].getCombinationsToTest().get(this.samplesCounter);

				int[] theIndices = theTestedCombo.getFirst().getIndices();

				if(this.updateAll){
					for(int pramIndex = 0; pramIndex < theIndices.length; pramIndex++){
						toUpdate = this.roleProblems[roleProblemIndex].getParamsStats()[pramIndex][theIndices[pramIndex]];

						toUpdate.incrementScoreSum(neededRewards[roleProblemIndex]);
						toUpdate.incrementVisits();
					}
				}else{
					int paramIndex = theTestedCombo.getSecond().intValue();
					toUpdate = this.roleProblems[roleProblemIndex].getParamsStats()[paramIndex][theIndices[paramIndex]];

					toUpdate.incrementScoreSum(neededRewards[roleProblemIndex]);
					toUpdate.incrementVisits();
				}
			}

			this.samplesCounter++;
		}else if(this.phase == Phase.EVALUATION){



			this.samplesCounter = (this.samplesCounter+1)%this.samplesPerIteration; // TODO: fix this to exact nm of samples per iteration that depends on num of combos
			if(this.samplesCounter == 0){
				this.sequentialHalvingIteration++;
			}
		}

	}

	@Override
	public void logStats() {
		// TODO Auto-generated method stub

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
}
