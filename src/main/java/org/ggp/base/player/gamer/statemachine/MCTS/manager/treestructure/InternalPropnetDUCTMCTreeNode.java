package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;



public class InternalPropnetDUCTMCTreeNode{

	/**
	 * List of the actions' statistics for each role in the state corresponding to this node.
	 */
	private DUCTMove[][] actions;

	/**
	 * Number of unexplored moves for each player.
	 */
	private int[] unexploredMovesCount;

	/**
	 * Goal for every role in the state (memorized only if the state corresponding to this tree node is terminal.
	 */
	private int[] goals;

	private int totVisits;

	public InternalPropnetDUCTMCTreeNode(DUCTMove[][] actions, int[] goals) {
		this.actions = actions;
		this.unexploredMovesCount = new int[actions.length];

		for(int i = 0; i < actions.length; i++){
			this.unexploredMovesCount[i] = actions[i].length;
		}

		this.goals = goals;
		this.totVisits = 0;
	}

	public DUCTMove[][] getActions(){
		return this.actions;
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
}
