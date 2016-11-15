package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies;

import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;


public abstract class Strategy extends SearchManagerComponent{

	public Strategy(GameDependentParameters gameDependentParameters, Random random, Properties properties, SharedReferencesCollector sharedReferencesCollector){
		super(gameDependentParameters, random, properties, sharedReferencesCollector);
	}

}
