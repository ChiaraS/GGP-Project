package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.sequential;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;

public class PnSequentialMCTSMoveStats extends MoveStats {

	/**
	 * Reference to the list of SUCTMovesStats for the next role.
	 * This list contains the statistics for all the next role's
	 * moves given that the current role played this move.
	 */
	private PnSequentialMCTSMoveStats[] nextRoleMovesStats;

	/**
	 *  Keeps track of the number of leaves in the moves statistics tree that are descendants
	 *  of this move statistics node and haven't been visited at least once yet.
	 */
	private int unvisitedSubleaves;

	public PnSequentialMCTSMoveStats(PnSequentialMCTSMoveStats[] nextRoleMovesStats) {
		super();
		this.nextRoleMovesStats = nextRoleMovesStats;
		if(this.nextRoleMovesStats == null){
			this.unvisitedSubleaves = 1;
		}else{
			this.unvisitedSubleaves = this.nextRoleMovesStats[0].getUnvisitedSubleaves() * this.nextRoleMovesStats.length;
			// This works because each of the next moves has the same amount of leaves in its descendants
			// and they are all not visited yet.
		}
	}

	/**
	 * Getter method.
	 *
	 * @return move statistics for the next role given this move.
	 */
	public PnSequentialMCTSMoveStats[] getNextRoleMovesStats(){
		return this.nextRoleMovesStats;
	}

	public int getUnvisitedSubleaves(){
		return this.unvisitedSubleaves;
	}

	public void decreaseUnvisitedSubLeaves(){
		if(this.unvisitedSubleaves > 0)
			this.unvisitedSubleaves--;
	}

	@Override
	public String toString(){
		String s = super.toString() + ", UNVISITED_SUBLEAVES(" + this.unvisitedSubleaves + "), ";
		s += "NEXT_ROLE_MOVES_STATS(";
		for(PnSequentialMCTSMoveStats m : this.nextRoleMovesStats){
			s += "\n      " + m.toString();
		}
		s += ")";

		return s;
	}

}
