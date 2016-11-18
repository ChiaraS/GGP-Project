package org.ggp.base.player.gamer.statemachine.MCTS.manager;

public abstract class MctsManager {

	/**
	 * Maximum depth that the MCTS algorithm must visit.
	 */
	protected int maxSearchDepth;

	/**
	 * Number of performed iterations.
	 */
	protected int iterations;

	/**
	 * Number of all visited states since the start of the search.
	 */
	protected int visitedNodes;

	/**
	 * Number of visited nodes in the current iteration so far.
	 */
	protected int currentIterationVisitedNodes;

	/**
	 * Start time of last performed search.
	 */
	protected long searchStart;

	/**
	 * End time of last performed search.
	 */
	protected long searchEnd;

	public int getIterations(){
		return this.iterations;
	}

	public int getVisitedNodes(){
		return this.visitedNodes;
	}

	public long getSearchTime(){
		return (this.searchEnd - this.searchStart);
	}


}
