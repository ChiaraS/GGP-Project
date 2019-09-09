package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;

public class DecoupledMctsNode extends MctsNode {

	/**
	 * List of the moves' statistics for each role in the state corresponding to this node.
	 *
	 * movesStats[i][j] = j-th move statistics for i-th role.
	 *
	 * TODO: refactor to use MAB structure to represent statistics.
	 *
	 */
	protected DecoupledMctsMoveStats[][] movesStats;

	/**
	 * Number of unexplored moves for each player.
	 */
	private int[] unexploredMovesCount;

	public DecoupledMctsNode(DecoupledMctsMoveStats[][] movesStats, double[] goals, boolean terminal, int numRoles) {

		super(goals, terminal, numRoles);
		this.movesStats = movesStats;

		// If this state has legal moves for the players (i.e. is not terminal
		// nor pseudo-terminal), we keep track of the number of not yet visited
		// moves for each player.
		if(movesStats != null){
			this.unexploredMovesCount = new int[movesStats.length];

			for(int i = 0; i < movesStats.length; i++){
				this.unexploredMovesCount[i] = movesStats[i].length;
			}
		}
	}

	public DecoupledMctsMoveStats[][] getMoves(){
		return this.movesStats;
	}

	public int[] getUnexploredMovesCount(){
		return this.unexploredMovesCount;
	}



	@Override
	public String toString(){

		String s = "NODE[\n";
		s += "  Moves[";
		if(this.movesStats == null){
			s += "null]\n";
		}else{
			for(int i = 0; i < this.movesStats.length; i++){
				s += "\n    Role" + i +"[";
				if(movesStats[i] == null){
					s += "null]";
				}else{
					for(int j = 0; j < movesStats[i].length; j++){
						s += "\n      " + movesStats[i][j].toString();
					}
					s += "\n    ]\n";
				}
			}
			s += "  ]\n";
		}


		// Unexplored moves count
		s += "  UnexploredMovesCount[";

		if(this.unexploredMovesCount == null){
			s += "null";
		}else{
			s += " ";
			for(int i = 0; i < this.unexploredMovesCount.length; i++){
				s += this.unexploredMovesCount[i] + " ";
			}
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
		s += "  TotVisits=[";
		if(this.totVisits == null){
			s += "null";
		}else{
			s += " ";
			for(int i = 0; i < this.totVisits.length; i++){
				s += this.totVisits[i] + " ";
			}
		}
		s += "]\n";

		// Stamp
		s += "  Stamp=" + this.gameStepStamp + "\n";

		s += "]";

		return s;
	}

	@Override
	public void decayStatistics(double decayFactor) {

		// If there are moves stats, decay each of them.
		if(this.movesStats != null){

			if(decayFactor != 1.0){

				for(int i = 0; i < this.movesStats.length; i++){

					this.totVisits[i] = 0;

					for(int j = 0; j < this.movesStats[i].length; j++){
						int visitsBeforeDecay = this.movesStats[i][j].getVisits();
						this.movesStats[i][j].decreaseByFactor(decayFactor);
						this.totVisits[i] += this.movesStats[i][j].getVisits();
						// If the move had been visited but after decaying the number of visits goes to 0
						// we must increase by 1 the number of unvisited moves
						if(this.movesStats[i][j].getVisits() == 0 && visitsBeforeDecay > 0){
							this.unexploredMovesCount[i]++;
						}
					}
				}
			}
		}

	}

	/**
	 * This method enables to retrieve the legal moves for each role without computing them with the state machine,
	 * because they are already memorized in the node.
	 *
	 * @return
	 */
	public List<List<Move>> getAllLegalMoves(){

		List<List<Move>> legalMoves = new ArrayList<List<Move>>();

		for(int roleIndex = 0; roleIndex < this.movesStats.length; roleIndex++) {
			legalMoves.add(this.getLegalMovesForRole(roleIndex));
		}

		return legalMoves;
	}

	/**
	 * This method enables to retrieve the legal moves for each role without computing them with the state machine,
	 * because they are already memorized in the node.
	 *
	 * @return
	 */
	public List<Move> getLegalMovesForRole(int roleIndex){

		List<Move> legalMovesForRole = new ArrayList<Move>();
		for(DecoupledMctsMoveStats moveStats : this.movesStats[roleIndex]) {
			legalMovesForRole.add(moveStats.getTheMove());
		}

		return legalMovesForRole;
	}

	@Override
	public int getNumJointMoves() {

		if(this.movesStats == null) {
			return 0;
		}

		int numJointMoves = 1;

		for(int i = 0; i < this.movesStats.length; i++) {
			numJointMoves *= this.movesStats[i].length;
		}

		if(numJointMoves == 0) {
			GamerLogger.logError("MctsNode", "DecoupledMctsNode - Detected no legal joint moves for non-terminal node.");
			throw new RuntimeException("DecoupledMctsNode - Detected no legal joint moves for non-terminal node.");
		}

		return numJointMoves;
	}

}
