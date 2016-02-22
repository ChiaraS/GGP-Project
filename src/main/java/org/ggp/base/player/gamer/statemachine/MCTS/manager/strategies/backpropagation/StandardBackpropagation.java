package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.UCTMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.DUCTMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.InternalPropnetDUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.InternalPropnetSUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.SUCTMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SlowSUCT.InternalPropnetSlowSUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SlowSUCT.SlowSUCTMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SlowSUCT.SlowSUCTMCTSMoveStats;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class StandardBackpropagation implements BackpropagationStrategy {

	/**
	 * The total number of roles in the game.
	 * Needed by the SUCT version of MCTS.
	 */
	private int numRoles;

	/**
	 * The role that is actually performing the search.
	 * Needed by the SUCT version of MCTS.
	 */
	private InternalPropnetRole myRole;

	public StandardBackpropagation(int numRoles, InternalPropnetRole myRole){
		this.numRoles = numRoles;
		this.myRole = myRole;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.BackpropagationStrategy#update(org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode, org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove, int[])
	 */
	@Override
	public void update(InternalPropnetMCTSNode node, MCTSJointMove jointMove, int[] goals){
		if(node instanceof InternalPropnetDUCTMCTSNode && jointMove instanceof UCTMCTSJointMove){
			this.ductUpdate((InternalPropnetDUCTMCTSNode)node, (UCTMCTSJointMove)jointMove, goals);
		}else if(node instanceof InternalPropnetSUCTMCTSNode && jointMove instanceof UCTMCTSJointMove){
			this.suctUpdate((InternalPropnetSUCTMCTSNode)node, (UCTMCTSJointMove)jointMove, goals);
		}else if(node instanceof InternalPropnetSlowSUCTMCTSNode && jointMove instanceof SlowSUCTMCTSJointMove){
			this.ssuctUpdate((InternalPropnetSlowSUCTMCTSNode)node, (SlowSUCTMCTSJointMove)jointMove, goals);
		}else{
			throw new RuntimeException("StandardBackpropagation-update(): detected wrong combination of types for node (" + node.getClass().getSimpleName() + ") and joint move (" + jointMove.getClass().getSimpleName() + ").");
		}
	}

	/**
	 * Backpropagation, DECOUPLED version.
	 * This method backpropagates the goal values, updating for each role the score
	 * and the visits of the move that was selected to be part of the joint move
	 * independently of what other roles did.
	 *
	 * @param node the node for which to update the moves.
	 * @param jointMove the explored joint move.
	 * @param goals the goals obtained by the simulation, to be used to update the statistics.
	 */
	private void ductUpdate(InternalPropnetDUCTMCTSNode node, UCTMCTSJointMove jointMove, int[] goals) {

		node.incrementTotVisits();

		DUCTMCTSMoveStats[][] moves = node.getMoves();

		int[] moveIndices = jointMove.getMovesIndices();

		for(int i = 0; i < moves.length; i++){
			// Get the DUCTMove
			DUCTMCTSMoveStats theMoveToUpdate = moves[i][moveIndices[i]];
			theMoveToUpdate.incrementScoreSum(goals[i]);
			if(theMoveToUpdate.getVisits() == 0){
				node.getUnexploredMovesCount()[i]--;
			}
			theMoveToUpdate.incrementVisits();
		}
	}

	/**
	 * Backpropagation, SEQUENTIAL version.
	 *
	 *
	 * This method backpropagates the goal values, updating for each role the score
	 * and the visits of the move that was selected to be part of the joint move.
	 * This method updates only the statistics of a move depending on the moves selected
	 * in the joint move for the roles that precede this move in the moves statistics tree.
	 *
	 * @param node the node for which to update the moves.
	 * @param jointMove the explored joint move.
	 * @param goals the goals obtained by the simulation, to be used to update the statistics.
	 */
	private void suctUpdate(InternalPropnetSUCTMCTSNode node, UCTMCTSJointMove jointMove, int[] goals) {

		node.incrementTotVisits();

		int currentRoleIndex = this.myRole.getIndex();

		SUCTMCTSMoveStats currentStatsToUpdate = node.getMovesStats()[jointMove.getMovesIndices()[currentRoleIndex]];

		while(currentRoleIndex != ((this.myRole.getIndex()-1+this.numRoles)%this.numRoles)){
			currentStatsToUpdate.incrementVisits();
			currentStatsToUpdate.incrementScoreSum(goals[currentRoleIndex]);
			currentRoleIndex = (currentRoleIndex+1)%this.numRoles;
			currentStatsToUpdate = currentStatsToUpdate.getNextRoleMovesStats()[jointMove.getMovesIndices()[currentRoleIndex]];
		}

		// When the while-loop exits, the leaf move's stats (or the ones of my role if the game is
		// a single player game) still have to be updated. We do so also checking if it was the
		// first time the joint move was explored (i.e. the unvisitedSubleaves count of this move's
		// stats equals 1 or equally its visits count equals 0).
		currentStatsToUpdate.incrementVisits();
		currentStatsToUpdate.incrementScoreSum(goals[currentRoleIndex]);

		// If it's the first visit for this leaf, it's also the first visit of the joint move and
		// we must decrement by 1 the unvisitedSubleaves count of all the moves in this joint move.
		if(currentStatsToUpdate.getUnvisitedSubleaves() == 1){
			currentRoleIndex = this.myRole.getIndex();

			currentStatsToUpdate = node.getMovesStats()[jointMove.getMovesIndices()[currentRoleIndex]];

			while(currentRoleIndex != ((this.myRole.getIndex()-1+this.numRoles)%this.numRoles)){
				currentStatsToUpdate.decreaseUnvisitedSubLeaves();
				currentRoleIndex = (currentRoleIndex+1)%this.numRoles;
				currentStatsToUpdate = currentStatsToUpdate.getNextRoleMovesStats()[jointMove.getMovesIndices()[currentRoleIndex]];
			}

			currentStatsToUpdate.decreaseUnvisitedSubLeaves();

			node.decreaseUnvisitedLeaves();
		}
	}

	/**
	 * Backpropagation, slow SEQUENTIAL version.
	 * This method backpropagates the goal values, updating for each role the score
	 * and the visits of the move that was selected to be part of the joint move.
	 * This method updates only the statistics of a move depending on the moves selected
	 * in the joint move for the roles that precede this move in the moves statistics tree.
	 *
	 * @param node the node for which to update the moves.
	 * @param jointMove the explored joint move.
	 * @param goals the goals obtained by the simulation, to be used to update the statistics.
	 */
	private void ssuctUpdate(InternalPropnetSlowSUCTMCTSNode node, SlowSUCTMCTSJointMove jointMove, int[] goals) {

		node.incrementTotVisits();

		// Get the leaf move from where to start updating
		SlowSUCTMCTSMoveStats theMoveToUpdate = jointMove.getLeafMove();

		// Get the index of the role that performs the leaf move.
		int roleIndex = ((this.myRole.getIndex()-1) + this.numRoles)%this.numRoles;

		// Update the score and the visits of the move.
		// For the leaf move, if this is the first visit, remove it from the unvisited leaf moves list.
		theMoveToUpdate.incrementScoreSum(goals[roleIndex]);
		if(theMoveToUpdate.getVisits() == 0){
			node.getUnvisitedLeaves().remove(theMoveToUpdate);
		}
		theMoveToUpdate.incrementVisits();

		// Get the parent move to be updated.
		theMoveToUpdate = theMoveToUpdate.getPreviousRoleMoveStats();

		// Until we reach the root move (i.e. our move), keep updating.
		while(theMoveToUpdate != null){
			// Compute the index of the role playing the parent move.
			roleIndex = ((roleIndex-1) + this.numRoles)%this.numRoles;

			// Update the score and the visits of the move.
			theMoveToUpdate.incrementScoreSum(goals[roleIndex]);
			theMoveToUpdate.incrementVisits();

			// Get the parent move, that will be the chosen move for the previous role.
			theMoveToUpdate = theMoveToUpdate.getPreviousRoleMoveStats();
		}
	}
}
