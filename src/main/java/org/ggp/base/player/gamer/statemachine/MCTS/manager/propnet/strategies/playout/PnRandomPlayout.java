package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector.PnRandomJointMoveSelector;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;

public class PnRandomPlayout extends PnStandardPlayout {

	public PnRandomPlayout(InternalPropnetStateMachine theMachine){
		super(theMachine, new PnRandomJointMoveSelector(theMachine));
	}

}
