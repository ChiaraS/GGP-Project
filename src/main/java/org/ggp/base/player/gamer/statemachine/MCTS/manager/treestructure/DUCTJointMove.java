package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import java.util.List;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class DUCTJointMove {

	/**
	 * The joint move computed by the selection or expansion strategy.
	 */
	private List<InternalPropnetMove> jointMove;

	/**
	 * Index that each single move has in the list of legal moves for its role.
	 */
	private int[] movesIndices;

	public DUCTJointMove(List<InternalPropnetMove> jointMove, int[] movesIndices) {
		this.jointMove = jointMove;
		this.movesIndices = movesIndices;
	}

	public List<InternalPropnetMove> getJointMove() {
		return jointMove;
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
