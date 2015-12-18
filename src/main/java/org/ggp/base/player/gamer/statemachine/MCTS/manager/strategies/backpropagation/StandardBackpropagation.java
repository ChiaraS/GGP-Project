package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;

public class StandardBackpropagation implements BackpropagationStrategy {

	public StandardBackpropagation() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void update(InternalPropnetDUCTMCTreeNode node, DUCTJointMove ductJointMove, int[] goals) {

		node.incrementTotVisits();

		DUCTMove[][] actions = node.getActions();

		int[] moveIndices = ductJointMove.getMovesIndices();

		for(int i = 0; i < actions.length; i++){
			actions[i][moveIndices[i]].incrementScoreSum(goals[i]);
			actions[i][moveIndices[i]].incrementVisits();
		}

	}

}
