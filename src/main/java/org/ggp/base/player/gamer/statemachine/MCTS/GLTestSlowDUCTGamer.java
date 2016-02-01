package org.ggp.base.player.gamer.statemachine.MCTS;

import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.StateMachine;

public class GLTestSlowDUCTGamer extends SlowUCTMCTSGamer {

	public GLTestSlowDUCTGamer() {
		super();
		this.DUCT = true;
		this.singleGame = false;
	}

	public GLTestSlowDUCTGamer(InternalPropnetStateMachine theMachine) {
		this();
		this.thePropnetMachine = theMachine;
	}

	@Override
	public StateMachine getInitialStateMachine(){
		return this.thePropnetMachine;
	}

}
