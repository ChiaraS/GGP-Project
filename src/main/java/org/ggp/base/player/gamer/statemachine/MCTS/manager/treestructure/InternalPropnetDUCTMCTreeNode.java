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

	private int totVisits;

	/**
	 * Keeps track of the last game turn for which this node was visited.
	 */
	private int gameStepStamp;

	public InternalPropnetDUCTMCTreeNode(DUCTMove[][] moves, int[] goals) {
		this.moves = moves;
		this.unexploredMovesCount = new int[moves.length];

		for(int i = 0; i < moves.length; i++){
			this.unexploredMovesCount[i] = moves[i].length;
		}

		this.goals = goals;
		this.totVisits = 0;
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

	public int getTotVisits(){
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
}
