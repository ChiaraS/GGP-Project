package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class PnMCTSJointMove extends MCTSJointMove{

	/**
	 * The joint move computed by the selection or expansion strategy.
	 */
	protected List<InternalPropnetMove> jointMove;

	public PnMCTSJointMove(List<InternalPropnetMove> jointMove) {
		this.jointMove = jointMove;
	}

	public List<InternalPropnetMove> getJointMove() {
		return jointMove;
	}

	@Override
	public String toString(){
		String s = "[ ";
		for(InternalPropnetMove m : this.jointMove){
			s += m.getIndex() + " ";
		}
		s += " ]";

		return s;
	}

}
