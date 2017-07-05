package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.crossover;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ParametersManager;

public abstract class CrossoverManager extends SearchManagerComponent {

	protected ParametersManager parametersManager;

	public CrossoverManager(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.parametersManager = sharedReferencesCollector.getParametersManager();
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	public abstract CombinatorialCompactMove crossover(CombinatorialCompactMove parent1, CombinatorialCompactMove parent2);

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "PARAMETERS_MANAGER = " + (this.parametersManager != null ? this.parametersManager.getClass().getSimpleName() : "null");
	}

}
