package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;

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

	public DecoupledMctsNode(DecoupledMctsMoveStats[][] movesStats, int[] goals, boolean terminal, int numRoles) {

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
		s += "  TotVisits=" + this.totVisits + "\n";

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

}
