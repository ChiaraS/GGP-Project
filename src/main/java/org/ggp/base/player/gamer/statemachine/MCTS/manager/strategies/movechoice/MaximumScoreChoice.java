package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.PnMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.DUCTMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCT.PnDUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.PnSUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SUCT.SUCTMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SlowSUCT.PnSlowSUCTMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.SlowSUCT.SlowSUCTMCTSMoveStats;
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
	public CompleteMoveStats chooseBestMove(PnMCTSNode initialNode) {

		MoveStats[] myMovesStats;

		if(initialNode instanceof PnDUCTMCTSNode){
			myMovesStats = ((PnDUCTMCTSNode)initialNode).getMoves()[myRole.getIndex()];
		}else if(initialNode instanceof PnSUCTMCTSNode){
			myMovesStats = ((PnSUCTMCTSNode)initialNode).getMovesStats();
		}else if(initialNode instanceof PnSlowSUCTMCTSNode){
			myMovesStats = ((PnSlowSUCTMCTSNode)initialNode).getMovesStats();
		}else{
			throw new RuntimeException("MaximumScoreChoice-chooseBestMove(): detected a node of a non-recognizable sub-type of class InternalPropnetMCTreeNode.");
		}

		//System.out.println("My moves: " + myMovesStats.length);

		List<Integer> chosenMovesIndices = new ArrayList<Integer>();

		double maxAvgScore = -1;
		double currentAvgScore;

		// For each legal move check the average score
		for(int i = 0; i < myMovesStats.length; i++){

			int visits =  myMovesStats[i].getVisits();

			//System.out.println("Visits: " + visits);

			int scoreSum = myMovesStats[i].getScoreSum();

			/**
			 * Extra check to make sure that neither the visits nor the
			 * scoreSum exceed the maximum feasible value for an int type.
			 * TODO: remove this check once you are reasonably sure that
			 * this can never happen.
			 */
			if(visits < 0 || scoreSum < 0){
				throw new RuntimeException("Negative value for visits and/or scores sum : VISITS=" + visits + ", SCORE_SUM=" + scoreSum + ".");
			}

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

		MoveStats toReturn = myMovesStats[bestMoveIndex];

		if(toReturn instanceof DUCTMCTSMoveStats){
			return (DUCTMCTSMoveStats)toReturn;
		}else if(toReturn instanceof SUCTMCTSMoveStats){
			return new CompleteMoveStats(toReturn.getVisits(), toReturn.getScoreSum(), ((PnSUCTMCTSNode)initialNode).getAllLegalMoves().get(this.myRole.getIndex()).get(bestMoveIndex));
		}else if(toReturn instanceof SlowSUCTMCTSMoveStats){
			return (SlowSUCTMCTSMoveStats)toReturn;
		}else{
			throw new RuntimeException("MaximumScoreChoice-chooseBestMove(): detected a node of a non-recognizable sub-type of class InternalPropnetMCTreeNode.");
		}
	}

	@Override
	public String getStrategyParameters() {
		return "[MOVE_CHOICE_STRATEGY = " + this.getClass().getSimpleName() + "]";
	}

	@Override
	public void afterMoveAction() {
		// TODO Auto-generated method stub

	}

}
