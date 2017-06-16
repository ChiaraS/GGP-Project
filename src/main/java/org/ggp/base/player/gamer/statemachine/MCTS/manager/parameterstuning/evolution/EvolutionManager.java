package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ParametersManager;

public abstract class EvolutionManager extends SearchManagerComponent {

	protected ParametersManager parametersManager;

	/**
	 * Size of the populations. It's the same for all roles.
	 */
	protected int populationsSize;

	/**
	 * Number of elite individuals of the population that will be kept when evolving
	 * and used to create new individuals for the population.
	 */
	protected int eliteSize;

	public EvolutionManager(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.populationsSize = gamerSettings.getIntPropertyValue("EvolutionManager.populationsSize");

		this.eliteSize = gamerSettings.getIntPropertyValue("EvolutionManager.eliteSize");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// Do nothing
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	public void setParametersManager(ParametersManager parametersManager){
		this.parametersManager = parametersManager;
	}

	public abstract CompleteMoveStats[] getInitialPopulation();

	/**
	 * Note that this method evolves the population modifying it directly.
	 *
	 * @param population
	 */
	public abstract void evolvePopulation(CompleteMoveStats[] population);

	@Override
	public String getComponentParameters(String indentation) {
		String params = indentation + "PARAMETERS_MANAGER = " + (this.parametersManager != null ? this.parametersManager.getClass().getSimpleName() : "null") +
				indentation + "POPULATION_SIZE = " + this.populationsSize +
				indentation + "ELITE_SIZE = " + this.eliteSize;

		return params;
	}

}
