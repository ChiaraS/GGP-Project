package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class MaximumScoreChoice implements MoveChoiceStrategy {

	private Random random;

	public MaximumScoreChoice(Random random) {
		this.random = random;
	}

	@Override
	public DUCTMove chooseBestMove(InternalPropnetDUCTMCTreeNode initialNode, InternalPropnetRole myRole) {

		DUCTMove[] myMovesStats = initialNode.getMoves()[myRole.getIndex()];

		List<Integer> chosenMovesIndices = new ArrayList<Integer>();

		double maxAvgScore = -1;
		double currentAvgScore;

		// For each legal move check the average score
		for(int i = 0; i < myMovesStats.length; i++){
			// Compute average score
			currentAvgScore = (double) myMovesStats[i].getScoreSum() / ((double) myMovesStats[i].getVisits());

			// If it's higher than the current maximum one, replace the max value and delete all best moves found so far
			if(currentAvgScore > maxAvgScore){
				maxAvgScore = currentAvgScore;
				chosenMovesIndices.clear();
				chosenMovesIndices.add(new Integer(i));
			}else if(currentAvgScore == maxAvgScore){
				chosenMovesIndices.add(new Integer(i));
			}
		}

		int bestMoveIndex = chosenMovesIndices.get(this.random.nextInt(chosenMovesIndices.size()));

		return myMovesStats[bestMoveIndex];
	}

}
