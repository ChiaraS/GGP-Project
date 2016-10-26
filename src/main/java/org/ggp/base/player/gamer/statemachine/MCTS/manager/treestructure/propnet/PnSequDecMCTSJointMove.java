/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet;

import java.util.List;

import org.ggp.base.util.statemachine.structure.compact.CompactMove;

/**
 * @author C.Sironi
 *
 */
public class PnSequDecMCTSJointMove extends PnMCTSJointMove {

	/**
	 * Index that each single move has in the list of legal moves for its role.
	 */
	private int[] movesIndices;

	/**
	 * @param jointMove
	 * @param movesIndices
	 */
	public PnSequDecMCTSJointMove(List<CompactMove> jointMove, int[] movesIndices) {
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
