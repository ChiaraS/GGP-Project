package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;

public class ProverMCTSJointMove extends MCTSJointMove{

	/**
	 * The joint move computed by the selection or expansion strategy.
	 */
	protected List<ProverMove> jointMove;

	public ProverMCTSJointMove(List<ProverMove> jointMove) {
		this.jointMove = jointMove;
	}

	public List<ProverMove> getJointMove() {
		return jointMove;
	}

	@Override
	public String toString(){
		String s = "[ ";
		for(ProverMove m : this.jointMove){
			s += m + " ";
		}
		s += " ]";

		return s;
	}

}
