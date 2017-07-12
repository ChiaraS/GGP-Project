package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.EvoProblemRepresentation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.UcbEvoProblemRepresentation;
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
	// First play urgency (i.e. value to be used when there is no statistics for a parameter value or a
	// parameter value combination).
	private double fpu;



	public UcbEvolutionManager(GameDependentParameters gameDependentParameters,	Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.numCandidates = gamerSettings.getIntPropertyValue("");

		// numCandidates must always be greater than or equal to (populationSize-eliteSize)
		if(this.numCandidates < this.populationsSize-this.eliteSize){
			 GamerLogger.logError("SearchManagerCreation", "UcbEvolutionManager - Impossible to create UcbEvolutionManager. " +
					 "The number of candidates (numCandidates = " + this.numCandidates +
					 ") must be at least equal to (populationSize-eliteSize = " + (this.populationsSize - this.eliteSize) + ").");
			 throw new RuntimeException("UcbEvolutionManager - Impossible to create UcbEvolutionManager. " +
					 "The number of candidates (numCandidates = " + this.numCandidates +
					 ") must be at least equal to (populationSize-eliteSize = " + (this.populationsSize - this.eliteSize) + ").");
		}

		this.c = gamerSettings.getDoublePropertyValue("");

		this.fpu = gamerSettings.getDoublePropertyValue("");
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
		if(roleProblem instanceof UcbEvoProblemRepresentation){

			CompleteMoveStats[] population = roleProblem.getPopulation();

			List<MyPair<CombinatorialCompactMove, Double>> candidateIndividuals;

			// If the size of the elite is less than 1, we use all the population to generate the individuals
			// for the next population and then substitute all the old population with the top elements of
			// the new population.
			if(this.eliteSize <= 0){

				// Get the candidate individuals.
				candidateIndividuals = this.generateAndSortCandidateIndividuals((UcbEvoProblemRepresentation)roleProblem,
						population.length);

				// The candidates are already ordered by decreasing UCB value.
				// Substitute all population with the first populationSize candidates.
				for(int individualIndex = 0; individualIndex < population.length; individualIndex++){
					population[individualIndex].resetStats(candidateIndividuals.get(individualIndex).getFirst());
				}
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
				candidateIndividuals = this.generateAndSortCandidateIndividuals((UcbEvoProblemRepresentation)roleProblem,
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
	private List<MyPair<CombinatorialCompactMove, Double>> generateAndSortCandidateIndividuals(UcbEvoProblemRepresentation roleProblem, int numParents){

		CompleteMoveStats[] population = roleProblem.getPopulation();

		// Candidate individuals for the next population with their UCB value
		List<MyPair<CombinatorialCompactMove, Double>> candidatesWithUcb = new ArrayList<MyPair<CombinatorialCompactMove, Double>>();

		CombinatorialCompactMove newCandidate;

		// Generate numCandidates new individuals using the first endIndex individuals in the population
		// (i.e. individuals at positions [0, end Index) in the population).
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
	 * using all the available statistics (i.e. the statistics of the combination in the global MAB and the
	 * statistics of each parameter value in the local MABs).
	 *
	 * @param newCandidate
	 * @return
	 */
	private double getAvgUcb(UcbEvoProblemRepresentation roleProblem, CombinatorialCompactMove newCandidate){

		double ucbSum = 0;

		MoveStats stats;

		// Sum UCB value of the whole combination from the global MAB
		if(roleProblem.getGlobalMab().getMovesInfo().containsKey(newCandidate)){
			stats = roleProblem.getGlobalMab().getMovesInfo().get(newCandidate).getFirst();
			ucbSum += this.computeUcb(stats.getScoreSum(), stats.getVisits(), roleProblem.getGlobalMab().getNumUpdates());
		}else{
			ucbSum += this.fpu;
		}

		// Sum UCB value of each single parameter value in the combination from the local MABs
		int[] valueIndices = newCandidate.getIndices();
		for(int paramIndex = 0; paramIndex < roleProblem.getLocalMabs().length; paramIndex++){
			stats = roleProblem.getLocalMabs()[paramIndex].getMoveStats()[valueIndices[paramIndex]];
			ucbSum += this.computeUcb(stats.getScoreSum(), stats.getVisits(), roleProblem.getLocalMabs()[paramIndex].getNumUpdates());
		}

		return ucbSum/(roleProblem.getLocalMabs().length + 1);

	}

	private double computeUcb(double scoreSum, double moveVisits, double parentVisits) {

		double exploitation = this.computeExploitation(scoreSum, moveVisits);
		double exploration = this.computeExploration(moveVisits, parentVisits);

		if(exploitation != -1 && exploration != -1){
			return exploitation + exploration;
		}else{
			return this.fpu;
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

		String params = indentation + "CROSSOVER_PROBABILITY = " + this.crossoverProbability +
				indentation + "CROSSOVER_MANAGER = " + this.crossoverManager.printComponent(indentation + "  ") +
				indentation + "MUTATION_MANAGER = " + this.mutationManager.printComponent(indentation + "  ");

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

}
