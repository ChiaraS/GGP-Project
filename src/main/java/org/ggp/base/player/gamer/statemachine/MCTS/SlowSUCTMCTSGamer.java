/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

import org.ggp.base.util.statemachine.InternalPropnetStateMachine;


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
		this.DUCT = false;
	}

	/**
	 *
	 */
	public SlowSUCTMCTSGamer(InternalPropnetStateMachine thePropnetMachine) {
		super(thePropnetMachine);
		this.DUCT = false;
	}

}
