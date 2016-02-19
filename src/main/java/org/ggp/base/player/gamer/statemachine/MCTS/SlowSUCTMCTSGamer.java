/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.InternalPropnetMCTSManager.MCTS_TYPE;



/**
 * @author C.Sironi
 *
 */
public class SlowSUCTMCTSGamer extends SlowUCTMCTSGamer {

	/**
	 *
	 */
	public SlowSUCTMCTSGamer() {
		super();
		this.mctsType = MCTS_TYPE.SLOW_SUCT;
	}

	/**
	 *
	 */
	/*
	public SlowSUCTMCTSGamer(InternalPropnetStateMachine thePropnetMachine) {
		super(thePropnetMachine);
		this.DUCT = false;
	}
	*/

}
