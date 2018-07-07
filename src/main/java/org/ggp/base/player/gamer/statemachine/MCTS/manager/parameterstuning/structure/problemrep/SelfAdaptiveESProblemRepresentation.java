package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;

import inriacmaes.CMAEvolutionStrategy;

/**
 * This class can have 3 different states identified by the values assigned to the variables
 * "population" and "meanValueCombo"
 *
 * First state: CMA-ES is active and still optimizing the function
 * - population != null
 * - meanValueCombo == null
 *
 * Second state: CMA-ES has done optimizing the function, but the fitness of the mean value
 * still needs to be computed (note that the mean value is expected to be the one with the
 * highest fitness)
 * - population == null
 * - meanValueCombo != null
 *
 * Last state: CMA-ES has done optimizing the function, the fitness of the mean value has
 * been updated and the best value could be computed (note that this can be the same as the
 * mean value if the mean value was indeed the one with the best fitness)
 * - population == null
 * - meanValueCombo == null
 *
 * @author c.sironi
 *
 */
public class SelfAdaptiveESProblemRepresentation extends EvoProblemRepresentation {

	/**
	 * The CMA-ES evolution strategy that takes care of evolving the population of parameter
	 * values for the game role associated with this problem representation.
	 */
	private CMAEvolutionStrategy cmaes;

	/**
	 * For each individual in the population, for each of its parameters, true if the parameter
	 * has been repaired, false if it has its original value.
	 */
	private boolean[][] repaired;

	/**
	 * Penalty that will be added to the fitness of the repaired individuals.
	 */
	private double[] penalty;

	/**
	 * True if CMA-ES stopped execution for this role problem. If true, the population of this
	 * role problem shouldn't be accessed anymore and the best value found by CMA-ES should be
	 * used instead for the remaining simulations.
	 */
	//private boolean stopped;

	/**
	 * Combination corresponding to the mean value of the distribution computed by CMA-ES.
	 */
	private CompleteMoveStats meanValueCombo;

	/**
	 * For each parameter in the meanValueCombo, true if the parameter has been repaired,
	 * false if it has its original value.
	 */
	private boolean[] meanRepaired;

	/**
	 * Penalty that will be added to the fitness of the repaired individuals.
	 */
	private double meanPenalty;

	/**
	 * Unordered list of indices of the individuals in the population.
	 * Each index appears only once in a random order.
	 * Used to keep track of which individual we must evaluate next.
	 */
	private List<Integer> unorderedIndividualsIndices;

	/**
	 * Index that keeps track of the population individual currently being evaluated.
	 * More precisely, it points to the entry in the array unorderedIndividualsIndices
	 * that corresponds to the index of the individual in the population that we are
	 * current;y evaluating.
	 */
	private int currentIndex;

	/**
	 * Counts the number of times the population has been evaluated.
	 * (i.e. Number of times we shuffled the order of the individuals indices and evaluated
	 * each individual once).
	 */
	private int evalRepetitionsCount;

	/**
	 *
	 * @param population must correspond to the initial population created by cmaes, BUT with values
	 * for the parameters already rescaled in their feasible values and not in [-inf;+inf].
	 * @param cmaes must be already initialized and ready to be used.
	 */
	public SelfAdaptiveESProblemRepresentation(CMAEvolutionStrategy cmaes, CompleteMoveStats[] population,
			boolean[][] repaired, double[] penalty) {

		super(population);

		this.setIteratorForNewPopulation();

		this.cmaes = cmaes;

		this.meanValueCombo = null;

		this.repaired = repaired;

		this.penalty = penalty;

	}

	public void setPopulation(CompleteMoveStats[] population, boolean[][] repaired, double[] penalty){
		this.population = population;
		if(population != null) {
			this.setIteratorForNewPopulation();
		}else {
			this.unorderedIndividualsIndices = null;
			this.currentIndex = -1;
			this.evalRepetitionsCount = 0;
		}
		this.repaired = repaired;
		this.penalty = penalty;
	}

	private void setIteratorForNewPopulation() {
		this.unorderedIndividualsIndices = new ArrayList<Integer>();
		for(int individualIndex = 0; individualIndex < this.population.length; individualIndex++) {
			this.unorderedIndividualsIndices.add(new Integer(individualIndex));
		}
		this.evalRepetitionsCount = 0;
		Collections.shuffle(this.unorderedIndividualsIndices);
		this.currentIndex = -1;
	}

	public CompleteMoveStats advanceToNextIndividual(int evalRepetitions) {
		this.currentIndex++;
		if(this.currentIndex >= this.unorderedIndividualsIndices.size()) {
			this.evalRepetitionsCount++;
			// Reached number of predefined repetitions (i.e. all individuals have been evaluated at least evalRepetitions times)
			if(this.evalRepetitionsCount >= evalRepetitions) {
				return null;
			}
			Collections.shuffle(this.unorderedIndividualsIndices);
			this.currentIndex = 0;
		}
		return this.getCurrentIndividual();
	}

	public CompleteMoveStats getCurrentIndividual() {
		return this.population[this.currentIndex];
	}

	public int getEvalRepetitionsCount() {
		return this.evalRepetitionsCount;
	}

	public CMAEvolutionStrategy getCMAEvolutionStrategy() {
		return this.cmaes;
	}

	public CompleteMoveStats getMeanValueCombo() {
		return this.meanValueCombo;
	}

	public void setMeanValueCombo(CompleteMoveStats meanValueCombo, boolean[] meanRepaired, double meanPenalty) {
		this.meanValueCombo = meanValueCombo;
		this.meanRepaired = meanRepaired;
		this.meanPenalty = meanPenalty;
	}

	public boolean[][] getRepaired(){
		return this.repaired;
	}

	public double[] getPenalty(){
		return this.penalty;
	}

	public boolean[] getMeanRepaired(){
		return this.meanRepaired;
	}

	public double getMeanPenalty(){
		return this.meanPenalty;
	}

	/*
	public boolean isStopped() {
		return this.stopped;
	}*/

	/*
	public void stop() {
		this.stopped = true;
	}*/



}
