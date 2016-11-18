package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;

public abstract class MoveChoiceStrategy extends Strategy {

	public MoveChoiceStrategy(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	public abstract CompleteMoveStats chooseBestMove(MctsNode initialNode);

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
