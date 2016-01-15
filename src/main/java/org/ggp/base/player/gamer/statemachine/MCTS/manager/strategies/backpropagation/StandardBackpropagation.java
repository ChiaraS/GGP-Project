package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;

public class StandardBackpropagation implements BackpropagationStrategy {

	public StandardBackpropagation() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void update(InternalPropnetDUCTMCTreeNode node, DUCTJointMove ductJointMove, int[] goals) {

		node.incrementTotVisits();

		DUCTMove[][] moves = node.getMoves();

		int[] moveIndices = ductJointMove.getMovesIndices();

		for(int i = 0; i < moves.length; i++){
			// Get the DUCTMove
			DUCTMove theMoveToUpdate = moves[i][moveIndices[i]];
			theMoveToUpdate.incrementScoreSum(goals[i]);
			if(theMoveToUpdate.getVisits() == 0){
				node.getUnexploredMovesCount()[i]--;
			}
			theMoveToUpdate.incrementVisits();
		}
	}
}
