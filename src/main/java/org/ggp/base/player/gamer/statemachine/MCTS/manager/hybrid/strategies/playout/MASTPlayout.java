package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.jointmoveselector.EpsilonMASTJointMoveSelector;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachinenew.AbstractStateMachine;

public class MASTPlayout extends MovesMemorizingStandardPlayout{

	public MASTPlayout(AbstractStateMachine theMachine, Random random, Map<Move, MoveStats> mastStatistics, double epsilon) {
		//this.theMachine = theMachine;
		super(theMachine, new EpsilonMASTJointMoveSelector(theMachine, random, mastStatistics, epsilon));

	}

}
