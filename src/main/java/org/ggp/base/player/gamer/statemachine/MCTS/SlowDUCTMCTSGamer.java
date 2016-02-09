/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

import org.ggp.base.util.statemachine.InternalPropnetStateMachine;

/**
 * Standard (i.e. non single-game) gamer that performs DUCT/MCTS.
 * @author C.Sironi
 *
 */
public class SlowDUCTMCTSGamer extends SlowUCTMCTSGamer {

	/**
	 *
	 */
	public SlowDUCTMCTSGamer() {
		super();
	}

	/**
	 *
	 */
	public SlowDUCTMCTSGamer(InternalPropnetStateMachine thePropnetMachine) {
		super(thePropnetMachine);
	}

}
