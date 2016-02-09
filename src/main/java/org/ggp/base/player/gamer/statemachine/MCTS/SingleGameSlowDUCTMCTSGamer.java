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
		this.propnetBuild = PROPNET_BUILD.ONCE;
	}

}
