package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid;

import org.ggp.base.util.statemachine.structure.Move;

public class MctsMove {

	/**
	 * The move.
	 */
	private Move theMove;

	/**
	 * The index of the statistics of the move in the list of move statistics of the
	 * node for which this move has been selected.
	 */
	private int moveIndex;

	public MctsMove(Move theMove, int moveIndex) {
		this.theMove = theMove;
		this.moveIndex = moveIndex;
	}

	public Move getMove() {
		return this.theMove;
	}

	public int getMovesIndex() {
		return this.moveIndex;
	}

	@Override
	public String toString(){
		return "(M=" + this.theMove + ",I=" + this.moveIndex + ")";
	}


}
