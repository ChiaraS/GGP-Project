/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

/**
 * @author C.Sironi
 *
 */
public class SingleGameSlowDUCTMCTSGamer extends SingleGameSlowUCTMCTSGamer {

	/**
	 *
	 */
	public SingleGameSlowDUCTMCTSGamer() {
		super();
		this.DUCT = true;
	}

}
