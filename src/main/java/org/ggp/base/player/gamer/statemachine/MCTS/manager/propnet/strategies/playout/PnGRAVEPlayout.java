package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector.PnRandomJointMoveSelector;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;

public class PnGRAVEPlayout extends PnMovesMemorizingStandardPlayout{

	public PnGRAVEPlayout(InternalPropnetStateMachine theMachine) {
		super(theMachine, new PnRandomJointMoveSelector(theMachine));
	}

}
