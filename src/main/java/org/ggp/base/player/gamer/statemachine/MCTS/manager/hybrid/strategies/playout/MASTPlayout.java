package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector.EpsilonMASTJointMoveSelector;

public class MASTPlayout extends MovesMemorizingStandardPlayout{

	public MASTPlayout(GameDependentParameters gameDependentParameters, Random random, Properties properties, SharedReferencesCollector sharedReferencesCollector, EpsilonMASTJointMoveSelector epsilonMASTJointMoveSelector){
		super(gameDependentParameters, random, properties, sharedReferencesCollector, epsilonMASTJointMoveSelector);

	}

}
