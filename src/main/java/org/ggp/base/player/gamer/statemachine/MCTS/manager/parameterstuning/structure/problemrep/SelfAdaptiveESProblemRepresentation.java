package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep;

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
	 *
	 * @param population must correspond to the initial population created by cmaes, BUT with values
	 * for the parameters already rescaled in their feasible values and not in [-inf;+inf].
	 * @param cmaes must be already initialized and ready to be used.
	 */
	public SelfAdaptiveESProblemRepresentation(CMAEvolutionStrategy cmaes, CompleteMoveStats[] population) {

		super(population);

		this.cmaes = cmaes;

		this.meanValueCombo = null;

	}

	public CMAEvolutionStrategy getCMAEvolutionStrategy() {
		return this.cmaes;
	}

	public CompleteMoveStats getMeanValueCombo() {
		return this.meanValueCombo;
	}

	public void setMeanValueCombo(CompleteMoveStats meanValueCombo) {
		this.meanValueCombo = meanValueCombo;
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
