package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector.EpsilonMASTJointMoveSelector;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;

public class MASTPlayout extends MovesMemorizingStandardPlayout{

	public MASTPlayout(AbstractStateMachine theMachine, EpsilonMASTJointMoveSelector epsilonMASTJointMoveSelector) {
		//this.theMachine = theMachine;
		super(theMachine, epsilonMASTJointMoveSelector);

	}

}
