package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;



public class InternalPropnetDUCTMCTSNode extends InternalPropnetMCTSNode{

	/**
	 * List of the moves' statistics for each role in the state corresponding to this node.
	 */
	private DUCTMCTSMove[][] moves;

	/**
	 * Number of unexplored moves for each player.
	 */
	private int[] unexploredMovesCount;

	public InternalPropnetDUCTMCTSNode(DUCTMCTSMove[][] moves, int[] goals, boolean terminal) {

		super(goals, terminal);
		this.moves = moves;

		// If this state has legal moves for the players (i.e. is not terminal
		// nor pseudo-terminal), we keep track of the number of not yet visited
		// moves for each player.
		if(moves != null){
			this.unexploredMovesCount = new int[moves.length];

			for(int i = 0; i < moves.length; i++){
				this.unexploredMovesCount[i] = moves[i].length;
			}
		}
	}

	public DUCTMCTSMove[][] getMoves(){
		return this.moves;
	}

	public int[] getUnexploredMovesCount(){
		return this.unexploredMovesCount;
	}



	@Override
	public String toString(){

		String s = "NODE[\n";
		s += "  Moves[";
		if(this.moves == null){
			s += "null]\n";
		}else{
			for(int i = 0; i < this.moves.length; i++){
				s += "\n    Role" + i +"[";
				if(moves[i] == null){
					s += "null]";
				}else{
					for(int j = 0; j < moves[i].length; j++){
						s += "\n      " + moves[i][j].toString();
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
