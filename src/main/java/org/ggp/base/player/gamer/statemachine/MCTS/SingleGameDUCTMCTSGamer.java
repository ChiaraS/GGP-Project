/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

/**
 * @author C.Sironi
 *
 */
public class SingleGameDUCTMCTSGamer extends UCTMCTSGamer{

	/**
	 *
	 */
	public SingleGameDUCTMCTSGamer() {
		super();
		this.propnetBuild = PROPNET_BUILD.ONCE;
	}

}
