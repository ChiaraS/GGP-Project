package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;

public class SUCTMCTSMoveStats extends MoveStats {

	private double uct;

	/**
	 * Reference to the list of SUCTMovesStats for the next role.
	 * This list contains the statistics for all the next role's
	 * moves given that the current role played this move.
	 */
	private SUCTMCTSMoveStats[] nextRoleMovesStats;

	/**
	 *  Keeps track of the number of leaves in the moves statistics tree that are descendants
	 *  of this move statistics node and haven't been visited at least once yet.
	 */
	private int unvisitedLeaves;

	public SUCTMCTSMoveStats(SUCTMCTSMoveStats[] nextRoleMovesStats, int unvisitedLeaves) {
		super();
		this.uct = 0.0;
		this.nextRoleMovesStats = nextRoleMovesStats;
		this.unvisitedLeaves = unvisitedLeaves;
	}

	/**
	 * Getter method.
	 *
	 * @return the current UCT value of the move.
	 */
	public double getUct() {
		return this.uct;
	}

	/**
	 * Setter method.
	 *
	 * @param uct the UCT value to be set
	 */
	public void setUct(double uct) {
		this.uct = uct;
	}

	/**
	 * Getter method.
	 *
	 * @return move statistics for the next role given this move.
	 */
	public SUCTMCTSMoveStats[] getNextRoleMovesStats(){
		return this.nextRoleMovesStats;
	}

	public int getUnvisitedLeaves(){
		return this.unvisitedLeaves;
	}

	public void decreaseUnvisitedLeaves(){
		if(this.unvisitedLeaves > 0)
			this.unvisitedLeaves--;
	}

	@Override
	public String toString(){
		return super.toString() + ", UCT(" + this.uct + ")";
	}

}
