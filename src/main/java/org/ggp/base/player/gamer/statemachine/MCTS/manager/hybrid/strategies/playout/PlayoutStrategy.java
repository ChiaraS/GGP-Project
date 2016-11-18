package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;

public abstract class PlayoutStrategy extends Strategy {

	public PlayoutStrategy(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	public abstract SimulationResult playout(MachineState state, int maxDepth);

	@Override
	public String printComponent() {
		String params = this.getComponentParameters();

		if(params != null){
			return "[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
