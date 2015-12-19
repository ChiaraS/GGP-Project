package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.expansion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class RandomExpansion implements ExpansionStrategy {

	private Random random;

	public RandomExpansion(Random random){
		this.random = random;
	}

	@Override
	public boolean expansionRequired(InternalPropnetDUCTMCTreeNode node) {

		int[] unexploredMovesCount = node.getUnexploredMovesCount();

		for(int i = 0; i < unexploredMovesCount.length; i++){
			if(unexploredMovesCount[i] > 0){
				return true;
			}
		}
		return false;
	}

	@Override
	public DUCTJointMove expand(InternalPropnetDUCTMCTreeNode node){

		DUCTMove[][] moves = node.getMoves();
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
				unexploredMovesCount[i]--;
			}
		}

		return new DUCTJointMove(jointMove, movesIndices);
	}

}
