package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector.RandomJointMoveSelector;

public class GRAVEPlayout extends MovesMemorizingStandardPlayout{

	public GRAVEPlayout(GameDependentParameters gameDependentParameters) {
		super(gameDependentParameters, new RandomJointMoveSelector(gameDependentParameters));
	}


}
