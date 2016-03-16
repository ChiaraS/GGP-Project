/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.slowsequential;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.PnMCTSNode;

/**
 * @author C.Sironi
 *
 */
public class PnSlowSeqentialMCTSNode extends PnMCTSNode {

	/**
	 * List of the moves' statistics for the state corresponding to this node.
	 * Each entry of the list contains the statistics for one of the moves of the
	 * player that is actually performing the search. Each entry is considered as
	 * the root of the tree that contains at each level the move statistics of one
	 * of the other players.
	 *
	 * NOTE: the moves must be ordered per role starting with the role that is
	 * actually performing the search, so the order is not the same as the default
	 * order used by the state machines. The idea is that the order of the roles is
	 * shifted according to the rule that the role that's performing the search will
	 * be the first role in the tree represented by "moves" (e.g. if we have the roles
	 * [r1 r2 r3] and r2 is the role performing the search, we will have in "moves" an
	 * entry for each move of r2, each of which referencing a list of the statistics of
	 * all the moves of r3, and each entry in each list for r3 will reference a list of
	 * moves for r1).
	 *
	 * movesStats = 	[
	 * 				r2.move1stats
	 * 					<-> r3.move1stats
	 * 						<-> r1.move1stats
	 * 						<-> r1.move2stats
	 * 					<-> r3.move2stats
	 * 						<-> r1.move1stats
	 * 						<-> r1.move2stats
	 * 				r2.move2stats
	 * 					<-> r3.move1stats
	 * 						<-> r1.move1stats
	 * 						<-> r1.move2stats
	 * 					<-> r3.move2stats
	 * 						<-> r1.move1stats
	 * 						<-> r1.move2stats
	 * 			]
	 *
	 * Note that the references in the tree are two-ways. Each move statistics has a reference
	 * both to all the statistics of the moves of the role that comes next and to the statistics
	 * of the move that's been chosen for the previous role.
	 */
	private SlowSequentialMCTSMoveStats[] movesStats;

	/**
	 * List of all the unvisited moves.
	 * This list keeps track of all the leaf nodes of the tree represented by "moves"
	 * that have not been visited yet (i.e. the corresponding moves statistics have 0
	 * visits). The leaves exactly correspond to all the possible joint moves.
	 * TODO: is this ok? So many combinations for some games!
	 */
	private List<SlowSequentialMCTSMoveStats> unvisitedLeaves;

	/**
	 * @param goals
	 * @param terminal
	 */
	public PnSlowSeqentialMCTSNode(SlowSequentialMCTSMoveStats[] movesStats, List<SlowSequentialMCTSMoveStats> unvisitedLeaves, int[] goals, boolean terminal) {
		super(goals, terminal);
		this.movesStats = movesStats;
		this.unvisitedLeaves = unvisitedLeaves;
	}

	public SlowSequentialMCTSMoveStats[] getMovesStats(){
		return this.movesStats;
	}

	public List<SlowSequentialMCTSMoveStats> getUnvisitedLeaves(){
		return this.unvisitedLeaves;
	}

	@Override
	public String toString(){

		String s = "NODE[\n";
		s += "  Moves[";
		if(this.movesStats == null){
			s += "null]\n";
		}else{
			for(int i = 0; i < this.movesStats.length; i++){
				s += "\n    Move" + i + "[";
				s += "\n      " + movesStats[i].toString();
			}
			s += "  ]\n";
		}


		// Unexplored moves count
		s += "  UnvisitedLeaves[";

		for(int i = 0; i < this.unvisitedLeaves.size(); i++){
			s += this.unvisitedLeaves.get(i).getTheMove() + " ";
		}
		s += "]\n";

		// Goals
		s += "  Goals[";
		if(this.goals == null){
			s += "null";
		}else{
			s += " ";
			for(int i = 0; i < this.goals.length; i++){
				s += this.goals[i] + " ";
			}
		}
		s += "]\n";

		// Terminal
		s += "  Terminal=" + this.terminal + "\n";

		// Tot visits
		s += "  TotVisits=" + this.totVisits + "\n";

		// Stamp
		s += "  Stamp=" + this.gameStepStamp + "\n";

		s += "]";

		return s;

	}
}
