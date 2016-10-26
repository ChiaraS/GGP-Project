package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector.PnEpsilonMASTJointMoveSelector;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public class PnMASTPlayout extends PnMovesMemorizingStandardPlayout{

	public PnMASTPlayout(InternalPropnetStateMachine theMachine, Random random, Map<CompactMove, MoveStats> mastStatistics, double epsilon) {
		//this.theMachine = theMachine;
		super(theMachine, new PnEpsilonMASTJointMoveSelector(theMachine, random, mastStatistics, epsilon));

	}

}
