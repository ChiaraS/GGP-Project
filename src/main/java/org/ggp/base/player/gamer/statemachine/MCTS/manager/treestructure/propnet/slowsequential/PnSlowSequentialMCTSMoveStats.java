/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.slowsequential;

import org.ggp.base.player.gamer.statemachine.MCS.manager.propnet.PnCompleteMoveStats;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

/**
 * @author C.Sironi
 *
 */
public class PnSlowSequentialMCTSMoveStats extends PnCompleteMoveStats {

	/**
	 * Index of the move statistics in the list of move statistics of the node
	 * to which this move statistics belongs.
	 */
	private int moveIndex;

	/**
	 * Reference to the parent SUCTMove of this move.
	 */
	private PnSlowSequentialMCTSMoveStats previousRoleMoveStats;

	/**
	 * Reference to the list of SUCTMovesStats for the next role.
	 * This list contains the statistics for all the next role's
	 * moves given that the current role played this move.
	 */
	private PnSlowSequentialMCTSMoveStats[] nextRoleMovesStats;

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
	public PnSlowSequentialMCTSMoveStats(InternalPropnetMove theMove, int moveIndex, PnSlowSequentialMCTSMoveStats[] nextRoleMovesStats) {
		super(theMove);
		this.moveIndex = moveIndex;
		this.previousRoleMoveStats = null;
		this.nextRoleMovesStats = nextRoleMovesStats;

		// If this is not a move of the last role in the tree, set for every next
		// role's move that this move is the parent.
		if(nextRoleMovesStats != null){
			for(int i = 0; i < this.nextRoleMovesStats.length; i++){
				this.nextRoleMovesStats[i].setPreviousRoleMoveStats(this);
			}
		}
	}

	/**
	 * Getter method.
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
	public PnSlowSequentialMCTSMoveStats[] getNextRoleMovesStats(){
		return this.nextRoleMovesStats;
	}

	/**
	 * Get method.
	 *
	 * @return move statistics for the previous role's move.
	 */
	public PnSlowSequentialMCTSMoveStats getPreviousRoleMoveStats(){
		return this.previousRoleMoveStats;
	}

	/**
	 * Set method.
	 *
	 * @param previousRoleMove move statistics for the previous role's move to be set.
	 */
	public void setPreviousRoleMoveStats(PnSlowSequentialMCTSMoveStats previousRoleMoveStats){
		this.previousRoleMoveStats = previousRoleMoveStats;
	}

	@Override
	public String toString(){
		String s = super.toString();
		s += ", PARENT(" + this.previousRoleMoveStats.getTheMove() + "),";
		s += "NEXT_ROLE_MOVES_STATS(";
		for(PnSlowSequentialMCTSMoveStats m : this.nextRoleMovesStats){
			s += "\n      " + m.toString();
		}
		s += ")";

		return s;
	}
}
