package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTActionsStatistics;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class MaximumScoreChoice implements MoveChoiceStrategy {

	private Random random;

	public MaximumScoreChoice(Random random) {
		this.random = random;
	}

	@Override
	public InternalPropnetMove chooseBestMove(InternalPropnetDUCTMCTreeNode initialNode, InternalPropnetRole myRole) {

		DUCTActionsStatistics stats = initialNode.getActionsStatistics()[myRole.getIndex()];

		List<InternalPropnetMove> legalMoves = stats.getLegalMoves();
		int[] visits = stats.getVisits();
		int[] scores = stats.getScores();

		List<Integer> chosenMovesIndices = new ArrayList<Integer>();

		double maxAvgScore = -1;
		double currentAvgScore;

		// For each legal action check the average score
		for(int i = 0; i < legalMoves.size(); i++){
			// Compute average score
			currentAvgScore = (double) scores[i] / ((double) visits[i]);

			// If it's higher than the current maximum one, replace the max value and delete all best moves found so far
			if(currentAvgScore > maxAvgScore){
				maxAvgScore = currentAvgScore;
				chosenMovesIndices.clear();
				chosenMovesIndices.add(new Integer(i));
			}else if(currentAvgScore == maxAvgScore){
				chosenMovesIndices.add(new Integer(i));
			}
		}

		if(chosenMovesIndices.size() > 1){
			return legalMoves.get(chosenMovesIndices.get(this.random.nextInt(chosenMovesIndices.size())));
		}else{
			return legalMoves.get(chosenMovesIndices.get(0));
		}
	}

}
