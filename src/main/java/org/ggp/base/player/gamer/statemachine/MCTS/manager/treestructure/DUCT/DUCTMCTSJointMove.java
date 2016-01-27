/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.util.statemachine.implementation.internalPropnet.structure.InternalPropnetMove;

/**
 * @author C.Sironi
 *
 */
public class DUCTMCTSJointMove extends MCTSJointMove {

	/**
	 * Index that each single move has in the list of legal moves for its role.
	 */
	private int[] movesIndices;

	/**
	 * @param jointMove
	 * @param movesIndices
	 */
	public DUCTMCTSJointMove(List<InternalPropnetMove> jointMove, int[] movesIndices) {
		super(jointMove);
		this.movesIndices = movesIndices;
	}

	public int[] getMovesIndices() {
		return movesIndices;
	}


	@Override
	public String toString(){
		String s = "JointMove[";
		for(int i = 0; i < this.movesIndices.length; i++){
			s += "(I=" + movesIndices[i] + ", M=" + this.jointMove.get(i) +")";
		}

		return s;
	}

}
