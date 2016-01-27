/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSMove;
import org.ggp.base.util.statemachine.implementation.internalPropnet.structure.InternalPropnetMove;

/**
 * @author C.Sironi
 *
 */
public class SUCTMCTSMove extends MCTSMove {

	/**
	 * Index of the move statistics in the list of move statistics of the node
	 * to which this move statistics belongs.
	 */
	private int moveIndex;

	/**
	 * Reference to the parent SUCTMove of this move.
	 */
	private SUCTMCTSMove previousRoleMove;

	/**
	 * Reference to the list of SUCTMoves for the next role.
	 * This list contains the statistics for all the next role's
	 * moves given that the current role played this move.
	 */
	private SUCTMCTSMove[] nextRoleMoves;

	/**
	 * Initializes a new collection of move statistics.
	 *
	 * @param theMove the move for which this class contains statistics.
	 * @param nextRoleMoves the list of statistics for the next role's moves,
	 * given that this move has been played by this role. ATTENTION: when
	 * creating a new SUCTMove always make sure that the nextRoleMoves passed
	 * as input are a copy of the array only belonging to this SUCTMove, otherwise
	 * the statistics will be messed up.
	 */
	public SUCTMCTSMove(InternalPropnetMove theMove, int moveIndex, SUCTMCTSMove[] nextRoleMoves) {
		super(theMove);
		this.moveIndex = moveIndex;
		this.previousRoleMove = null;
		this.nextRoleMoves = nextRoleMoves;

		// If this is not a move of the last role in the tree, set for every next
		// role's move that this move is the parent.
		if(nextRoleMoves != null){
			for(int i = 0; i < this.nextRoleMoves.length; i++){
				this.nextRoleMoves[i].setPreviousRoleMove(this);
			}
		}
	}

	/**
	 * Gettewr method.
	 *
	 * @return the index of these move statistics in the node to which they belong.
	 */
	public int getMoveIndex(){
		return this.moveIndex;
	}

	/**
	 * Getter method.
	 *
	 * @return move statistics for the next role given this move.
	 */
	public SUCTMCTSMove[] getNextRoleMoves(){
		return this.nextRoleMoves;
	}

	/**
	 * Get method.
	 *
	 * @return move statistics for the previous role's move.
	 */
	public SUCTMCTSMove getPreviousRoleMove(){
		return this.previousRoleMove;
	}

	/**
	 * Set method.
	 *
	 * @param previousRoleMove move statistics for the previous role's move to be set.
	 */
	public void setPreviousRoleMove(SUCTMCTSMove previousRoleMove){
		this.previousRoleMove = previousRoleMove;
	}

	@Override
	public String toString(){
		String s = super.toString();
		s += "\n      PARENT(" + this.previousRoleMove.getTheMove() + ")";
		s += "\n      NEXT_MOVES(";
		for(SUCTMCTSMove m : this.nextRoleMoves){
			s += "\n      " + m.toString();
		}
		s += ")";

		return s;
	}
}
