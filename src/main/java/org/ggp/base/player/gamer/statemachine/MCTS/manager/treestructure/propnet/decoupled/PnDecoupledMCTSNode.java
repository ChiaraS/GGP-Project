package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.decoupled;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;



public class PnDecoupledMCTSNode extends MCTSNode{

	/**
	 * List of the moves' statistics for each role in the state corresponding to this node.
	 *
	 * movesStats[i][j] = j-th move statistics for i-th role.
	 *
	 */
	protected PnDecoupledMCTSMoveStats[][] movesStats;

	/**
	 * Number of unexplored moves for each player.
	 */
	private int[] unexploredMovesCount;

	public PnDecoupledMCTSNode(PnDecoupledMCTSMoveStats[][] movesStats, int[] goals, boolean terminal) {

		super(goals, terminal);
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

	public PnDecoupledMCTSMoveStats[][] getMoves(){
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
		s += "  TotVisits=" + this.totVisits + "\n";

		// Stamp
		s += "  Stamp=" + this.gameStepStamp + "\n";

		s += "]";

		return s;
	}
}
