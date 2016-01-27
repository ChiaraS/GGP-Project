/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.util.statemachine.implementation.internalPropnet.structure.InternalPropnetMove;

/**
 * @author C.Sironi
 *
 */
public class SUCTMCTSJointMove extends MCTSJointMove {

	/**
	 * Reference to the leaf move that allows to reconstruct the joint move.
	 */
	private SUCTMCTSMove leafMove;

	/**
	 * @param jointMove
	 */
	public SUCTMCTSJointMove(List<InternalPropnetMove> jointMove, SUCTMCTSMove leafMove) {
		super(jointMove);
		this.leafMove = leafMove;
	}

	public SUCTMCTSMove getLeafMove(){
		return this.leafMove;
	}

}
