package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.DiscreteParametersManager;
import org.ggp.base.util.logging.GamerLogger;

import csironi.ggp.course.utils.MyPair;

/**
 * This class implements LSI for a single player. It is set up to work as follows:
 * - When the samples reserved for the generation phase are enough to evaluate each value of each
 * parameter, this class follows the LSI algorithm for the generation phase.
 * - When the samples reserved for the generation phase are not enough to evaluate each value of each
 * parameter, the predefined number of candidates for the evaluation phase are generated randomly and
 * used in the evaluation phase.
 * - When the evaluation phase starts,
 * @author c.sironi
 *
 */
public class SimLimitedLsiProblemRepresentation /*extends LsiProblemRepresentation*/{

    public enum Phase{
    	GENERATION, EVALUATION, BEST, STOP
    }

    /***************** Parameters used for logging *************/

    private int actualNumGenSamples;

    private int actualNumEvalSamples;

    /***********************************************************/


    /**
     * Phase of this LSI problem.
     */
    private Phase phase;

    /**
     * Index that keeps track of the next combination to return.
     */
    private int currentIndex;

    /**
     * The parameters manager.
     */
    private DiscreteParametersManager discreteParametersManager;

    private Random random;

    /**
     * Parameters shared by all role problems.
     */
    private ProblemRepParameters problemRepParameters;

    //******************************* For the generation phase ********************************//

	/**
	 * Combinatorial actions (i.e. combinations of parameters) that must be tested during the
	 * generation phase, paired with the index of the only parameter that was not picked randomly.
	 * This allows to update only the statistics of this parameter value with the outcome of the
	 * simulation that tested the combination. An alternative would be to ignore this parameter
	 * index and always update all values with the outcome of the simulation, even if they were
	 * selected randomly to complete the combination.
	 */
	private List<MyPair<CombinatorialCompactMove,Integer>> combinationsToTest;

	/**
	 * For each parameter, a list of statistics, each of which corresponds to a possible
	 * value that can be assigned to that parameter.
	 */
	private MoveStats[][] paramsStats;

    //******************************* For the evaluation phase ********************************//

	/**
	 * Stats of the combinatorial actions (i.e. combinations of parameters) generated during the generation
	 * phase that must be evaluated with sequential halving during the evaluation phase.
	 */
	private List<CompleteMoveStats> generatedCandidatesStats;

	/**
	 * Maximum number of samples that can be taken for each iteration of sequential halving
	 */
	private int maxSamplesPerIteration;

	/**
	 * Number of candidates being considered during the current iteration of the evaluation phase.
	 * Each time we finish evaluating all of them, they will be halved and only the best half of
	 * them will be considered.
	 */
	private int numCandidatesOfCurrentIteration;

	/**
	 * List of indices of the combinations for the evaluation phase in a random order, specifying the order in which the
	 * generated combinations will be evaluated (indices might appear more than once because each sample might need to be
	 * evaluated multiple times).
	 */
	private List<Integer> evalOrder;

	public SimLimitedLsiProblemRepresentation(DiscreteParametersManager discreteParametersManager, Random random, ProblemRepParameters problemRepParameters) {

		this.actualNumGenSamples = 0;

		this.actualNumEvalSamples = 0;

		this.currentIndex = 0;

		this.discreteParametersManager = discreteParametersManager;

		this.random = random;

		this.problemRepParameters = problemRepParameters;

		this.paramsStats = new MoveStats[this.discreteParametersManager.getNumTunableParameters()][];

		for(int paramIndex = 0; paramIndex < paramsStats.length; paramIndex++){
			this.paramsStats[paramIndex] = new MoveStats[this.discreteParametersManager.getNumPossibleValues(paramIndex)];
			for(int valueIndex = 0; valueIndex < paramsStats[paramIndex].length; valueIndex++){
				this.paramsStats[paramIndex][valueIndex] = new MoveStats();
			}
		}

		// If we intentionally don't want to run the generation phase and set the number of sample to 0,
		// numCandidatesToGenerate random candidates will be generated and used to perform sequential halving
		if(this.problemRepParameters.getDynamicNumGenSamples() == 0) {

			this.combinationsToTest = null;

			this.phase = Phase.EVALUATION;

			this.generateRandomCandidates();

			if(this.generatedCandidatesStats.isEmpty()){ // No candidates
				throw new RuntimeException("Constructor - SimLimitedLsiProblemRepresentation generated 0 candidates for the evaluation phase!");
			}

			// Set the parameters needed for the evaluation phase and check if we have enough samples
			this.numCandidatesOfCurrentIteration = this.generatedCandidatesStats.size();
			// Number of iterations in sequential halving (i.e. number of times the candidates will be all evaluated and halved)
			int seqHalvingIterations = ((int) Math.ceil(Math.log(this.numCandidatesOfCurrentIteration)/Math.log(2.0)));

			// Extra check to make sure that everything is right. This should never happen because whenever the number of
			// eval samples is not set (i.e. =-1) the number of generation samples is set to Integer.MAX_VALUE, so the
			// evaluation phase should never start.
			if(this.problemRepParameters.getDynamicNumEvalSamples() == -1) {
				GamerLogger.logError("ParametersTuner", "LsiParametersTuner - Something is wrong with the code, trying to start evaluation phase before having estimated the number of available evaluation samples.");
				throw new RuntimeException("SimLimitedLsiProblemRepresentation - Something is wrong with the code, trying to start evaluation phase before having estimated the number of available evaluation samples.");
			}

			this.maxSamplesPerIteration = Math.floorDiv(this.problemRepParameters.getDynamicNumEvalSamples(), seqHalvingIterations);

			//if(this.maxSamplesPerIteration >= this.numCandidatesOfCurrentIteration) {
			this.currentIndex = 0;
			if(this.numCandidatesOfCurrentIteration > 1){
				this.computeEvalOrder();
			}else if(this.numCandidatesOfCurrentIteration == 1){ // Otherwise we only have one candidate, that is automatically the best
				this.evalOrder = null;
				this.phase = Phase.STOP;
			}
			/*}else {
				// We don't have enough samples to evaluate all candidates at least once, a random one
				// among the generated ones will be picked and set as best.
				Collections.shuffle(this.generatedCandidatesStats);
				this.evalOrder = null;
				this.phase = Phase.BEST;
			}*/
		}else {

			// If we want to perform the generation phase, we will sample each value of each parameter at least
			// once even if we don't have enough samples available for the generation phase.

			this.phase = Phase.GENERATION;

			this.currentIndex = 0;

			this.combinationsToTest = this.createCombinationsToTest();

			this.generatedCandidatesStats = null;

			this.maxSamplesPerIteration = 0;

			this.numCandidatesOfCurrentIteration = 0;

			this.evalOrder = null;
		}

	}

	/**
	 * Creates one combination for each parameter value of all the parameters assigning values of
	 * the other parameters randomly.
	 */
	private List<MyPair<CombinatorialCompactMove,Integer>> createCombinationsToTest() {
		// For each value x of each parameter we generate one sample containing x,
		// completing the parameter combination with random values for the other parameters.
		List<MyPair<CombinatorialCompactMove,Integer>> nextCombinationsToTest = new ArrayList<MyPair<CombinatorialCompactMove,Integer>>();

		for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){
			for(int valueIndex = 0; valueIndex < this.discreteParametersManager.getNumPossibleValues(paramIndex); valueIndex++){
				nextCombinationsToTest.add(new MyPair<CombinatorialCompactMove,Integer>(new CombinatorialCompactMove(this.randomlyCompleteCombinatorialMove(paramIndex,valueIndex)),new Integer(paramIndex)));
			}
		}

		// Randomize order in which the combinations will be tested for each role so that the combinations
		// won't be tested always against the same combination for all the roles.
		Collections.shuffle(nextCombinationsToTest);

		return nextCombinationsToTest;
	}

	private int[] randomlyCompleteCombinatorialMove(int paramIndex, int valueIndex){

		int[] combinatorialMove = new int[this.discreteParametersManager.getNumTunableParameters()];
		for(int i = 0; i < combinatorialMove.length; i++){
			if(i == paramIndex){
				combinatorialMove[i] = valueIndex;
			}else{
				combinatorialMove[i] = -1;
			}
		}

		for(int i = 0; i < combinatorialMove.length; i++){
			if(i != paramIndex){
				combinatorialMove[i] = this.problemRepParameters.getRandomSelector().selectMove(new MoveStats[0],
						this.discreteParametersManager.getValuesFeasibility(i, combinatorialMove), null, -1);
			}
		}

		return combinatorialMove;

	}

	private void generateRandomCandidates(){

		CombinatorialCompactMove combinatorialCompactMove;

		Set<CombinatorialCompactMove> generatedCombinations = new HashSet<CombinatorialCompactMove>();

		this.generatedCandidatesStats = new ArrayList<CompleteMoveStats>();

		List<CombinatorialCompactMove> legalCombos = this.discreteParametersManager.getAllLegalParametersCombinations();

		for(int candidateIndex = 0; candidateIndex < this.problemRepParameters.getNumCandidatesToGenerate(); candidateIndex++){

			combinatorialCompactMove = legalCombos.get(this.random.nextInt(legalCombos.size()));

			if(generatedCombinations.add(combinatorialCompactMove)){ // Make sure there are no duplicate combinations
				this.generatedCandidatesStats.add(new CompleteMoveStats(combinatorialCompactMove));
			}

		}

	}

	public int[] getNextCombination(){

		switch(this.phase){
		case GENERATION:
			return this.combinationsToTest.get(this.currentIndex).getFirst().getIndices();
		case EVALUATION:
			return ((CombinatorialCompactMove) this.generatedCandidatesStats.get(this.evalOrder.get(this.currentIndex)).getTheMove()).getIndices();
		case BEST:
			// Since we are returning the best move, set the algorithm as stopped
			this.phase = Phase.STOP;
			// The best move has been found and is the first one in the list of candidates
			return ((CombinatorialCompactMove) this.generatedCandidatesStats.get(0).getTheMove()).getIndices();
		default:
			// If the phase is STOP, throw exception because this method is supposed to be called only during other phases.
			GamerLogger.logError("ParametersTuner", "LsiParametersTuner - Unrecognized phase of LSI when trying to get next combination to test: " + this.phase + "!");
			throw new RuntimeException("LsiParametersTuner - Unrecognized phase of LSI when trying to get next combination to test: " + this.phase + "!");
		}

	}

	public void updateStatsOfCombination(double reward){

		switch(this.phase){
		case GENERATION:
			MyPair<CombinatorialCompactMove,Integer> theTestedCombo = this.combinationsToTest.get(this.currentIndex);

			int[] theIndices = theTestedCombo.getFirst().getIndices();

			if(this.problemRepParameters.getUpdateAll()){
				for(int pramIndex = 0; pramIndex < theIndices.length; pramIndex++){
					this.paramsStats[pramIndex][theIndices[pramIndex]].incrementScoreSum(reward);
					this.paramsStats[pramIndex][theIndices[pramIndex]].incrementVisits();
				}
			}else{
				int paramIndex = theTestedCombo.getSecond().intValue();
				this.paramsStats[paramIndex][theIndices[paramIndex]].incrementScoreSum(reward);
				this.paramsStats[paramIndex][theIndices[paramIndex]].incrementVisits();
			}

			this.actualNumGenSamples++;

			this.currentIndex++;

			// If we tested all combinations available, generate the next batch of combinations
			// (one for each value of each parameter), and add them to the combinations to test.
			// NOTE: we keep all tested combinations so we can log them all.
			if(this.currentIndex >= this.combinationsToTest.size()) {

				// If we don't have enough generation samples left to test each value of each parameter once more,
				// we start the evaluation phase.
				if(this.currentIndex + this.discreteParametersManager.getTotalNumPossibleValues() > this.problemRepParameters.getDynamicNumGenSamples()){

					this.phase = Phase.EVALUATION;
					this.generateCandidates();

					if(this.generatedCandidatesStats.isEmpty()){ // No candidates
							throw new RuntimeException("SimLimitedLsiProblemRepresentation generated 0 candidates for the evaluation phase!");
					}

					// Set the parameters needed for the evaluation phase and check if we have enough samples
					this.numCandidatesOfCurrentIteration = this.generatedCandidatesStats.size();
					// Number of iterations in sequential halving (i.e. number of times the candidates will be all evaluated and halved)
					int seqHalvingIterations = ((int) Math.ceil(Math.log(this.numCandidatesOfCurrentIteration)/Math.log(2.0)));

					// Extra check to make sure that everything is right. This should never happen because whenever the number of
					// eval samples is not set (i.e. =-1) the number of generation samples is set to Integer.MAX_VALUE, so the
					// evaluation phase should never start.
					if(this.problemRepParameters.getDynamicNumEvalSamples() == -1) {
						GamerLogger.logError("ParametersTuner", "LsiParametersTuner - Something is wrong with the code, trying to start evaluation phase before having estimated the number of available evaluation samples.");
						throw new RuntimeException("SimLimitedLsiProblemRepresentation - Something is wrong with the code, trying to start evaluation phase before having estimated the number of available evaluation samples.");
					}

					this.maxSamplesPerIteration = Math.floorDiv(this.problemRepParameters.getDynamicNumEvalSamples(), seqHalvingIterations);

					//if(this.maxSamplesPerIteration >= this.numCandidatesOfCurrentIteration) {
					this.currentIndex = 0;
					if(this.numCandidatesOfCurrentIteration > 1){
						this.computeEvalOrder();
					}else if(this.numCandidatesOfCurrentIteration == 1){ // Otherwise we only have one candidate, that is automatically the best
						this.evalOrder = null;
						this.phase = Phase.STOP;
					}
					/*}else {
						// We don't have enough samples to evaluate all candidates at least once, a random one
						// among the generated ones will be picked and set as best.
						Collections.shuffle(this.generatedCandidatesStats);
						this.evalOrder = null;
						this.phase = Phase.BEST;
					}*/
				}else{
					this.combinationsToTest.addAll(this.createCombinationsToTest());
				}
			}
			break;
		case EVALUATION:

			this.generatedCandidatesStats.get(this.evalOrder.get(this.currentIndex)).incrementScoreSum(reward);
			this.generatedCandidatesStats.get(this.evalOrder.get(this.currentIndex)).incrementVisits();

			this.actualNumEvalSamples++;

			this.currentIndex++;

			if(this.currentIndex == this.evalOrder.size()){ // All candidates have been tested for the given amount of times
				// We must half the candidates and recompute the order.

				Collections.sort(this.generatedCandidatesStats.subList(0,this.numCandidatesOfCurrentIteration),
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

				this.numCandidatesOfCurrentIteration = (int) Math.ceil((double)this.numCandidatesOfCurrentIteration/2.0);
				this.currentIndex = 0;

				if(this.numCandidatesOfCurrentIteration > 1){
					this.computeEvalOrder();
				}else{ // Otherwise we only have one candidate, that is automatically the best
					this.evalOrder = null;
					this.phase = Phase.BEST;
				}

			}
		default:
			// Nothing must be done
			break;
		}
	}

	private void generateCandidates(){

		double avgRewards[][];

		RandomGenerator rg = new Well19937c(); // Use this also for the rest of the player's code?

		CombinatorialCompactMove combinatorialCompactMove;

		Set<CombinatorialCompactMove> generatedCombinations;

		avgRewards = this.computeAverageRewardsForParamValues();

		generatedCombinations = new HashSet<CombinatorialCompactMove>();

		this.generatedCandidatesStats = new ArrayList<CompleteMoveStats>();

		// NOTE: the pseudocode in the paper generates up to k combinations but if there are duplicates the total
		// considered combinations are less than k. This means that it is possible for the different roles to complete
		// the evaluation phase at different moments.
		for(int candidateIndex = 0; candidateIndex < this.problemRepParameters.getNumCandidatesToGenerate(); candidateIndex++){

			combinatorialCompactMove = this.generateCandidate(avgRewards, rg);

			if(generatedCombinations.add(combinatorialCompactMove)){ // Make sure there are no duplicate combinations
				this.generatedCandidatesStats.add(new CompleteMoveStats(combinatorialCompactMove));
			}

		}

	}

	private double[][] computeAverageRewardsForParamValues(){

		// For each param value compute the average reward normalized between 0 and 1.
		double[][] avgRewards = new double[this.paramsStats.length][];

		double scoreSum;
		int visits;

		for(int paramIndex = 0; paramIndex < this.paramsStats.length; paramIndex++){

			avgRewards[paramIndex] = new double[this.paramsStats[paramIndex].length];

			for(int valueIndex = 0; valueIndex < this.paramsStats[paramIndex].length; valueIndex++){

				visits = this.paramsStats[paramIndex][valueIndex].getVisits();

				if(visits == 0){
					avgRewards[paramIndex][valueIndex] = 0.0;
				}else{
					scoreSum = this.paramsStats[paramIndex][valueIndex].getScoreSum();
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

		boolean nonZeroSum ; // Checks that at least one probability is greater than 0

		// Compute one of the indices of the combination until all the indices of the combination are set.
		for(int count = 0; count < avgRewards.length; count++){

			feasibility = new boolean[avgRewards.length][];

			// Compute feasibility of all parameter values wrt the current setting of indices
			for(int paramIndex = 0; paramIndex < avgRewards.length; paramIndex++){
				if(indices[paramIndex] == -1){
					feasibility[paramIndex] = this.discreteParametersManager.getValuesFeasibility(paramIndex, indices);
				}else{
					feasibility[paramIndex] = null; // null means that no values are feasible because we already set an index for this param value
				}
			}

			// For each value that is feasible, add the corresponding probability to the list that will be used
			// to generate the samples with the EnumeratedDistribution
			probabilities = new ArrayList<Pair<MyPair<Integer,Integer>,Double>>();

			nonZeroSum = false;

			// Compute feasibility of all parameter values wrt the current setting of indices
			for(int paramIndex = 0; paramIndex < feasibility.length; paramIndex++){
				if(feasibility[paramIndex] != null){
					for(int valueIndex = 0; valueIndex < feasibility[paramIndex].length; valueIndex++){
						if(feasibility[paramIndex][valueIndex]){
							if(avgRewards[paramIndex][valueIndex] != 0.0){
								nonZeroSum = true;
							}
							probabilities.add(new Pair<MyPair<Integer,Integer>,Double>(new MyPair<Integer,Integer>(paramIndex, valueIndex), avgRewards[paramIndex][valueIndex]));
						}
					}
				}
			}

			if(nonZeroSum){ // Sum of all probabilities is > 0

				try{
					distribution = new EnumeratedDistribution<MyPair<Integer,Integer>>(rg, probabilities);
				}catch(Exception e){
					String distributionString = "[ ";
					for(Pair<MyPair<Integer,Integer>,Double> p : probabilities){
						distributionString += "(" + p.getFirst().getFirst() + ";" + p.getFirst().getSecond() + ";" + p.getSecond() + ")";
					}
					GamerLogger.logError("ParametersTuner", "LsiParametersTuner-Error when creating distribution: " + distributionString + ".");
					GamerLogger.logStackTrace("ParametersTuner", e);
					throw e;
				}

				selectedSample = distribution.sample();
			}else{
				Pair<MyPair<Integer,Integer>,Double> pair = probabilities.get(rg.nextInt(probabilities.size()));
				selectedSample = pair.getFirst();
			}

			indices[selectedSample.getFirst().intValue()] = selectedSample.getSecond().intValue();
		}

		return new CombinatorialCompactMove(indices);

	}

	public void computeEvalOrder(){

		int samplesPerCombo = Math.floorDiv(this.maxSamplesPerIteration, this.numCandidatesOfCurrentIteration);

		if(samplesPerCombo < 1){
			samplesPerCombo = 1; // Get at least one sample
			//throw new RuntimeException("SimLimitedLsiProblemRepresentation has not enough evaluations samples to evaluate each combo at least once!");
		}

		// Prepare random order of testing for the best elements
		this.evalOrder = new ArrayList<Integer>();

		for(int comboIndex = 0; comboIndex < this.numCandidatesOfCurrentIteration; comboIndex++){
			for(int repetition = 0; repetition < samplesPerCombo; repetition++){
				this.evalOrder.add(new Integer(comboIndex));
			}
		}

		Collections.shuffle(evalOrder);

	}

	/*
	public void setGeneratedCandidatesStats(List<CompleteMoveStats> generatedCandidatesStats, int maxSamplesPerIteration){
		this.generatedCandidatesStats = generatedCandidatesStats;
		this.maxSamplesPerIteration = maxSamplesPerIteration;

		this.numCandidatesOfCurrentIteration = this.generatedCandidatesStats.size();
		this.currentIndex = 0;
		if(this.numCandidatesOfCurrentIteration > 1){
			this.computeEvalOrder();
		}else{ // Otherwise we only have one candidate, that is automatically the best
			this.evalOrder = null;
			this.phase = Phase.STOP;
		}
	}*/

	public int getNumCandidatesOfCurrentIteration(){
		return this.numCandidatesOfCurrentIteration;
	}

	public List<MyPair<CombinatorialCompactMove,Integer>> getCombinationsToTest(){
		return this.combinationsToTest;
	}

	public MoveStats[][] getParamsStats(){
		return this.paramsStats;
	}

	public Phase getPhase(){
		return this.phase;
	}

	public List<CompleteMoveStats> getGeneratedCandidatesStats(){
		return this.generatedCandidatesStats;
	}

	public int getActualNumGenSamples() {
		return this.actualNumGenSamples;
	}

	public int getActualNumEvalSamples() {
		return this.actualNumEvalSamples;
	}

}
