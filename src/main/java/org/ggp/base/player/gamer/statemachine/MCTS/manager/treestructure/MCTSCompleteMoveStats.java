package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MCTSCompleteMoveStats extends MCTSMoveStats {

	/**
	 * The move associated with these statistics.
	 */
	protected InternalPropnetMove theMove;

	public MCTSCompleteMoveStats(InternalPropnetMove theMove) {
		this.theMove = theMove;
	}

	public MCTSCompleteMoveStats(long visits, long scoreSum, double uct, InternalPropnetMove theMove) {
		super(visits, scoreSum, uct);
		this.theMove = theMove;
	}

	/**
	 * Getter method.
	 *
	 * @return the move associated to these statistics.
	 */
	public InternalPropnetMove getTheMove() {
		return this.theMove;
	}

}
