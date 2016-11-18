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
	public String printComponent() {
		String params = this.getComponentParameters();

		if(params != null){
			return "[BEFORE_SIM_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[BEFORE_SIM_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
