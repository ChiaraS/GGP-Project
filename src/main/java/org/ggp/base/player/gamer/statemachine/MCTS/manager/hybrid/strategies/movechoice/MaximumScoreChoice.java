package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.sequential.SequentialMctsMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.sequential.SequentialMctsNode;

public class MaximumScoreChoice extends MoveChoiceStrategy {

	public MaximumScoreChoice(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice.MoveChoiceStrategy#chooseBestMove(org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode)
	 */
	@Override
	public CompleteMoveStats chooseBestMove(MctsNode initialNode) {

		MoveStats[] myMovesStats;

		if(initialNode instanceof DecoupledMctsNode){
			myMovesStats = ((DecoupledMctsNode)initialNode).getMoves()[this.gameDependentParameters.getMyRoleIndex()];
		}else if(initialNode instanceof SequentialMctsNode){
			myMovesStats = ((SequentialMctsNode)initialNode).getMovesStats();
		}/*else if(initialNode instanceof SlowSeqentialMCTSNode){
			myMovesStats = ((SlowSeqentialMCTSNode)initialNode).getMovesStats();
		}*/else{
			throw new RuntimeException("MaximumScoreChoice-chooseBestMove(): detected a node of a non-recognizable sub-type of class MctsNode.");
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

		if(toReturn instanceof DecoupledMctsMoveStats){
			return (DecoupledMctsMoveStats)toReturn;
		}else if(toReturn instanceof SequentialMctsMoveStats){
			return new CompleteMoveStats(toReturn.getVisits(), toReturn.getScoreSum(), ((SequentialMctsNode)initialNode).getAllLegalMoves().get(this.gameDependentParameters.getMyRoleIndex()).get(bestMoveIndex));
		}/*else if(toReturn instanceof SlowSequentialMCTSMoveStats){
			return (SlowSequentialMCTSMoveStats)toReturn;
		}*/else{
			throw new RuntimeException("MaximumScoreChoice-chooseBestMove(): detected a node of a non-recognizable sub-type of class MctsNode.");
		}
	}

	@Override
	public String getComponentParameters() {
		return null;
	}

}
