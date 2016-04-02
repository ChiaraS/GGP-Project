/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.slowsequential;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

/**
 * @author C.Sironi
 *
 */
public class SlowSequentialMCTSJointMove extends MCTSJointMove {

	/**
	 * Reference to the leaf move that allows to reconstruct the joint move.
	 */
	private SlowSequentialMCTSMoveStats leafMove;

	/**
	 * @param jointMove
	 */
	public SlowSequentialMCTSJointMove(List<InternalPropnetMove> jointMove, SlowSequentialMCTSMoveStats leafMove) {
		super(jointMove);
		this.leafMove = leafMove;
	}

	public SlowSequentialMCTSMoveStats getLeafMove(){
		return this.leafMove;
	}

}
