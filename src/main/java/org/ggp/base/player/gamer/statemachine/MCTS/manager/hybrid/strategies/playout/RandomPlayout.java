package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector.RandomJointMoveSelector;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;

public class RandomPlayout extends StandardPlayout {

	public RandomPlayout(AbstractStateMachine theMachine){
		super(theMachine, new RandomJointMoveSelector(theMachine));
	}


}
