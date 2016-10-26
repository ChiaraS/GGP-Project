package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover;

import java.util.List;

import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public class ProverMCTSJointMove{

	/**
	 * The joint move computed by the selection or expansion strategy.
	 */
	protected List<ExplicitMove> jointMove;

	public ProverMCTSJointMove(List<ExplicitMove> jointMove) {
		this.jointMove = jointMove;
	}

	public List<ExplicitMove> getJointMove() {
		return jointMove;
	}

	@Override
	public String toString(){
		String s = "[ ";
		for(ExplicitMove m : this.jointMove){
			s += m + " ";
		}
		s += " ]";

		return s;
	}

}
