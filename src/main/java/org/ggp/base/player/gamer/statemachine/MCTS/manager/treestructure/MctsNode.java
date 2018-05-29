/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

/**
 * @author C.Sironi
 *
 */
public abstract class MctsNode{

	/**
	 * Goal for every role in the state (memorized only if the state corresponding to this tree node is terminal.
	 */
	protected int[] goals;

	/**
	 * True if the state is terminal, false otherwise.
	 */
	protected boolean terminal;

	/**
	 * Number of times this node has been visited. We keep a separate value for each role
	 * because when we decay the statistics, due to the rounding of the visits of each single
	 * move of each role, the sum of the visits of all the moves might be different for each
	 * role.
	 *
	 * E.g. suppose role 0 has moves [a0 b0 c0] with visits [3 6 1] and role 1 has moves
	 * [a1 b1 c1] with visits [4 2 4] and the total number of visits of the node is 10.
	 * For each role, the sum of the visits of each move is also equal to the total number
	 * of node visits. However, if we decay them with decay factor 0.5, for role 0 we
	 * obtain the visits [1.5 3 0.5] that get rounded to the integers [2 3 1], while for role
	 * 1 we obtain the visits [2 1 2] that are already rounded. As can be seen the sum of move
	 * visits are different for the two roles and due to rounding for role 0 this sum is higher
	 * than 10*0.5.
	 */
	protected int[] totVisits;

	/**
	 * Keeps track of the last game turn for which this node was visited.
	 */
	protected int gameStepStamp;

	/**
	 *
	 */
	public MctsNode(int[] goals, boolean terminal, int numRoles) {
		this.goals = goals;
		this.terminal = terminal;
		this.totVisits = new int[numRoles];
		this.gameStepStamp = -1;
	}

	public int[] getGoals(){
		return this.goals.clone();
	}

	public boolean isTerminal(){
		return this.terminal;
	}

	public int[] getTotVisits(){
		return this.totVisits;
	}

	public void incrementTotVisits(){
		for(int i = 0; i < this.totVisits.length; i++){
			this.totVisits[i]++;
		}
	}

	public int getGameStepStamp() {
		return this.gameStepStamp;
	}

	public void setGameStepStamp(int gameStepStamp) {
		this.gameStepStamp = gameStepStamp;
	}

	public abstract void decayStatistics(double decayFactor);

}
