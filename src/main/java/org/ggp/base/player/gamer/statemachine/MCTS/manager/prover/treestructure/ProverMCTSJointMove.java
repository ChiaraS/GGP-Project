package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure;

import java.util.List;

import org.ggp.base.util.statemachine.Move;

public class ProverMCTSJointMove {

	/**
	 * The joint move computed by the selection or expansion strategy.
	 */
	protected List<Move> jointMove;

	public ProverMCTSJointMove(List<Move> jointMove) {
		this.jointMove = jointMove;
	}

	public List<Move> getJointMove() {
		return jointMove;
	}

	@Override
	public String toString(){
		String s = "[ ";
		for(Move m : this.jointMove){
			s += m + " ";
		}
		s += " ]";

		return s;
	}

}
