/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

/**
 * @author C.Sironi
 *
 */
public class SingleGameSlowSUCTMCTSGamer extends SlowUCTMCTSGamer{

	/**
	 *
	 */
	public SingleGameSlowSUCTMCTSGamer() {
		super();
		this.DUCT = false;
		this.propnetBuild = PROPNET_BUILD.ONCE;
	}

}
