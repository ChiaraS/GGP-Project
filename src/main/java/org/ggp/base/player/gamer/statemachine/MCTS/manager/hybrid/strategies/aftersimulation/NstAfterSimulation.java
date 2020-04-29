package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.NGramTreeNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;

import csironi.ggp.course.utils.MyPair;

public class NstAfterSimulation extends AfterSimulationStrategy {

	/**
	 * ATTENTION: the correct functioning of NST is based on the assumption that
	 * the roles in the GDL file are specified in the same order in which they
	 * alternate their turns in sequential-move games;
	 */
	private List<NGramTreeNode<MoveStats>> nstStatistics;

	private PLAYOUT_STAT_UPDATE_TYPE updateType;

	private int maxNGramLength;

	/**
	 * This parameter decides how much the reward is discounted for each state starting
	 * from the leaf to the root of the current simulation.
	 * Given a simulation of length n, where the root starts at 0, we use the following
	 * values for the reward used to update the statistics of the moves used in the state:
	 * reward_(n-1) = reward
	 * reward_(n-2) = reward * rewardDiscount
	 * reward_(n-3) = reward * rewardDiscount^2
	 * reward_(n-4) = reward * rewardDiscount^3
	 * etc...
	 */
	private double rewardDiscount;

	public NstAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.nstStatistics = new ArrayList<NGramTreeNode<MoveStats>>();

		sharedReferencesCollector.setNstStatistics(nstStatistics);

		//if(gamerSettings.specifiesProperty("AfterSimulationStrategy.updateType")){
			String updateTypeString = gamerSettings.getPropertyValue("AfterSimulationStrategy.updateType");
			switch(updateTypeString.toLowerCase()){
				case "scores":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.SCORES;
					break;
				case "wins":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.WINS;
					break;
				case "winner_only":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.WINNER_ONLY;
					break;
				default:
					GamerLogger.logError("SearchManagerCreation", "NstAfterSimulation - The property " + updateTypeString + " is not a valid update type for NST statistics.");
					throw new RuntimeException("NstAfterSimulation - Invalid update type for NST statistics " + updateTypeString + ".");
			}
		//}else{
		//	this.updateType = PLAYOUT_STAT_UPDATE_TYPE.SCORES; // Default when nothing is specified
		//}

		this.maxNGramLength = gamerSettings.getIntPropertyValue("AfterSimulationStrategy.maxNGramLength");

		this.rewardDiscount = gamerSettings.getDoublePropertyValue("AfterSimulationStrategy.rewardDiscount");


	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent() {
		this.nstStatistics.clear();
	}

	@Override
	public void setUpComponent() {
		for(int roleIndex = 0; roleIndex < this.gameDependentParameters.getNumRoles(); roleIndex++){
			this.nstStatistics.add(new NGramTreeNode<MoveStats>(null)); // Move stats are not relevant in the root node of the NGramTree of each role.
		}
	}

	@Override
	public void afterSimulationActions(SimulationResult[] simulationResult) {

		if(simulationResult == null || simulationResult.length < 1){
			GamerLogger.logError("AfterSimulationStrategy", "NstAfterSimulation - No simulation results available to update NST statistics!");
			throw new RuntimeException("No simulation results available to update NST statistics!");
		}

		List<List<Move>> allJointMoves;

		for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

			// All joint moves played in the current simulation
			allJointMoves = simulationResult[resultIndex].getAllJointMoves();

			//this.printJointMoves(allJointMoves);

			if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move.
				GamerLogger.logError("AfterSimulationStrategy", "NstAfterSimulation - Found no joint moves in the simulation result when updating the NST statistics. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("No joint moves in the simulation result.");
			}

			switch(this.updateType){
				case SCORES:

					double[] goals = simulationResult[resultIndex].getTerminalGoals();

					if(goals == null){
						GamerLogger.logError("AfterSimulationStrategy", "NstAfterSimulation - Found null terminal goals in the simulation result when updating the NST statistics with the playout moves. Probably a wrong combination of strategies has been set!");
						throw new RuntimeException("Null terminal goals in the simulation result.");
					}

					/*
					System.out.println();
					String s = "[ ";
					for(int i = 0; i < goals.length; i++){
						s += (goals[i] + " ");
					}
					s += "]";
					System.out.println(s);
					*/

					updateAllRolesForPlayout(allJointMoves, goals);

					break;

				case WINS:

					double[] wins = simulationResult[resultIndex].getRescaledTerminalWins(); // Returns wins but in [0,100] instead of [0,1]

					if(wins == null){
						GamerLogger.logError("AfterSimulationStrategy", "NstAfterSimulation - Found null rescaled terminal wins in the simulation result when updating the NST statistics with the playout moves. Probably a wrong combination of strategies has been set!");
						throw new RuntimeException("Null rescaled terminal wins in the simulation result.");
					}

					updateAllRolesForPlayout(allJointMoves, wins);

					break;

			case WINNER_ONLY:

				int winnerIndex = simulationResult[resultIndex].getSingleWinner();

				if(winnerIndex != -1){

					this.updateWinningRoleForPlayout(allJointMoves, winnerIndex);

				}

				break;
			}

			//this.printNstStats();
		}
	}

	/**
	 * Updates the NST statistics for the moves and n-grams of all roles in all states traversed during
	 * the playout with the given rewards of all roles.
	 *
	 * @param allJointMoves
	 * @param rewards
	 */
	private void updateAllRolesForPlayout(List<List<Move>> allJointMoves, double[] rewards){

		double discount = 1;

		// Iterate over all the joint moves in the playout, starting from the last one.
		for(int jmIndex = allJointMoves.size()-1; jmIndex >= 0; jmIndex--){

			// For a given joint move in the playout, iterate over all the roles to update all its
			// n-grams that end with his current move in the joint move.
			for(int roleIndex = 0; roleIndex < this.gameDependentParameters.getNumRoles(); roleIndex++){

				this.updateNGramsForRoleInState(roleIndex, jmIndex, allJointMoves, rewards[roleIndex]*discount);

			}

			discount *= this.rewardDiscount;

		}

	}

	/**
	 * Updates the NST statistics for the moves of the winning role in all states traversed
	 * during the playout with the maximum reward, i.e. 100.
	 *
	 * @param allJointMoves
	 * @param winnerIndex
	 */
	private void updateWinningRoleForPlayout(List<List<Move>> allJointMoves, int winnerIndex){

		double discountedReward = 100.0;

		// Iterate over all the joint moves in the playout.
		for(int jmIndex = allJointMoves.size()-1; jmIndex >= 0; jmIndex--){

			this.updateNGramsForRoleInState(winnerIndex, jmIndex, allJointMoves, discountedReward);

			discountedReward *= this.rewardDiscount;

		}

	}

	/**
	 * Given a role index and the index of the joint move performed in the considered state of the simulation,
	 * this method updates the statistics of the n-grams of all lengths (from 1 to maxNGramLength) that
	 * start with the move performed by the given role in the considered joint move. To do so, the method
	 * needs the list of all the joint moves and the reward obtained by the role in the simulation.
	 * This method assumes that maxNGramsLength is at least 1 (i.e. at least one iteration must be performed
	 * in this method.
	 *
	 * @param allJointMoves
	 */
	private void updateNGramsForRoleInState(int roleIndex, int jmIndex, List<List<Move>> allJointMoves, double discountedReward){

		// This variable keeps track of the index of the role for which we are adding a move
		// to the current n-gram, that ends with the move for roleIndex.
		int currentRoleIndex = roleIndex;

		// This index keeps track of the joint move that we are considering to compute the n-gram
		int currentJmIndex = jmIndex;

		// This variable keeps track of the NST node for the n-gram that ends with the move of roleIndex
		// and starts with the move visited in the previous iteration.
		// The first time it corresponds to a node with no statistics, thus to a 0-gram.
		NGramTreeNode<MoveStats> previousNstNodeForNGram = this.nstStatistics.get(currentRoleIndex);

		// This variable keeps track of the NST node for the n-gram that ends with the move of roleIndex
		// and starts with the current move.
		NGramTreeNode<MoveStats> currentNstNodeForNGram;

		// This variable keeps track of the length that we are currently considering for the n-grams
		// that we are updating
		int currentNGramLength = 1;

		// This variable keeps track of the current move that we are considering in the n-gram
		Move currentNGramMove;

		while(currentNGramLength <= this.maxNGramLength && currentJmIndex >= 0){

			// 1. Get current first move in the n-gram
			currentNGramMove = allJointMoves.get(currentJmIndex).get(currentRoleIndex);

			// 2. Get NST node for the n-gram starting with the current move of the current role.
			currentNstNodeForNGram = previousNstNodeForNGram.getNextMoveNode(currentNGramMove);

			// 3. If the node doesn't exist, add it
			if(currentNstNodeForNGram == null){
				currentNstNodeForNGram = new NGramTreeNode<MoveStats>(new MoveStats());
				previousNstNodeForNGram.addNextMoveNode(currentNGramMove, currentNstNodeForNGram);
			}

			// 4. Get the statistic in the node and update them
			currentNstNodeForNGram.getStatistic().incrementScoreSum(discountedReward);
			currentNstNodeForNGram.getStatistic().incrementVisits();

			// Update all variables for the next iteration
			// Go to previous role in the order
			currentRoleIndex = (currentRoleIndex-1+this.gameDependentParameters.getNumRoles())%this.gameDependentParameters.getNumRoles();
			// Got to previous joint move
			currentJmIndex--;
			// Increase n-gram length
			currentNGramLength++;
			// Update the current n-gram tree node
			previousNstNodeForNGram = currentNstNodeForNGram;

		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String nstStatisticsString;

		if(this.nstStatistics != null){
			nstStatisticsString = "[ ";

			int roleIndex = 0;
			for(NGramTreeNode<MoveStats> roleNstStats : this.nstStatistics){
				nstStatisticsString += (roleNstStats == null ? "null " : "Tree" + roleIndex + " ");
				roleIndex++;
			}

			nstStatisticsString += "]";

		}else{
			nstStatisticsString = "null";
		}

		return indentation + "UPDATE_TYPE = " + this.updateType +
				indentation + "MAX_N_GRAM_LENGTH = " + this.maxNGramLength +
				indentation + "REWARD_DISCOUNT = " + this.rewardDiscount +
				indentation + "nst_statistics = " + nstStatisticsString;

	}


	/* FOR DEBUGGING */
	public void printJointMoves(List<List<Move>> allJointMoves){
		for(List<Move> jm : allJointMoves){
			String s = "[ ";
			for(Move m : jm){
				s += this.gameDependentParameters.getTheMachine().convertToExplicitMove(m).toString() + " ";
			}
			s += "]";
			System.out.println(s);
		}
	}

	/*public static void printAllMovesForAllRoles(List<List<List<Move>>> allMoves){
		for(List<List<Move>> legalMovesForRolesInState : allMoves){
			printJointMoves(legalMovesForRolesInState);
			System.out.println();
		}
	}*/




	private void printNstStats(){

		String toLog = "STEP=;" + 1 + ";\n";

		if(nstStatistics == null){
			for(int roleIndex = 0; roleIndex < nstStatistics.size(); roleIndex++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(new CompactRole(roleIndex)) + ";\n");
				toLog += "null;\n";
			}
		}else{
			List<MyPair<List<Move>,NGramTreeNode<MoveStats>>> currentLevel;
			List<MyPair<List<Move>,NGramTreeNode<MoveStats>>> nextLevel;
			double scoreSum;
			double visits;
			for(int roleIndex = 0; roleIndex < nstStatistics.size(); roleIndex++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(new CompactRole(roleIndex)) + ";\n");
				currentLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<MoveStats>>>();
				nextLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<MoveStats>>>();
				// Add the 1-grams to the current level
				for(Entry<Move,NGramTreeNode<MoveStats>> nGramStats : nstStatistics.get(roleIndex).getNextMoveNodes().entrySet()){
					List<Move> nGram = new ArrayList<Move>();
					nGram.add(nGramStats.getKey());
					currentLevel.add(new MyPair<List<Move>,NGramTreeNode<MoveStats>>(nGram,nGramStats.getValue()));
				}
				int nGramLength = 1;

				while(!currentLevel.isEmpty()){
					toLog += ("N_GRAM_LENGTH=;" + nGramLength + ";\n");
					for(MyPair<List<Move>,NGramTreeNode<MoveStats>> nGramTreeNode: currentLevel){
						scoreSum = nGramTreeNode.getSecond().getStatistic().getScoreSum();
						visits = nGramTreeNode.getSecond().getStatistic().getVisits();
						toLog += ("MOVE=;" + getNGramString2(nGramTreeNode.getFirst()) +
						";SCORE_SUM=;" + scoreSum + ";VISITS=;" + visits + ";AVG_VALUE=;" + (scoreSum/visits) + ";\n");

						for(Entry<Move,NGramTreeNode<MoveStats>> nGramStats : nGramTreeNode.getSecond().getNextMoveNodes().entrySet()){
							List<Move> nGram = new ArrayList<Move>(nGramTreeNode.getFirst());
							nGram.add(nGramStats.getKey());
							nextLevel.add(new MyPair<List<Move>,NGramTreeNode<MoveStats>>(nGram,nGramStats.getValue()));
						}

					}
					currentLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<MoveStats>>>(nextLevel);
					nextLevel.clear();
					nGramLength++;
				}
			}
		}

		toLog += "\n";

		System.out.println(toLog);

	}

	private String getNGramString2(List<Move> reversedNGram){
		String nGram = "]";

		for(Move m : reversedNGram){
			nGram = this.gameDependentParameters.getTheMachine().convertToExplicitMove(m).toString() + " " + nGram;
		}

		nGram = "[ " + nGram;

		return nGram;
	}




}
