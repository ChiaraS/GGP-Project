package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class UcbEvolutionManager extends StandardEvolutionManager {

	public UcbEvolutionManager(GameDependentParameters gameDependentParameters,	Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// TODO Auto-generated constructor stub
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
	public void evolvePopulation(CompleteMoveStats[] population) {
		// TODO Auto-generated method stub

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
