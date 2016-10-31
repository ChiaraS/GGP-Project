package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector.RandomJointMoveSelector;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;

public class GRAVEPlayout extends MovesMemorizingStandardPlayout{

	public GRAVEPlayout(AbstractStateMachine theMachine) {
		super(theMachine, new RandomJointMoveSelector(theMachine));
	}


}
