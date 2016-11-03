package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.sequential.SequentialMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.sequential.SequentialMCTSNode;

public class MaximumScoreChoice implements MoveChoiceStrategy {

	/**
	 * The index in the default list of roles of the role that is actually performing the search and for which the best move will be computed.
	 */
	private int myRoleIndex;

	private Random random;

	public MaximumScoreChoice(int myRoleIndex, Random random){
		this.myRoleIndex =  myRoleIndex;
		this.random = random;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice.MoveChoiceStrategy#chooseBestMove(org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode)
	 */
	@Override
	public CompleteMoveStats chooseBestMove(MCTSNode initialNode) {

		MoveStats[] myMovesStats;

		if(initialNode instanceof DecoupledMCTSNode){
			myMovesStats = ((DecoupledMCTSNode)initialNode).getMoves()[this.myRoleIndex];
		}else if(initialNode instanceof SequentialMCTSNode){
			myMovesStats = ((SequentialMCTSNode)initialNode).getMovesStats();
		}/*else if(initialNode instanceof SlowSeqentialMCTSNode){
			myMovesStats = ((SlowSeqentialMCTSNode)initialNode).getMovesStats();
		}*/else{
			throw new RuntimeException("MaximumScoreChoice-chooseBestMove(): detected a node of a non-recognizable sub-type of class MCTSNode.");
		}

		//System.out.println();
		//System.out.println("Choosing move!");

		//System.out.println("My moves: " + myMovesStats.length);

		List<Integer> chosenMovesIndices = new ArrayList<Integer>();

		double maxAvgScore = -Double.MAX_VALUE;
		double currentAvgScore;

		// For each legal move check the average score
		for(int i = 0; i < myMovesStats.length; i++){

			//System.out.println("Move " + i);

			int visits =  myMovesStats[i].getVisits();

			//System.out.println("Visits: " + visits);

			double scoreSum = myMovesStats[i].getScoreSum();

			//System.out.println("Score sum: " + scoreSum);

			/**
			 * Extra check to make sure that the visits exceed the maximum
			 * feasible value for an int type.
			 * TODO: remove this check once you are reasonably sure that
			 * this can never happen.
			 */
			if(visits < 0){
				throw new RuntimeException("Negative value for visits : VISITS=" + visits + ".");
			}

			/*
			if(scoreSum < 0.0){
				throw new RuntimeException("Negative value for scores sum : "SCORE_SUM=" + scoreSum + ".");
			}
			*/

			if(visits == 0){
				// Default score for unvisited moves
				currentAvgScore = -1;

				//System.out.println("Default move average score: " + currentAvgScore);

			}else{
				// Compute average score
				currentAvgScore = scoreSum / ((double) visits);

				//System.out.println("Computed average score: " + currentAvgScore);
			}

			//System.out.println("Max avg score: " + maxAvgScore);

			// If it's higher than the current maximum one, replace the max value and delete all best moves found so far
			if(currentAvgScore > maxAvgScore){
				maxAvgScore = currentAvgScore;
				chosenMovesIndices.clear();
				chosenMovesIndices.add(new Integer(i));
				//System.out.println("Resetting and adding index: " + i);
			}else if(currentAvgScore == maxAvgScore){
				chosenMovesIndices.add(new Integer(i));

				//System.out.println("Adding index: " + i);
			}
		}

		//System.out.println("Number of indices: " + chosenMovesIndices.size());

		int bestMoveIndex = chosenMovesIndices.get(this.random.nextInt(chosenMovesIndices.size()));

		//System.out.println("Choosing move " + bestMoveIndex);

		MoveStats toReturn = myMovesStats[bestMoveIndex];

		if(toReturn instanceof DecoupledMCTSMoveStats){
			return (DecoupledMCTSMoveStats)toReturn;
		}else if(toReturn instanceof SequentialMCTSMoveStats){
			return new CompleteMoveStats(toReturn.getVisits(), toReturn.getScoreSum(), ((SequentialMCTSNode)initialNode).getAllLegalMoves().get(this.myRoleIndex).get(bestMoveIndex));
		}/*else if(toReturn instanceof SlowSequentialMCTSMoveStats){
			return (SlowSequentialMCTSMoveStats)toReturn;
		}*/else{
			throw new RuntimeException("MaximumScoreChoice-chooseBestMove(): detected a node of a non-recognizable sub-type of class MCTSNode.");
		}
	}

	@Override
	public String getStrategyParameters() {
		return null;
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[MOVE_CHOICE_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[MOVE_CHOICE_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}
}
