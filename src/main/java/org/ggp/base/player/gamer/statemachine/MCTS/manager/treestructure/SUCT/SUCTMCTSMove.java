/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

/**
 * @author C.Sironi
 *
 */
public class SUCTMCTSMove extends MCTSMove {

	/**
	 * Reference to the parent SUCTMove of this move.
	 */
	private SUCTMCTSMove previousPlayerMove;

	/**
	 * Reference to the list of SUCTMoves for the next player.
	 * This list contains the statistics for all the next player's
	 * moves given that the current player played this move.
	 */
	private SUCTMCTSMove[] nextPlayerMoves;

	/**
	 * Initializes a new collection of move statistics.
	 *
	 * @param theMove the move for which this class contains statistics.
	 * @param nextPlayerMoves the list of statistics for the next player's moves,
	 * given that this move has been played by this player. ATTENTION: when
	 * creating a new SUCTMove always make sure that the nextPlayerMoves passed
	 * as input are a copy of the array only belonging to this SUCTMove, otherwise
	 * the statistics will be messed up.
	 */
	public SUCTMCTSMove(InternalPropnetMove theMove, SUCTMCTSMove[] nextPlayerMoves) {
		super(theMove);
		this.nextPlayerMoves = nextPlayerMoves;

		// Set for every next player's move that this move is the parent.
		for(int i = 0; i < this.nextPlayerMoves.length; i++){
			this.nextPlayerMoves[i].setPreviousPlayerMove(this);
		}
	}

	/**
	 * Getter method.
	 *
	 * @return move statistics for the next player given this move.
	 */
	public SUCTMCTSMove[] getNextPlayerMoves(){
		return this.nextPlayerMoves;
	}

	/**
	 * Get method.
	 *
	 * @return move statistics for the previous player's move.
	 */
	public SUCTMCTSMove getPreviousPlayerMove(){
		return this.previousPlayerMove;
	}

	/**
	 * Set method.
	 *
	 * @param previousPlayerMove move statistics for the previous player's move to be set.
	 */
	public void setPreviousPlayerMove(SUCTMCTSMove previousPlayerMove){
		this.previousPlayerMove = previousPlayerMove;
	}

	@Override
	public String toString(){
		String s = super.toString();
		s += "\n      PARENT(" + this.previousPlayerMove.getTheMove() + ")";
		s += "\n      NEXT_MOVES(";
		for(SUCTMCTSMove m : this.nextPlayerMoves){
			s += "\n      " + m.toString();
		}
		s += ")";

		return s;
	}
}
