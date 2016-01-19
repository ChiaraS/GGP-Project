package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import java.util.List;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MCTSJointMove {

	/**
	 * The joint move computed by the selection or expansion strategy.
	 */
	protected List<InternalPropnetMove> jointMove;

	public MCTSJointMove(List<InternalPropnetMove> jointMove) {
		this.jointMove = jointMove;
	}

	public List<InternalPropnetMove> getJointMove() {
		return jointMove;
	}

}
