package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.NTuple;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.IncrementalMab;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.EvoProblemRepresentation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.NTupleEvoProblemRepresentation;
import org.ggp.base.util.logging.GamerLogger;

import csironi.ggp.course.utils.MyPair;

public class UcbEvolutionManager extends StandardEvolutionManager {

	/**
	 * Number of candidate individuals that must be generated every time the population must
	 * be evolved. Among these candidates the best ones will be selected to be in the evolved
	 * population. Note that numCandidates must always be greater than or equal to
	 * (populationSize-eliteSize).
	 */
	private int numCandidates;

	/**
	 * Parameters needed to compute the UCB value fo each individual.
	 */
	// C constant
	private double c;
	// First play urgency (i.e. value to be used when there is no statistics for an n-tuple of parameter values).
	// This value must be >= 0 to be considered. When set to a negative value it means that, whenever no statistics
	// are available for an n-tuple of parameter values, then no value at all will be considered in the averaged UCB
	// value for that particular n-tuple.
	private double fpu;



	public UcbEvolutionManager(GameDependentParameters gameDependentParameters,	Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.numCandidates = gamerSettings.getIntPropertyValue("EvolutionManager.numCandidates");

		// numCandidates must always be greater than or equal to (populationSize-eliteSize)
		if(this.numCandidates < this.populationsSize-this.eliteSize){
			 GamerLogger.logError("SearchManagerCreation", "UcbEvolutionManager - Impossible to create UcbEvolutionManager. " +
					 "The number of candidates (numCandidates = " + this.numCandidates +
					 ") must be at least equal to (populationSize-eliteSize = " + (this.populationsSize - this.eliteSize) + ").");
			 throw new RuntimeException("UcbEvolutionManager - Impossible to create UcbEvolutionManager. " +
					 "The number of candidates (numCandidates = " + this.numCandidates +
					 ") must be at least equal to (populationSize-eliteSize = " + (this.populationsSize - this.eliteSize) + ").");
		}

		this.c = gamerSettings.getDoublePropertyValue("EvolutionManager.c");

		this.fpu = gamerSettings.getDoublePropertyValue("EvolutionManager.fpu");
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		super.clearComponent();
	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();
	}

	@Override
	public void evolvePopulation(EvoProblemRepresentation roleProblem) {

		// The type of the role problem must be UcbEvoProblemRepresentation or we won't have the
		// global and local statistics from which to get the UCB value of each combination.
		if(roleProblem instanceof NTupleEvoProblemRepresentation){

			CompleteMoveStats[] population = roleProblem.getPopulation();

			List<MyPair<CombinatorialCompactMove, Double>> candidateIndividuals;

			// If the size of the elite is less than 1, we use all the population to generate the individuals
			// for the next population and then substitute all the old population with the top elements of
			// the new population.
			if(this.eliteSize <= 0){

				// Get the candidate individuals.
				candidateIndividuals = this.generateAndSortCandidateIndividuals((NTupleEvoProblemRepresentation)roleProblem,
						population.length);

				// The candidates are already ordered by decreasing UCB value.
				// Substitute all population with the first populationSize candidates.
				for(int individualIndex = 0; individualIndex < population.length; individualIndex++){
					population[individualIndex].resetStats(candidateIndividuals.get(individualIndex).getFirst());
				}
				FIX: What if there are multiple candidates with the same value but some of them must be excluded?
			}else{
				// Otherwise we use the best eliteSize individuals in the population to generate the individuals
				// for the next population and then substitute all the non-elite individuals in the old population
				// with the top elements of the new population.

				// Sort the individuals depending on their fitness.
				Arrays.sort(population,
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

				// Get the candidate individuals.
				candidateIndividuals = this.generateAndSortCandidateIndividuals((NTupleEvoProblemRepresentation)roleProblem,
						this.eliteSize);

				// For the individuals that we are keeping, reset all statistics.
				for(int individualIndex = 0; individualIndex < this.eliteSize; individualIndex++){
					population[individualIndex].resetStats();
				}

				// The candidates are already ordered by decreasing UCB value.
				// Substitute the remaining individuals in the population with the first (populationSize-eliteSize)
				// candidates.
				for(int individualIndex = this.eliteSize; individualIndex < population.length; individualIndex++){
					population[individualIndex].resetStats(candidateIndividuals.get(individualIndex-this.eliteSize).getFirst());
				}
			}
		}else{
			GamerLogger.logError("EvolutionManager", "UcbEvolutionManager - Impossible to evolve the population " +
					"without global and local statistics to compute the UCB values. Wrong type of role problem: " +
					roleProblem.getClass().getSimpleName() + ".");
			throw new RuntimeException("UcbEvolutionManager - Impossible to evolve the population without global " +
					"and local statistics to compute the UCB values. Wrong type of role problem: " +
					roleProblem.getClass().getSimpleName() + ".");
		}

	}

	/**
	 * This method uses part (numParents) of the individuals of a population to generate numCandidates
	 * new individuals. The generated individuals are also sorted in a decreasing order wrt their average
	 * UCB value. Among the numCandidates generated individuals, a fixed number of individuals
	 * will be selected by the caller of this method to be part of the next population.
	 *
	 * @param population the population with all individuals.
	 * @param numParents the first numParents individuals of the population will be used to generate
	 * numCandidates new individuals.
	 * @param numCandidates number of candidate new individuals to be generated.
	 * @return a list with the generated individuals together with their average UCB value, computed
	 * using the global and local MABs in the problem representation.
	 */
	private List<MyPair<CombinatorialCompactMove, Double>> generateAndSortCandidateIndividuals(NTupleEvoProblemRepresentation roleProblem, int numParents){

		CompleteMoveStats[] population = roleProblem.getPopulation();

		// Candidate individuals for the next population with their UCB value
		Add the population single individual as well?
		List<MyPair<CombinatorialCompactMove, Double>> candidatesWithUcb = new ArrayList<MyPair<CombinatorialCompactMove, Double>>();

		CombinatorialCompactMove newCandidate;

		// Generate numCandidates new individuals using the first numParents individuals in the population
		// (i.e. individuals at positions [0, end numParents) in the population).
		for(int i = 0; i < this.numCandidates; i++){

			if(this.random.nextDouble() < this.crossoverProbability){
				// Create new individual with crossover
				newCandidate = this.crossoverManager.crossover(((CombinatorialCompactMove)population[this.random.nextInt(numParents)].getTheMove()),
						((CombinatorialCompactMove)population[this.random.nextInt(numParents)].getTheMove()));
			}else{
				// Create new individual with mutation
				newCandidate = this.mutationManager.mutation(((CombinatorialCompactMove)population[this.random.nextInt(numParents)].getTheMove()));
			}

			candidatesWithUcb.add(new MyPair<CombinatorialCompactMove, Double>(newCandidate, new Double(this.getAvgUcb(roleProblem, newCandidate))));

		}

		// Sort the candidate individuals in a decreasing order wrt their average UCB value
		Collections.sort(candidatesWithUcb,
				new Comparator<MyPair<CombinatorialCompactMove, Double>>(){

			@Override
			public int compare(MyPair<CombinatorialCompactMove, Double> o1, MyPair<CombinatorialCompactMove, Double> o2) {
				// Sort from largest to smallest
				if(o1.getSecond() > o2.getSecond()){
					return -1;
				}else if(o1.getSecond() < o2.getSecond()){
					return 1;
				}else{
					return 0;
				}
			}

		});

		return candidatesWithUcb;
	}

	/**
	 * Given an individual (i.e. combination of parameter value indices) returns the average UCB value computed
	 * using all the available statistics (i.e. the statistics of the values for each of the n-tuples in the
	 * landscape model).
	 *
	 * @param newCandidate
	 * @return
	 */
	private double getAvgUcb(NTupleEvoProblemRepresentation roleProblem, CombinatorialCompactMove newCandidate){

		double ucbSum = 0;
		int numAveragedValues = 0;

		Map<NTuple,IncrementalMab> landscapeModelForUCBComputation = roleProblem.getLandscapeModelForUCBComputation();

		CombinatorialCompactMove nTupleValues;

		MyPair<MoveStats,Double> stats;

		for(Entry<NTuple,IncrementalMab> nTupleLookupTable : landscapeModelForUCBComputation.entrySet()) {

			nTupleValues = roleProblem.getNTupleValues(newCandidate, nTupleLookupTable.getKey());

			stats = nTupleLookupTable.getValue().getMovesInfo().get(nTupleValues);

			if(stats != null && stats.getFirst().getVisits() > 0 && nTupleLookupTable.getValue().getNumUpdates() > 0) {
				ucbSum += this.computeUcb(stats.getFirst().getScoreSum(), stats.getFirst().getVisits(), nTupleLookupTable.getValue().getNumUpdates());
				numAveragedValues++;
			}else if(this.fpu >= 0) {
				ucbSum += this.fpu;
				numAveragedValues++;
			}

		}

		// If we have no stats for any of the n-tuples involving the parameter values in the newCandidate
		// and this.fpu is set to a negative value, then numAveragedValues=0 and we cannot compute the
		// average UCB value.
		// When this happens we return 0 as the UCB value of the newCandidate.
		// TODO: add this as a parameter that can be specified in the settings?
		if(numAveragedValues == 0) {
			return 0;
		}

		return ucbSum/((double)numAveragedValues);

	}

	// This method expects moveVisits and parentVisits to be greater than 0
	private double computeUcb(double scoreSum, double moveVisits, double parentVisits) {

		double exploitation = this.computeExploitation(scoreSum, moveVisits);
		double exploration = this.computeExploration(moveVisits, parentVisits);

		if(exploitation != -1 && exploration != -1){
			return exploitation + exploration;
		}else{
			GamerLogger.logError("EvolutionManager", "UcbEvolutionManager - Expected visits greater than 0 for moveVisits=" +
					moveVisits + " and for parentVisits=" + parentVisits + ".");
			throw new RuntimeException("UcbEvolutionManager - Expected visits greater than 0 for moveVisits=" +
					moveVisits + " and for parentVisits=" + parentVisits + ".");
		}
	}

	private double computeExploitation(double scoreSum, double moveVisits){
		if(moveVisits == 0.0){
			return -1.0;
		}else{
			return ((scoreSum / moveVisits) / 100.0);
		}
	}

	private double computeExploration(double moveVisits, double parentVisits){
		if(parentVisits != 0.0 && moveVisits != 0.0){
			return (this.c * (Math.sqrt(Math.log(parentVisits)/moveVisits)));
		}else{
			return -1.0;
		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "NUM_CANDIDATES = " + this.numCandidates +
				indentation + "C = " + this.c +
				indentation + "FPU = " + this.fpu;

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

}
