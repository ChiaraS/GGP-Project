package org.ggp.base.player.gamer.statemachine.MCTS;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.InternalPropnetMCTSManager.MCTS_TYPE;

public class SUCTMCTSGamer extends UCTMCTSGamer{

	/**
	 *
	 */
	public SUCTMCTSGamer() {
		super();
		this.mctsType = MCTS_TYPE.SUCT;
	}

}
