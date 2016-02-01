package org.ggp.base.player.gamer.statemachine.MCTS;

public class L4J2SlowDUCTMCTSGamer extends L4J2SlowUCTMCTSGamer {

	/**
	 *
	 */
	public L4J2SlowDUCTMCTSGamer() {
		super();
		this.DUCT = true;
		this.singleGame = false;
	}

}