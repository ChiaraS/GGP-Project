package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.jointmoveselector.RandomJointMoveSelector;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;

public class RandomPlayout extends StandardPlayout {

	public RandomPlayout(InternalPropnetStateMachine theMachine){
		super(theMachine, new RandomJointMoveSelector(theMachine));
	}

}
