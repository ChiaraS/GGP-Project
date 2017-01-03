package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.expansion;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;

public abstract class ExpansionStrategy extends Strategy {

	public ExpansionStrategy(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	public abstract boolean expansionRequired(MctsNode node);

	public abstract MctsJointMove expand(MctsNode node);

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
