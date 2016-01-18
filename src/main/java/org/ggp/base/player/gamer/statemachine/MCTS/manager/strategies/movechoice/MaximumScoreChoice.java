package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class MaximumScoreChoice implements MoveChoiceStrategy {

	private Random random;

	public MaximumScoreChoice(Random random) {
		this.random = random;
	}

	@Override
	public MCTSMove chooseBestMove(InternalPropnetDUCTMCTreeNode initialNode, InternalPropnetRole myRole) {

		MCTSMove[] myMovesStats = initialNode.getMoves()[myRole.getIndex()];

		//System.out.println("My moves: " + myMovesStats.length);

		List<Integer> chosenMovesIndices = new ArrayList<Integer>();

		double maxAvgScore = -1;
		double currentAvgScore;

		// For each legal move check the average score
		for(int i = 0; i < myMovesStats.length; i++){

			long visits =  myMovesStats[i].getVisits();

			//System.out.println("Visits: " + visits);

			long scoreSum = myMovesStats[i].getScoreSum();

			//System.out.println("Score sum: " + scoreSum);

			if(visits == 0){
				// Default score for unvisited moves
				currentAvgScore = -1;

				//System.out.println("Default move average score: " + currentAvgScore);

			}else{
				// Compute average score
				currentAvgScore = ((double) scoreSum) / ((double) visits);

				//System.out.println("Computed average score: " + currentAvgScore);
			}

			//System.out.println("Max avg score: " + maxAvgScore);

			// If it's higher than the current maximum one, replace the max value and delete all best moves found so far
			if(currentAvgScore > maxAvgScore){
				maxAvgScore = currentAvgScore;
				chosenMovesIndices.clear();
				chosenMovesIndices.add(new Integer(i));
				//System.out.println("Resetting.");
			}else if(currentAvgScore == maxAvgScore){
				chosenMovesIndices.add(new Integer(i));

				//System.out.println("Adding index: " + i);
			}
		}

		//System.out.println("Number of indices: " + chosenMovesIndices.size());

		int bestMoveIndex = chosenMovesIndices.get(this.random.nextInt(chosenMovesIndices.size()));

		return myMovesStats[bestMoveIndex];
	}

}
