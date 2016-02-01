package org.ggp.base.player.gamer.statemachine.MCTS;

import org.ggp.base.util.statemachine.L4J2InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.StateMachine;

public class L4J2TestSlowDUCTGamer extends L4J2SlowUCTMCTSGamer {

	public L4J2TestSlowDUCTGamer() {
		super();
		this.DUCT = true;
		this.singleGame = false;
	}

	public L4J2TestSlowDUCTGamer(L4J2InternalPropnetStateMachine theMachine) {
		this();
		this.thePropnetMachine = theMachine;
	}

	@Override
	public StateMachine getInitialStateMachine(){
		return this.thePropnetMachine;
	}

}
