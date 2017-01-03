package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;

public abstract class BeforeSimulationStrategy extends Strategy {

	public BeforeSimulationStrategy(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	public abstract void beforeSimulationActions();

	@Override
	public String printComponent(String indentation) {
		String params = this.getComponentParameters(indentation);

		if(params != null){
			return this.getClass().getSimpleName() + params;
		}else{
			return this.getClass().getSimpleName();
		}
	}

}
