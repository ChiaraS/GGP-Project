package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;

import inriacmaes.CMAEvolutionStrategy;

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
	private boolean stopped;

	/**
	 *
	 * @param population must correspond to the initial population created by cmaes, BUT with values
	 * for the parameters already rescaled in their feasible values and not in [-inf;+inf].
	 * @param cmaes must be already initialized and ready to be used.
	 */
	public SelfAdaptiveESProblemRepresentation(CMAEvolutionStrategy cmaes, CompleteMoveStats[] population) {

		super(population);

		this.cmaes = cmaes;

		this.stopped = false;

	}

	public CMAEvolutionStrategy getCMAEvolutionStrategy() {
		return this.cmaes;
	}

	public boolean isStopped() {
		return this.stopped;
	}

	public void stop() {
		this.stopped = true;
	}

}
