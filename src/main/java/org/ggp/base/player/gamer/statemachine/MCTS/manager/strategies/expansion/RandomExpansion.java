package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.DUCTMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.DUCTMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.InternalPropnetDUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.InternalPropnetSlowSUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.SUCTMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.SlowSUCTMCTSMoveStats;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class RandomExpansion implements ExpansionStrategy {

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

	private Random random;

	public RandomExpansion(int numRoles, InternalPropnetRole myRole, Random random){
		this.numRoles = numRoles;
		this.myRole = myRole;
		this.random = random;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.ExpansionStrategy#expansionRequired(org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode)
	 */
	@Override
	public boolean expansionRequired(InternalPropnetMCTSNode node){
		if(node instanceof InternalPropnetDUCTMCTSNode){
			return this.expansionRequired((InternalPropnetDUCTMCTSNode)node);
		}else if(node instanceof InternalPropnetSlowSUCTMCTSNode){
			return this.expansionRequired((InternalPropnetSlowSUCTMCTSNode)node);
		}else{
			throw new RuntimeException("RandomExpansion-expansionRequired(): detected a node of a non-recognizable sub-type of class InternalPropnetMCTreeNode.");
		}
	}

	private boolean expansionRequired(InternalPropnetDUCTMCTSNode node) {

		int[] unexploredMovesCount = node.getUnexploredMovesCount();

		for(int i = 0; i < unexploredMovesCount.length; i++){
			if(unexploredMovesCount[i] > 0){
				return true;
			}
		}
		return false;
	}

	private boolean expansionRequired(InternalPropnetSlowSUCTMCTSNode node){
		return node.getUnvisitedLeaves().size() != 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion.ExpansionStrategy#expand(org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode)
	 */
	@Override
	public MCTSJointMove expand(InternalPropnetMCTSNode node){
		if(node instanceof InternalPropnetDUCTMCTSNode){
			return this.expand((InternalPropnetDUCTMCTSNode)node);
		}else if(node instanceof InternalPropnetSlowSUCTMCTSNode){
			return this.expand((InternalPropnetSlowSUCTMCTSNode)node);
		}else{
			throw new RuntimeException("RandomExpansion-expand(): detected a node of a non-recognizable sub-type of class InternalPropnetMCTreeNode.");
		}
	}

	/**
	 * Random Expansion, DECOUPLED version.
	 * This method selects for each role a move that has not been visited yet and
	 * returns a joint move. If for a role all moves have been already visited, a
	 * random one is returned. Note that this will never happen if this method is
	 * called only after checking that the "expansionRequired" method returned true.
	 *
	 * @param node the node for which to choose a joint move.
	 * @return the joint move.
	 */
	private MCTSJointMove expand(InternalPropnetDUCTMCTSNode node){

		DUCTMCTSMoveStats[][] moves = node.getMoves();
		int[] unexploredMovesCount = node.getUnexploredMovesCount();

		List<InternalPropnetMove> jointMove = new ArrayList<InternalPropnetMove>();
		int[] movesIndices = new int[moves.length];

		// For each role...
		for(int i = 0; i < moves.length; i++){
			// ...if all moves have been explored...
			if(unexploredMovesCount[i] == 0){
				// ...select a random one,...
				movesIndices[i] = this.random.nextInt(moves[i].length);
				jointMove.add(moves[i][movesIndices[i]].getTheMove());
			}else{ // ...otherwise, if at least one move is still unexplored...
				//...select a random one among the unexplored ones.
				int unexploredIndex = this.random.nextInt(unexploredMovesCount[i]);

				int moveIndex = -1;

				while(unexploredIndex > -1){
					moveIndex++;
					if(moves[i][moveIndex].getVisits() == 0){
						unexploredIndex--;
					}
				}

				movesIndices[i] = moveIndex;
				jointMove.add(moves[i][movesIndices[i]].getTheMove());

				// Do this when backpropagating and actually incrementing the number of visits
				// of this move from 0 to 1.
				//unexploredMovesCount[i]--;
			}
		}

		return new DUCTMCTSJointMove(jointMove, movesIndices);
	}

	/**
	 * Random Expansion, SEQUENTIAL version.
	 * This method selects a joint move that has not been explored yet.
	 * This is done by selecting a random unvisited leaf move in the tree of move statistics.
	 * Visiting the tree backward from the leaf allows to reconstruct the joint move.
	 * If all joint moves have been explored, this method returns a random one among them.
	 * Note that this will never happen if this method is called only after checking that
	 * the "expansionRequired" method returned true.
	 *
	 * @param node the node for which to choose a joint move.
	 * @return the joint move.
	 */
	private MCTSJointMove expand(InternalPropnetSlowSUCTMCTSNode node){

		List<InternalPropnetMove> jointMove = new ArrayList<InternalPropnetMove>(this.numRoles);

		// Initialize ArrayList with numRoles null elements.
		for(int i = 0; i < this.numRoles; i++){
			jointMove.add(null);
		}

		SlowSUCTMCTSMoveStats leafMove = null;

		List<SlowSUCTMCTSMoveStats> unvisitedLeaves = node.getUnvisitedLeaves();
		if(unvisitedLeaves.isEmpty()){
			return this.getRandomMove(node);
		}

		// Initialize the roleIndex to myRole index. In this way the first index for which a move will be
		// chosen will be the role index right before this one (numRoles-1 in case myRole index is 0).
		int roleIndex = this.myRole.getIndex();

		// Get a random leaf move (will be the selected move for myRole if it's a single-player game, or for
		// the role that comes right before myRole in the list of roles for a multi-player game).
		leafMove = unvisitedLeaves.get(this.random.nextInt(unvisitedLeaves.size()));
		SlowSUCTMCTSMoveStats chosenMove = leafMove;

		while(chosenMove != null){
			roleIndex = ((roleIndex-1) + this.numRoles)%this.numRoles;

			// Add the move to the joint move.
			jointMove.set(roleIndex, chosenMove.getTheMove());

			// Get the parent move, that will be the chosen move for the previous role.
			chosenMove = chosenMove.getPreviousRoleMove();
		}

		return new SUCTMCTSJointMove(jointMove, leafMove);
	}

	private MCTSJointMove getRandomMove(InternalPropnetSlowSUCTMCTSNode node){

		List<InternalPropnetMove> jointMove = new ArrayList<InternalPropnetMove>(this.numRoles);

		// Initialize ArrayList with numRoles null elements.
		for(int i = 0; i < this.numRoles; i++){
			jointMove.add(null);
		}

		// Get the index of myRole.
		int roleIndex = this.myRole.getIndex();

		// Get a random move for myRole.
		SlowSUCTMCTSMoveStats[] moves = node.getMoves();
		SlowSUCTMCTSMoveStats chosenMove = null;

		while(moves != null){

			// Select a random move.
			chosenMove = moves[this.random.nextInt(moves.length)];

			// Add the move to the joint move.
			jointMove.set(roleIndex, chosenMove.getTheMove());

			// Get the next role moves.
			moves = chosenMove.getNextRoleMoves();

			// Compute the index for the next role
			roleIndex = (roleIndex+1)%this.numRoles;

		}

		return new SUCTMCTSJointMove(jointMove, chosenMove);
	}

}
