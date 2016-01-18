package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTreeNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCTDUCTJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.InternalPropnetSUCTMCTreeNode;
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

	@Override
	public boolean expansionRequired(InternalPropnetMCTreeNode node){
		if(node instanceof InternalPropnetDUCTMCTreeNode){
			return this.expansionRequired((InternalPropnetDUCTMCTreeNode)node);
		}else if(node instanceof InternalPropnetSUCTMCTreeNode){
			return this.expansionRequired((InternalPropnetSUCTMCTreeNode)node);
		}else{
			throw new RuntimeException("RandomExpansion: detected a node of a non-recognizable sub-type of class InternalPropnetMCTreeNode.");
		}
	}

	private boolean expansionRequired(InternalPropnetDUCTMCTreeNode node) {

		int[] unexploredMovesCount = node.getUnexploredMovesCount();

		for(int i = 0; i < unexploredMovesCount.length; i++){
			if(unexploredMovesCount[i] > 0){
				return true;
			}
		}
		return false;
	}

	private boolean expansionRequired(InternalPropnetSUCTMCTreeNode node){
		return node.getUnvisitedLeaves().size() != 0;
	}

	@Override
	public SUCTDUCTJointMove expand(InternalPropnetMCTreeNode node){
		if(node instanceof InternalPropnetDUCTMCTreeNode){
			return this.expand((InternalPropnetDUCTMCTreeNode)node);
		}else if(node instanceof InternalPropnetSUCTMCTreeNode){
			return this.expand((InternalPropnetSUCTMCTreeNode)node);
		}else{
			throw new RuntimeException("RandomExpansion: detected a node of a non-recognizable sub-type of class InternalPropnetMCTreeNode.");
		}
	}

	private SUCTDUCTJointMove expand(InternalPropnetDUCTMCTreeNode node){

		MCTSMove[][] moves = node.getMoves();
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

		return new SUCTDUCTJointMove(jointMove, movesIndices);
	}

	private SUCTDUCTJointMove expand(InternalPropnetSUCTMCTreeNode node){

		int startingIndex = ((this.myRole.getIndex()-1) + this.numRoles)%this.numRoles;
	}

}
