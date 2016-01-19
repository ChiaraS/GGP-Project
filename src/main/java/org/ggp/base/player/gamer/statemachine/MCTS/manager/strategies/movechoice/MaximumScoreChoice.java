package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.InternalPropnetDUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.InternalPropnetSUCTMCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class MaximumScoreChoice implements MoveChoiceStrategy {

	/**
	 * The role performing the search and for which the best move will be computed.
	 */
	private InternalPropnetRole myRole;

	private Random random;

	public MaximumScoreChoice(InternalPropnetRole myRole, Random random){
		this.myRole = myRole;
		this.random = random;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MoveChoiceStrategy#chooseBestMove(org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode)
	 */
	@Override
	public MCTSMove chooseBestMove(InternalPropnetMCTSNode initialNode) {

		MCTSMove[] myMovesStats;

		if(initialNode instanceof InternalPropnetDUCTMCTSNode){
			myMovesStats = ((InternalPropnetDUCTMCTSNode)initialNode).getMoves()[myRole.getIndex()];
		}else if(initialNode instanceof InternalPropnetSUCTMCTSNode){
			myMovesStats = ((InternalPropnetSUCTMCTSNode)initialNode).getMoves();
		}else{
			throw new RuntimeException("MaximumScoreChoice-chooseBestMove(): detected a node of a non-recognizable sub-type of class InternalPropnetMCTreeNode.");
		}

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
