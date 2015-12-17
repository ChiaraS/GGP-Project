package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import java.util.List;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;


public class InternalPropnetDUCTMCTreeNode{

	/**
	 * List of all possible joint moves that haven't been visited yet.
	 */
	private List<List<InternalPropnetMove>> unvisitedJointMoves;

	/**
	 * List of the actions' statistics for each role in the state corresponding to this node.
	 */
	private DUCTActionsStatistics[] actionsStatistics;

	/**
	 * Goal for every role in the state (memorized only if the state corresponding to this tree node is terminal.
	 */
	private int[] goals;

	private int totVisits;

	public InternalPropnetDUCTMCTreeNode(List<List<InternalPropnetMove>> unvisitedJointMoves, DUCTActionsStatistics[] actionsStatistics, int[] goals) {
		this.unvisitedJointMoves = unvisitedJointMoves;
		this.actionsStatistics = actionsStatistics;
		this.goals = goals;
		this.totVisits = 0;
	}

	public List<List<InternalPropnetMove>> getUnvisitedJointMoves(){
		return this.unvisitedJointMoves;
	}

	public DUCTActionsStatistics[] getActionsStatistics(){
		return this.actionsStatistics;
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
