package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.DiscreteParametersManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.EvoProblemRepresentation;

public abstract class DiscreteEvolutionManager extends EvolutionManager {

	protected DiscreteParametersManager discreteParametersManager;

	public DiscreteEvolutionManager(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);
		this.discreteParametersManager = sharedReferencesCollector.getDiscreteParametersManager();
	}

	@Override
	public void clearComponent() {
		super.clearComponent();
	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();
	}

	public abstract CompleteMoveStats[] getInitialPopulation();

	/**
	 * Note that this method evolves the population modifying it directly.
	 * This method must modify the given population directly and assume that the size
	 * of the evolved population must remain the same (i.e. for each individual removed
	 * one must be added).
	 *
	 * @param roleProblem
	 */
	public abstract void evolvePopulation(EvoProblemRepresentation roleProblem);

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "PARAMETERS_MANAGER = " + (this.discreteParametersManager != null ? this.discreteParametersManager.getClass().getSimpleName() : "null");

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

}
