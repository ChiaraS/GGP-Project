/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;


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
		this.singleGame = false;
	}

}
