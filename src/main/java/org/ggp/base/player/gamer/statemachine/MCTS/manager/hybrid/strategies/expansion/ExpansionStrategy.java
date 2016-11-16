package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.expansion;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;

public abstract class ExpansionStrategy extends Strategy {

	public ExpansionStrategy(GameDependentParameters gameDependentParameters, Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);
	}

	public abstract boolean expansionRequired(MCTSNode node);

	public abstract MCTSJointMove expand(MCTSNode node);

	@Override
	public String printComponent() {
		String params = this.getComponentParameters();

		if(params != null){
			return "[EXPANSION_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[EXPANSION_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
