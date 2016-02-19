/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.InternalPropnetMCTSManager.MCTS_TYPE;

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
		this.mctsType = MCTS_TYPE.SLOW_SUCT;
		this.propnetBuild = PROPNET_BUILD.ONCE;
	}

}
