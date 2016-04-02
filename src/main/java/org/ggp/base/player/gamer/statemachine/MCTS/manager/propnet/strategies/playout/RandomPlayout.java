package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector.RandomJointMoveSelector;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;

public class RandomPlayout extends StandardPlayout {

	public RandomPlayout(InternalPropnetStateMachine theMachine){
		super(theMachine, new RandomJointMoveSelector(theMachine));
	}

}
