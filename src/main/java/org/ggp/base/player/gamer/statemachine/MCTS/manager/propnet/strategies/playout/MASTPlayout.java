package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector.EpsilonMASTJointMoveSelector;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MASTPlayout extends MemorizedStandardPlayout{

	public MASTPlayout(InternalPropnetStateMachine theMachine, Random random, Map<InternalPropnetMove, MoveStats> mastStatistics, double epsilon) {
		//this.theMachine = theMachine;
		super(theMachine, new EpsilonMASTJointMoveSelector(theMachine, random, mastStatistics, epsilon));

	}

}
