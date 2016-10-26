package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet;

import java.util.List;

import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public class PnMCTSJointMove{

	/**
	 * The joint move computed by the selection or expansion strategy.
	 */
	protected List<CompactMove> jointMove;

	public PnMCTSJointMove(List<CompactMove> jointMove) {
		this.jointMove = jointMove;
	}

	public List<CompactMove> getJointMove() {
		return jointMove;
	}

	@Override
	public String toString(){
		String s = "[ ";
		for(CompactMove m : this.jointMove){
			s += m.getIndex() + " ";
		}
		s += " ]";

		return s;
	}

}
