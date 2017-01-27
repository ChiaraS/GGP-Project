package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class EvoAfterMove extends AfterMoveStrategy {

	private SingleParameterEvolutionManager evolutionManager;

	public EvoAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.evolutionManager = sharedReferencesCollector.getSingleParameterEvolutionManager();
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	@Override
	public String getComponentParameters(String indentation) {
		// Only the component that creates the manager prints its content
		//return indentation + "EVOLUTION_MANAGER = " + this.evolutionManager.printEvolutionManager(indentation + "  ");

		// Here we only print the name
		return indentation + "EVOLUTION_MANAGER = " + this.evolutionManager.getClass().getSimpleName();
	}

	@Override
	public void afterMoveActions() {

		this.evolutionManager.logIndividualsState();

	}

}
