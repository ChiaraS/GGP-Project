/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.sequential;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

/**
 * @author C.Sironi
 *
 */
public class PnSequentialMCTSNode extends MctsNode {

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
	 * be the first role in the tree represented by "movesStats" (e.g. if we have the
	 * roles [r1 r2 r3] and r2 is the role performing the search, we will have in
	 * "movesStats" an entry for each move of r2, each of which referencing a list of
	 * the statistics of all the moves of r3, and each entry in each list for r3 will
	 * reference a list of moves for r1).
	 *
	 * movesStats = 	[
	 * 				r2.move1stats
	 * 					-> r3.move1stats
	 * 						-> r1.move1stats
	 * 						-> r1.move2stats
	 * 					-> r3.move2stats
	 * 						-> r1.move1stats
	 * 						-> r1.move2stats
	 * 				r2.move2stats
	 * 					-> r3.move1stats
	 * 						-> r1.move1stats
	 * 						-> r1.move2stats
	 * 					-> r3.move2stats
	 * 						-> r1.move1stats
	 * 						-> r1.move2stats
	 * 			]
	 *
	 * Note that the references in the tree are one-way. Each move statistics has a reference
	 * to all the statistics of the moves of the role that comes next but not to the statistics
	 * of the move that's been chosen for the previous role.
	 */
	private PnSequentialMCTSMoveStats[] movesStats;

	/**
	 * List of legal moves for each role in this state
	 * (NOTE! The roles are not re-ordered to have the playing role as the first,
	 * but are in the standard order as returned by the state machine).
	 */
	private List<List<CompactMove>> allLegalMoves;

	/**
	 * Number of joint moves that haven't been visited yet from this node.
	 * Note that this corresponds to the number of leaf nodes in the moves
	 * statistics tree that have the visits count equal to 0.
	 */
	private int unvisitedLeaves;

	/**
	 * @param goals
	 * @param terminal
	 */
	public PnSequentialMCTSNode(List<List<CompactMove>> allLegalMoves, PnSequentialMCTSMoveStats[] movesStats, double[] goals, boolean terminal, int unvisitedLeaves, int numRoles) {
		super(goals, terminal,numRoles);
		this.movesStats = movesStats;
		this.allLegalMoves = allLegalMoves;
		this.unvisitedLeaves = unvisitedLeaves;
	}

	public PnSequentialMCTSMoveStats[] getMovesStats(){
		return this.movesStats;
	}

	public List<List<CompactMove>> getAllLegalMoves(){
		return this.allLegalMoves;
	}

	public int getUnvisitedLeaves(){
		return this.unvisitedLeaves;
	}

	public void decreaseUnvisitedLeaves(){
		this.unvisitedLeaves--;
	}

	@Override
	public String toString(){

		return "";

/*		String s = "NODE[\n";
		s += "  Moves[";
		if(this.legalMoves == null){
			s += "null]\n";
		}else{
			for(int i = 0; i < this.legalMoves.length; i++){
				s += "\n    Move" + i + "[";
				s += "\n      " + moves[i].toString();
			}
			s += "  ]\n";
		}


		// Unexplored moves count
		s += "  UnvisitedLeaves[" + this.unvisitedLeaves + "]\n";

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
*/
	}

	@Override
	public void decayStatistics(double decayFactor) {
		// TODO Auto-generated method stub

		// Not implemented cause this class will soon disappear

	}

	@Override
	public int getNumJointMoves() {

		if(this.allLegalMoves == null) {
			return 0;
		}

		int numJointMoves = 1;

		for(int i = 0; i < this.allLegalMoves.size(); i++) {
			numJointMoves *= this.allLegalMoves.get(i).size();
		}

		if(numJointMoves == 0) {
			GamerLogger.logError("MctsNode", "PnSequentialMCTSNode - Detected no legal joint moves for non-terminal node.");
			throw new RuntimeException("PnSequentialMCTSNode - Detected no legal joint moves for non-terminal node.");
		}

		return numJointMoves;
	}
}
