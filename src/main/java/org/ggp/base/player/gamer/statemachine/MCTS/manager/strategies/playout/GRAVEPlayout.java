package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.jointmoveselector.RandomJointMoveSelector;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class GRAVEPlayout extends MemorizedStandardPlayout{

	public GRAVEPlayout(InternalPropnetStateMachine theMachine, List<List<InternalPropnetMove>> allJointMoves) {
		super(theMachine, new RandomJointMoveSelector(theMachine), allJointMoves);
	}

}
