package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;

public class StandardEvolutionManager extends EvolutionManager {

	/**
	 * When evolving the population, a new individual is created by crossover of two random parents
	 * with crossoverProbability, while it's created as mutation of a single parent with probability
	 * (1-crossoverProbability).
	 */
	private double crossoverProbability;

	/**
	 * If creating a new individual with mutation of single parent, for each gene of the individual (i.e.
	 * parameter in the combination) change to a random value with probability geneMutationProbability,
	 * and leave the same value with probability (1-geneMutationProbability).
	 */
	private double geneMutationProbability;


	public StandardEvolutionManager(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.crossoverProbability = gamerSettings.getDoublePropertyValue("EvolutionManager.crossoverProbability");

		this.geneMutationProbability = gamerSettings.getDoublePropertyValue("EvolutionManager.geneMutationProbability");
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

	/**
	 * Returns random initial population with populationSize individuals.
	 */
	@Override
	public CompleteMoveStats[] getInitialPopulation() {

		CompleteMoveStats[] population = new CompleteMoveStats[this.populationsSize];

		List<CombinatorialCompactMove> allLegalCombos = this.parametersManager.getAllLegalParametersCombinations();

		for(int i = 0; i < this.populationsSize; i++){
			population[i] = new CompleteMoveStats(allLegalCombos.get(this.random.nextInt(allLegalCombos.size())));
		}

		return population;
	}

	@Override
	public void evolvePopulation(CompleteMoveStats[] population) {

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

		// For the individuals that we are keeping, reset all statistics.
		for(int i = 0; i < this.eliteSize; i++){
			population[i].resetStats();
		}

		// For other individuals, generate new individual t substitute them and reset also statistics.
		// Keep the first eliteSize best individuals and create new individuals
		// to substitute the ones that are being thrown away.
		for(int i = this.eliteSize; i < population.length; i++){

			if(this.random.nextDouble() < this.crossoverProbability){
				// Create new individual with crossover
				population[i].resetStats(this.crossover(((CombinatorialCompactMove)population[this.random.nextInt(this.eliteSize)].getTheMove()),
						((CombinatorialCompactMove)population[this.random.nextInt(this.eliteSize)].getTheMove())));
			}else{
				// Create new individual with mutation
				population[i].resetStats(this.mutation(((CombinatorialCompactMove)population[this.random.nextInt(this.eliteSize)].getTheMove())));
			}

		}

	}

	/**
	 * IMPLEMENTATION OF UNIFORM CROSSOVER
	 * Given two parents, creates a child where each gene (parameter value) is selected 50% of times
	 * from the first parent and 50% from the second parent.
	 * @param parent1
	 * @param parent2
	 * @return
	 */
	private CombinatorialCompactMove crossover(CombinatorialCompactMove parent1, CombinatorialCompactMove parent2){

		// NOTE! We have to keep in mind that there is a constraint on K and Ref when mutating parameter values.
		// Crossover follows the following idea: for each parameter in a random order, select the value from one
		// of the two parents randomly. If this value makes the combination generated so far infeasible, use the
		// value of the other parent. If this value also makes the combination infeasible, pick for the parameter
		// a random value among the feasible ones.

		int[] parentCombo1 = parent1.getIndices();
		int[] parentCombo2 = parent2.getIndices();
		int[] childCombo = new int[parentCombo1.length];

		List<Integer> order = new ArrayList<Integer>();
		for(int paramIndex = 0; paramIndex < childCombo.length; paramIndex++){
			childCombo[paramIndex] = -1;
			order.add(new Integer(paramIndex));
		}

		Collections.shuffle(order);

		for(Integer paramIndex : order){
			if(this.random.nextDouble() < 0.5){
				// Get value from first parent
				childCombo[paramIndex.intValue()] = parentCombo1[paramIndex.intValue()];

				if(!this.parametersManager.isValid(childCombo)){

					// If combination is invalid, get value from second parent
					childCombo[paramIndex.intValue()] = parentCombo2[paramIndex.intValue()];

					if(!this.parametersManager.isValid(childCombo)){
						// If combination is still invalid, get random feasible value
						childCombo[paramIndex.intValue()] = -1;
						List<Integer> feasibleValues = this.parametersManager.getFeasibleValues(paramIndex.intValue(), childCombo);
						childCombo[paramIndex.intValue()] = feasibleValues.get(this.random.nextInt(feasibleValues.size())).intValue();
					}

				}

			}else{
				// Get value from second parent
				childCombo[paramIndex.intValue()] = parentCombo2[paramIndex.intValue()];

				if(!this.parametersManager.isValid(childCombo)){

					// If combination is invalid, get value from second parent
					childCombo[paramIndex.intValue()] = parentCombo1[paramIndex.intValue()];

					if(!this.parametersManager.isValid(childCombo)){
						// If combination is still invalid, get random feasible value
						childCombo[paramIndex.intValue()] = -1;
						List<Integer> feasibleValues = this.parametersManager.getFeasibleValues(paramIndex.intValue(), childCombo);
						childCombo[paramIndex.intValue()] = feasibleValues.get(this.random.nextInt(feasibleValues.size())).intValue();
					}

				}

			}

		}

		return new CombinatorialCompactMove(childCombo);

	}

	/**
	 * IMPLEMENTATION OF UNIFORM RANDOM MUTATION
	 * Given a parent, creates a child by mutating each of its genes (parameters) with probability geneMutationProbability.
	 * If a gene must mutate, then a random value is selected for it among the feasible values.
	 *
	 * @param parent
	 * @return
	 */
	private CombinatorialCompactMove mutation(CombinatorialCompactMove parent){

		// NOTE! We have to keep in mind that there is a constraint on K and Ref when mutating parameter values.

		int[] parentCombo = parent.getIndices();

		int[] childCombo = new int[parentCombo.length];

		List<Integer> paramIndicesToMutate = new ArrayList<Integer>();

		// First of all check which parameters will have to mutate and which will stay the same.
		// For the parameters that will mutate, initialize the value as -1. Copy the value of the other parameters.
		for(int paramIndex = 0; paramIndex < parentCombo.length; paramIndex++){
			if(this.random.nextDouble() < this.geneMutationProbability){ // Mutate
				childCombo[paramIndex] = -1;
				paramIndicesToMutate.add(new Integer(paramIndex));
			}else{ // Keep value.
				childCombo[paramIndex] = parentCombo[paramIndex];
			}
		}

		// If nothing has to mutate, return.
		if(!paramIndicesToMutate.isEmpty()){
			// Mutate values in a random order (to guarantee more fairness when K and Ref are involved - it's
			// more fair to alternate which one is picked first since they influence each other's values).
			Collections.shuffle(paramIndicesToMutate);

			List<Integer> feasibleValues;

			for(Integer paramIndex : paramIndicesToMutate){
				feasibleValues = this.parametersManager.getFeasibleValues(paramIndex.intValue(), childCombo);
				childCombo[paramIndex.intValue()] = feasibleValues.get(this.random.nextInt(feasibleValues.size())).intValue();
			}
		}

		return  new CombinatorialCompactMove(childCombo);

	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "CROSSOVER_PROBABILITY = " + this.crossoverProbability +
				indentation + "GENE_MUTATION_PROBABILITY = " + this.geneMutationProbability;

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

}
