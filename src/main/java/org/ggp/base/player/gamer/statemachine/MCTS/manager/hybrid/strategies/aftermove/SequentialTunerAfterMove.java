package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion.SequentialTunerBeforeSimulation;

public class SequentialTunerAfterMove extends AfterMoveStrategy {

	private SequentialTunerBeforeSimulation sequentialTunerBeforeSimulation;

	public SequentialTunerAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.sequentialTunerBeforeSimulation = sharedReferencesCollector.getSequentialTunerBeforeSimulation();
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
	public void afterMoveActions() {
		//this.sequentialTunerBeforeSimulation.startTuningNextParameter();
	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "BEFORE_SIMULATION_STRATEGY = " + this.sequentialTunerBeforeSimulation.getClass().getSimpleName();
	}

}
