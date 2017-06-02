package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.lang.reflect.InvocationTargetException;
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
import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.RandomSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.TimeLimitedLsiProblemRepresentation;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.reflection.ProjectSearcher;

import csironi.ggp.course.utils.MyPair;

public class TimeLimitedLsiParametersTuner extends ParametersTuner {

	//private int numSim;

	/**
	 * True if LSI could terminate execution before the end of the timeout for the last performed game step.
	 */
	private boolean doneForStep;

	/**
	 * Random selector used to select random values for the parameters when completing combinatorial moves.
	 */
	private RandomSelector randomSelector;

	/**
	 * Given the statistics collected so far for each combination, selects the best one among them.
	 */
	private TunerSelector bestCombinationSoFarSelector;

	/**
	 * True if when receiving the reward for a certain combinatorial action we want to use it to update the stats
	 * of all unit actions that form the combinatorial action. False if we want to use it only to update the stats
	 * for the only unit action that wasn't generated randomly when creating the combinatorial action.
	 */
	private boolean updateAll;

	/**
	 * Number of combinations to be generated for the evaluation phase.
	 */
	private int numCandidatesToGenerate;

	/**
	 * Percentage of the total budget (i.e. total time) that will be dedicated to the generation of
	 * candidate combinatorial actions (i.e. combinations of parameters) that will be evaluated in
	 * the subsequent phase. The remaining budget will be used for the evaluation phase.
	 * Must be specified as a number in the interval [0, 1].
	 */
	private double generationPercentage;

	/**
	 * Time in milliseconds by which this tuner must end the generation phase of the LSI algorithm
	 * for the current game step.
	 */
	private long generationTimeout;

	/**
	 * Time in milliseconds by which this tuner must end the generation phase of the LSI algorithm
	 * for the current game step.
	 */
	private long evaluationTimeout;

	/**
	 * Lsi problem representations for each of the roles for which the parameters are being tuned.
	 */
	private TimeLimitedLsiProblemRepresentation[] roleProblems;

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
	 * False if it has been computed by the proper termination of the LSI algorithm. The simulations-limited
	 * LSI algorithm doesn't complete properly if the total number of simulations that it requires is higher
	 * than the number of simulations performed during the whole game. The time-limited LSI algorithm, instead,
	 * might not complete properly because the search manager and the TimeLimitedLsiParametersTuner don't use
	 * the same time management check, but check the time independently. This can cause the manager to stop
	 * the search before the TimeLimitedLsiParametersTuner has actually been able to complete the tuning.
	 */
	private boolean[] isIntermediate;

	public TimeLimitedLsiParametersTuner(GameDependentParameters gameDependentParameters, Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.randomSelector = new RandomSelector(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, "");

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

		this.updateAll = gamerSettings.getBooleanPropertyValue("ParametersTuner.updateAll");

		this.numCandidatesToGenerate = gamerSettings.getIntPropertyValue("ParametersTuner.numCandidatesToGenerate");

		this.generationPercentage = gamerSettings.getDoublePropertyValue("ParametersTuner.generationPercentage");

		this.generationTimeout = -1;

		this.evaluationTimeout = -1;

		this.roleProblems = null;

		this.selectedCombinations = null;

		this.bestCombinations = null;

		this.isIntermediate = null;

		this.doneForStep = false;

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		super.setReferences(sharedReferencesCollector);

		this.randomSelector.setReferences(sharedReferencesCollector);

		this.bestCombinationSoFarSelector.setReferences(sharedReferencesCollector);

	}

	@Override
	public void setUpComponent() {

		super.setUpComponent();

		//this.numSim = 0;

		this.doneForStep = false;

		this.randomSelector.setUpComponent();

		this.bestCombinationSoFarSelector.setUpComponent();

		this.generationTimeout = -1;

		this.evaluationTimeout = -1;

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		if(!this.reuseBestCombos || this.bestCombinations == null || this.bestCombinations.length != numRolesToTune){

			this.roleProblems = new TimeLimitedLsiProblemRepresentation[numRolesToTune];

			// For each role for which we are tuning create the corresponding role problem
			for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
				this.roleProblems[roleProblemIndex] = new TimeLimitedLsiProblemRepresentation(this.parametersManager.getNumPossibleValuesForAllParams(), this.updateAll);
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

		this.randomSelector.clearComponent();

		this.bestCombinationSoFarSelector.clearComponent();

		this.generationTimeout = -1;

		this.evaluationTimeout = -1;

		this.roleProblems = null;

		this.selectedCombinations = null;

		this.doneForStep = false;

	}

	public void startTuningForNewStep(long timeout){

		// The first time will log always false. The first line of the file "LSITermination" should thus be ignored.
		GamerLogger.log(FORMAT.CSV_FORMAT, "LSITermination", "Termination;" + this.doneForStep + ";");
		//GamerLogger.log(FORMAT.CSV_FORMAT, "LSITermination", "Termination;" + this.doneForStep + ";" + this.numSim + ";");

		//this.numSim = 0;

		this.doneForStep = false;

		long currentTime = System.currentTimeMillis();

		if(currentTime >= timeout){ // We already passed the time limit for tuning, so we don't want to tune but leave the already set values for the parameters
			this.generationTimeout = -1;
			this.evaluationTimeout = -1;
		}else{
			this.evaluationTimeout = timeout;
			this.generationTimeout = currentTime + (long) (((double)(timeout-currentTime)) * this.generationPercentage);
		}

	}

	@Override
	public void setNextCombinations() {

		if(this.evaluationTimeout == -1){ // For this game step we are not tuning
			// This happens when the timeout that has been given to the tuner already expired before the start of the tuning,
			// but the timeout for the manager is longer so it runs other simulations and calls this method anyway.
			// If the timeout for the tuner expired, we do nothing and leave the currently set combination of values.
			return;
		}

		boolean changed = false;
		// If we are tuning, check if we are in the generation phase
		if(System.currentTimeMillis() < this.generationTimeout){
			for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
				MyPair<Integer, Integer> toTest = this.roleProblems[roleProblemIndex].getNextParamValueToTest();
				this.selectedCombinations[roleProblemIndex] = this.randomlyCompleteCombinatorialMove(toTest.getFirst(), toTest.getSecond());
				changed = true;
			}
		}else{
			for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
				switch(this.roleProblems[roleProblemIndex].getPhase()){
				case GENERATION:
					List<CompleteMoveStats> candidates = this.roleProblems[roleProblemIndex].getGeneratedCandidatesStats();
					if(candidates == null){
						candidates = this.generateCandidates(roleProblemIndex, this.numCandidatesToGenerate);
					}else{
						// Num of candidates that might not be in the correct order
						int numCandidatesOfLastIteration = this.roleProblems[roleProblemIndex].getNumCandidatesOfCurrentIteration();
						Collections.sort(candidates.subList(0,numCandidatesOfLastIteration),
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
						candidates.subList(candidates.size()/2, candidates.size()).clear(); // Remove the last half elements
						candidates.addAll(this.generateCandidates(roleProblemIndex, this.numCandidatesToGenerate-candidates.size()));
					}
					this.roleProblems[roleProblemIndex].setGeneratedCandidatesStats(candidates, this.evaluationTimeout);
					this.selectedCombinations[roleProblemIndex] = this.roleProblems[roleProblemIndex].getNextCandidateToEvaluate();
					changed = true;
					break;
				case EVALUATION:
					this.selectedCombinations[roleProblemIndex] = this.roleProblems[roleProblemIndex].getNextCandidateToEvaluate();
					changed = true;
					break;
				default:
					break;
				}
			}
		}

		if(changed){
			this.parametersManager.setParametersValues(selectedCombinations);
		}else if(!this.doneForStep){
			this.doneForStep = true;
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

		if(this.evaluationTimeout == -1){ // For this game step we are not tuning
			// This happens when the timeout that has been given to the tuner already expired before the start of the tuning,
			// but the timeout for the manager is longer so it runs other simulations and calls this method anyway.
			// If the timeout for the tuner expired, we do nothing and leave the currently set combination of values.
			return;
		}

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

			switch(this.roleProblems[roleProblemIndex].getPhase()){
			case GENERATION:
				this.roleProblems[roleProblemIndex].updateStatsOfParamValue(this.selectedCombinations[roleProblemIndex], neededRewards[roleProblemIndex]);
				break;
			case EVALUATION: case STOP: // Keep updating stats of best move even after it has been found
				this.roleProblems[roleProblemIndex].updateStatsOfCandidate(neededRewards[roleProblemIndex]);
				break;
			default:
				break;
			}
		}

		//this.numSim++;

	}

	private List<CompleteMoveStats> generateCandidates(int roleProblemIndex, int numCamdidates){

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
		for(int candidateIndex = 0; candidateIndex < numCamdidates; candidateIndex++){

			combinatorialCompactMove = this.generateCandidate(avgRewards, rg);

			if(generatedCombinations.add(combinatorialCompactMove)){ // Make sure there are no duplicate combinations
				generatedCombinationsStats.add(new CompleteMoveStats(combinatorialCompactMove));
			}

		}

		return generatedCombinationsStats;

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

		//this.logAvgRewards(avgRewards);

		return avgRewards;
	}

	/*
	private void logAvgRewards(double[][] avgRewards){
		String s = "";
		for(int i = 0; i < avgRewards.length; i++){
			for(int j = 0; j < avgRewards[i].length; j++){
				s += (avgRewards[i][j] + ";");
			}
			s += "\n";
		}

		s +="\n";

		GamerLogger.log(FORMAT.CSV_FORMAT, "AvgRewards", s);
	}*/

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
		for(int count = 0; count < indices.length; count++){

			feasibility = new boolean[avgRewards.length][];

			// Compute feasibility of all parameter values wrt the current setting of indices
			for(int paramIndex = 0; paramIndex < avgRewards.length; paramIndex++){
				if(indices[paramIndex] == -1){
					feasibility[paramIndex] = this.parametersManager.getValuesFeasibility(paramIndex, indices);
				}else{
					feasibility[paramIndex] = null; // null means that no values are feasible because we already set an index for this param value
				}
			}

			// For each value that is feasible, add the corresponding probability to the list that will be used
			// to generate the samples with the EnumeratedDistribution
			probabilities = new ArrayList<Pair<MyPair<Integer,Integer>,Double>>();

			nonZeroSum = false;

			// Compute probability of all parameter values
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
	public int getNumIndependentCombinatorialProblems() {
		return this.roleProblems.length;
	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "RANDOM_SELECTOR = " + this.randomSelector.printComponent(indentation + "  ") +
				indentation + "BEST_COMBINATION_SO_FAR_SELECTOR = " + this.bestCombinationSoFarSelector.printComponent(indentation + "  ") +
				indentation + "UPDATE_ALL = " + this.updateAll +
				indentation + "NUM_CANDIDATES_TO_GENERATE = " + this.numCandidatesToGenerate +
				indentation + "GENERATION_PERCENTAGE = " + this.generationPercentage +
				indentation + "GENERATION_TIMEOUT = " + this.generationTimeout +
				indentation + "EVALUATION_TIMEOUT = " + this.evaluationTimeout +
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


	/*
	public static void main(String args[]){
		RandomGenerator rg = new Well19937c();
		EnumeratedDistribution<String> distribution;
		List<Pair<String,Double>> probabilities = new ArrayList<Pair<String,Double>>();
		probabilities.add(new Pair<String,Double>("A", 1.0));
		probabilities.add(new Pair<String,Double>("B", 1.0));
		probabilities.add(new Pair<String,Double>("C", 1.0));
		probabilities.add(new Pair<String,Double>("D", 1.0));
		distribution = new EnumeratedDistribution<String>(rg, probabilities);
		Map<String,Integer> map = new HashMap<String,Integer>();
		String selectedSample;
		Integer numSamples;
		for(int i = 0; i < 10000; i++){

			selectedSample = distribution.sample();

			numSamples = map.get(selectedSample);

			if(numSamples == null){
				numSamples = new Integer(0);
			}

			map.put(selectedSample, new Integer(numSamples.intValue()+1));
		}

		for(Entry<String, Integer> e : map.entrySet()){
			System.out.println(e.getKey() + " = " + e.getValue());
		}
	}*/

	public static void main(String args[]){
		List<String> a = new ArrayList<String>();
		a.add("A");
		a.add("L");
		a.add("B");
		a.add("E");
		a.add("R");
		a.add("T");
		a.add("I");
		a.add("N");
		a.add("O");

		List<String> b = new ArrayList<String>();
		b.add("E");
		b.add("L");
		b.add("L");
		b.add("O");

		System.out.print("a =");
		for(String s : a){
			System.out.print(" " + s);
		}
		System.out.println();
		System.out.println();

		a.subList(5, 9).clear();

		System.out.println(a.size());

		a.addAll(b);

//		List<String> c = new ArrayList(a.subList(0, 5));
	//	c.addAll(b);

		System.out.print("a =");
		for(String s : a){
			System.out.print(" " + s);
		}
		System.out.println();
		System.out.println();

		System.out.print("b =");
		for(String s : b){
			System.out.print(" " + s);
		}
		System.out.println();
		System.out.println();

		/*System.out.print("c =");
		for(String s : c){
			System.out.print(" " + s);
		}
		System.out.println();
		System.out.println();
*/

	}

}
