package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;



public class InternalPropnetDUCTMCTreeNode{

	/**
	 * List of the moves' statistics for each role in the state corresponding to this node.
	 */
	private DUCTMove[][] moves;

	/**
	 * Number of unexplored moves for each player.
	 */
	private int[] unexploredMovesCount;

	/**
	 * Goal for every role in the state (memorized only if the state corresponding to this tree node is terminal.
	 */
	private int[] goals;

	/**
	 * True if the state is terminal, false otherwise.
	 */
	private boolean terminal;

	private long totVisits;

	/**
	 * Keeps track of the last game turn for which this node was visited.
	 */
	private int gameStepStamp;

	public InternalPropnetDUCTMCTreeNode(DUCTMove[][] moves, int[] goals, boolean terminal) {

		this.moves = moves;

		// If this state has legal moves for the players (i.e. is not terminal),
		// we keep track of the number of not yet visited moves for each player.
		if(moves != null){
			this.unexploredMovesCount = new int[moves.length];

			for(int i = 0; i < moves.length; i++){
				this.unexploredMovesCount[i] = moves[i].length;
			}
		}


		this.goals = goals;
		this.terminal = terminal;
		this.totVisits = 0L;
		this.gameStepStamp = -1;
	}

	public DUCTMove[][] getMoves(){
		return this.moves;
	}

	public int[] getUnexploredMovesCount(){
		return this.unexploredMovesCount;
	}

	public int[] getGoals(){
		return this.goals;
	}

	public boolean isTerminal(){
		return this.terminal;
	}

	public long getTotVisits(){
		return this.totVisits;
	}

	public void incrementTotVisits(){
		this.totVisits++;
	}

	public int getGameStepStamp() {
		return this.gameStepStamp;
	}

	public void setGameStepStamp(int gameStepStamp) {
		this.gameStepStamp = gameStepStamp;
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
