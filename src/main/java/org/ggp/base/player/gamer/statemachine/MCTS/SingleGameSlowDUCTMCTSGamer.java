/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

/**
 * @author C.Sironi
 *
 */
public class SingleGameSlowDUCTMCTSGamer extends SlowUCTMCTSGamer{

	/**
	 *
	 */
	public SingleGameSlowDUCTMCTSGamer() {
		super();
		this.DUCT = true;
		this.singleGame = true;
	}

}
