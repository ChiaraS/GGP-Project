package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.mutation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.DiscreteParametersManager;

public abstract class MutationManager extends SearchManagerComponent {

	protected DiscreteParametersManager discreteParametersManager;

	public MutationManager(GameDependentParameters gameDependentParameters,	Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.discreteParametersManager = sharedReferencesCollector.getDiscreteParametersManager();
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	public abstract CombinatorialCompactMove mutation(CombinatorialCompactMove parent);

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "PARAMETERS_MANAGER = " + (this.discreteParametersManager != null ? this.discreteParametersManager.getClass().getSimpleName() : "null");
	}

}
