/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

/**
 * @author C.Sironi
 *
 */
public abstract class InternalPropnetMCTSNode{

	/**
	 * Goal for every role in the state (memorized only if the state corresponding to this tree node is terminal.
	 */
	protected int[] goals;

	/**
	 * True if the state is terminal, false otherwise.
	 */
	protected boolean terminal;

	/**
	 * Number of times this node has been visited.
	 */
	protected int totVisits;

	/**
	 * Keeps track of the last game turn for which this node was visited.
	 */
	protected int gameStepStamp;

	/**
	 *
	 */
	public InternalPropnetMCTSNode(int[] goals, boolean terminal) {
		this.goals = goals;
		this.terminal = terminal;
		this.totVisits = 0;
		this.gameStepStamp = -1;
	}

	public int[] getGoals(){
		return this.goals;
	}

	public boolean isTerminal(){
		return this.terminal;
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
