package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTActionsStatistics;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class StandardBackpropagation implements BackpropagationStrategy {

	public StandardBackpropagation() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void update(InternalPropnetDUCTMCTreeNode node, List<InternalPropnetMove> jointMove, int[] goals) {

		node.incrementTotVisits();

		DUCTActionsStatistics[] stats = node.getActionsStatistics();

		for(int i = 0; i < stats.length; i++){
			// Look for the index of the move of the player in the list of legal moves
			int moveIndex = stats[i].getLegalMoves().indexOf(jointMove.get(i));
			if(moveIndex == -1){
				GamerLogger.log("MCTSManager", "MCTS selected a non-legal move for a player.");
				throw new RuntimeException("MCTS selected a non-legal move for a player.");
			}

			stats[i].getScores()[moveIndex] += goals[i];
			stats[i].getVisits()[moveIndex]++;
		}

	}

}
