package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public abstract class EvolutionManager extends SearchManagerComponent {

	public EvolutionManager(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// TODO Auto-generated method stub
	}

	@Override
	public void clearComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpComponent() {
		// TODO Auto-generated method stub

	}

	public abstract CompleteMoveStats[] getInitialPopulation();

	public abstract CompleteMoveStats[] evolvePopulation(CompleteMoveStats[] population);

	@Override
	public String getComponentParameters(String indentation) {
		// TODO Auto-generated method stub
		return null;
	}

}
