package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector.EpsilonMASTJointMoveSelector;

public class MASTPlayout extends MovesMemorizingStandardPlayout{

	public MASTPlayout(GameDependentParameters gameDependentParameters, EpsilonMASTJointMoveSelector epsilonMASTJointMoveSelector){
		super(gameDependentParameters, epsilonMASTJointMoveSelector);

	}

}
