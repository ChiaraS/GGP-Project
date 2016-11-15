package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice;

import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;

public abstract class MoveChoiceStrategy extends Strategy {

	public MoveChoiceStrategy(GameDependentParameters gameDependentParameters, Random random, Properties properties, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, properties, sharedReferencesCollector);
	}

	public abstract CompleteMoveStats chooseBestMove(MCTSNode initialNode);

	@Override
	public String printComponent() {
		String params = this.getComponentParameters();

		if(params != null){
			return "[MOVE_CHOICE_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[MOVE_CHOICE_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
