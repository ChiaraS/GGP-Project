package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.GraveSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;

public class GraveAfterSimulation extends AfterSimulationStrategy {

	private GraveSelection graveSelection;

	public GraveAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.graveSelection = sharedReferencesCollector.getGraveSelection();
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
	public void afterSimulationActions(SimulationResult simulationResult) {
		this.graveSelection.resetClosestAmafStats();
	}

	@Override
	public String getComponentParameters(String indentation) {
		return null;
	}

}
