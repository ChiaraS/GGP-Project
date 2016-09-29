package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.movechoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.prover.ProverCompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.decoupled.ProverDecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.decoupled.ProverDecoupledMCTSNode;

public class ProverMaximumScoreChoice implements ProverMoveChoiceStrategy {

	/**
	 * The role performing the search and for which the best move will be computed.
	 */
	//private Role myRole;
	private int myRoleIndex;

	private Random random;

	public ProverMaximumScoreChoice(/*Role myRole*/int myRoleIndex, Random random){
		//this.myRole = myRole;
		this.myRoleIndex = myRoleIndex;
		this.random = random;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.movechoice.MoveChoiceStrategy#chooseBestMove(org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode)
	 */
	@Override
	public ProverCompleteMoveStats chooseBestMove(MCTSNode initialNode) {

		MoveStats[] myMovesStats;

		if(initialNode instanceof ProverDecoupledMCTSNode){
			myMovesStats = ((ProverDecoupledMCTSNode)initialNode).getMoves()[myRoleIndex];
		}/*else if(initialNode instanceof PnSequentialMCTSNode){
			myMovesStats = ((PnSequentialMCTSNode)initialNode).getMovesStats();
		}else if(initialNode instanceof PnSlowSeqentialMCTSNode){
			myMovesStats = ((PnSlowSeqentialMCTSNode)initialNode).getMovesStats();
		}*/else{
			throw new RuntimeException("MaximumScoreChoice-chooseBestMove(): detected a node of a non-recognizable sub-type of class InternalPropnetMCTreeNode.");
		}

		//System.out.println();
		//System.out.println("Choosing move!");

		//System.out.println("My moves: " + myMovesStats.length);

		List<Integer> chosenMovesIndices = new ArrayList<Integer>();

		double maxAvgScore = -1;
		double currentAvgScore;

		// For each legal move check the average score
		for(int i = 0; i < myMovesStats.length; i++){

			//System.out.println("Move " + i);

			int visits =  myMovesStats[i].getVisits();

			//System.out.println("Visits: " + visits);

			double scoreSum = myMovesStats[i].getScoreSum();

			//System.out.println("Score sum: " + scoreSum);

			/**
			 * Extra check to make sure that neither the visits nor the
			 * scoreSum exceed the maximum feasible value for an int type.
			 * TODO: remove this check once you are reasonably sure that
			 * this can never happen.
			 */
			if(visits < 0 || scoreSum < 0.0){
				throw new RuntimeException("Negative value for visits and/or scores sum : VISITS=" + visits + ", SCORE_SUM=" + scoreSum + ".");
			}

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

		if(toReturn instanceof ProverDecoupledMCTSMoveStats){
			return (ProverDecoupledMCTSMoveStats)toReturn;
		}/*else if(toReturn instanceof SequentialMCTSMoveStats){
			return new CompleteMoveStats(toReturn.getVisits(), toReturn.getScoreSum(), ((PnSequentialMCTSNode)initialNode).getAllLegalMoves().get(this.myRole.getIndex()).get(bestMoveIndex));
		}else if(toReturn instanceof SlowSequentialMCTSMoveStats){
			return (SlowSequentialMCTSMoveStats)toReturn;
		}*/else{
			throw new RuntimeException("MaximumScoreChoice-chooseBestMove(): detected a node of a non-recognizable sub-type of class InternalPropnetMCTreeNode.");
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
