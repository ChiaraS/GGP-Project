package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover;

import java.util.List;

import org.ggp.base.util.statemachine.proverStructure.ProverMove;

public class ProverSequDecMCTSJointMove extends ProverMCTSJointMove {

	/**
	 * Index that each single move has in the list of legal moves for its role.
	 */
	private int[] movesIndices;

	/**
	 * @param jointMove
	 * @param movesIndices
	 */
	public ProverSequDecMCTSJointMove(List<ProverMove> jointMove, int[] movesIndices) {
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
