/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

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
		this.DUCT = true;
		this.singleGame = false;
	}

}
