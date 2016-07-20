package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.jointmoveselector.ProverRandomJointMoveSelector;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;

public class ProverGRAVEPlayout extends ProverMemorizedStandardPlayout {

	public ProverGRAVEPlayout(StateMachine theMachine, List<List<Move>> allJointMoves) {
		super(theMachine, new ProverRandomJointMoveSelector(theMachine), allJointMoves);
		// TODO Auto-generated constructor stub
	}

}
