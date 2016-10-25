/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.slowsequential;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnMCTSJointMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

/**
 * @author C.Sironi
 *
 */
public class PnSlowSequentialMCTSJointMove extends PnMCTSJointMove {

	/**
	 * Reference to the leaf move that allows to reconstruct the joint move.
	 */
	private PnSlowSequentialMCTSMoveStats leafMove;

	/**
	 * @param jointMove
	 */
	public PnSlowSequentialMCTSJointMove(List<InternalPropnetMove> jointMove, PnSlowSequentialMCTSMoveStats leafMove) {
		super(jointMove);
		this.leafMove = leafMove;
	}

	public PnSlowSequentialMCTSMoveStats getLeafMove(){
		return this.leafMove;
	}

}
