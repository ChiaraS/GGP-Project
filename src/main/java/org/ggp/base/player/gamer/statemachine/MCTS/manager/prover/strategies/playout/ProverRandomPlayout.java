package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.jointmoveselector.ProverRandomJointMoveSelector;
import org.ggp.base.util.statemachine.StateMachine;

public class ProverRandomPlayout extends ProverStandardPlayout {

	public ProverRandomPlayout(StateMachine theMachine){
		super(theMachine, new ProverRandomJointMoveSelector(theMachine));
	}

}