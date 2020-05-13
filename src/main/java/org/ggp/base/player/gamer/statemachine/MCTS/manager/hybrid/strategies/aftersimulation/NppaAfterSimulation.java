package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.NGramTreeNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.PpaInfo;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;

import csironi.ggp.course.utils.MyPair;

public class NppaAfterSimulation extends AfterSimulationStrategy {

	/**
	 * ATTENTION: the correct functioning of NPPA is based on the assumption that
	 * the roles in the GDL file are specified in the same order in which they
	 * alternate their turns in sequential-move games;
	 */
	private List<NGramTreeNode<PpaInfo>> nppaStatistics;

	// Parameter that decides how much the weight changes
	private double alpha;

	/**
	 * This parameter decides how much alpha is discounted for each state starting
	 * from the leaf to the root of the current simulation.
	 * Given a simulation of length n, where the root starts at 0, we use the following
	 * values of alpha:
	 * alpha_(n-1) = alpha
	 * alpha_(n-2) = alpha * alphaDiscount
	 * alpha_(n-3) = alpha * alphaDiscount^2
	 * alpha_(n-4) = alpha * alphaDiscount^3
	 * etc...
	 */
	private double alphaDiscount;

	private PLAYOUT_STAT_UPDATE_TYPE updateType;

	private int maxNGramLength;

	public NppaAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.nppaStatistics = new ArrayList<NGramTreeNode<PpaInfo>>();

		sharedReferencesCollector.setNppaStatistics(this.nppaStatistics);

		this.alpha = gamerSettings.getDoublePropertyValue("AfterSimulationStrategy.alpha");

		this.alphaDiscount = gamerSettings.getDoublePropertyValue("AfterSimulationStrategy.alphaDiscount");

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
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.SINGLE_WINNER;
					break;
				default:
					GamerLogger.logError("SearchManagerCreation", "NppaAfterSimulation - The property " + updateTypeString + " is not a valid update type for NPPA statistics.");
					throw new RuntimeException("NppaAfterSimulation - Invalid update type for NPPA statistics " + updateTypeString + ".");
			}
		//}else{
		//	this.updateType = PLAYOUT_STAT_UPDATE_TYPE.SCORES; // Default when nothing is specified
		//}

		this.maxNGramLength = gamerSettings.getIntPropertyValue("AfterSimulationStrategy.maxNGramLength");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent() {
		this.nppaStatistics.clear();
	}

	@Override
	public void setUpComponent() {
		for(int roleIndex = 0; roleIndex < this.gameDependentParameters.getNumRoles(); roleIndex++){
			this.nppaStatistics.add(new NGramTreeNode<PpaInfo>(null)); // PpaInfo is not relevant in the root node of the NGramTree of each role.
		}
	}

	@Override
	public void afterSimulationActions(SimulationResult[] simulationResult) {

		if(simulationResult == null || simulationResult.length < 1){
			GamerLogger.logError("AfterSimulationStrategy", "NppaAfterSimulation - No simulation results available to update NPPA statistics!");
			throw new RuntimeException("No simulation results available to update NPPA statistics!");
		}

		List<List<Move>> allJointMoves;
		List<List<List<Move>>> allMovesInAllStates;

		for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

			double[] g = simulationResult[resultIndex].getTerminalGoalsIn0_100();
			for(int i = 0; i < g.length; i++){
				g[i] /= 100.0;
			}
			//System.out.println();
			//String s = "[ ";
			//for(int i = 0; i < g.length; i++){
			//	s += (g[i] + " ");
			//}
			//s += "]";
			//System.out.println("Goals = " + s);

			// All joint moves played in the current simulation
			allJointMoves = simulationResult[resultIndex].getAllJointMoves();

			// All legal moves for all the roles in each state traversed by the current simulation
			allMovesInAllStates = simulationResult[resultIndex].getAllLegalMovesOfAllRoles();

			//System.out.println("JM");
			//this.printJointMoves(allJointMoves);

			//System.out.println("All legal moves");
			//this.printAllMovesForAllRoles(allMovesInAllStates);

			if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move.
				GamerLogger.logError("AfterSimulationStrategy", "NppaAfterSimulation - Found no joint moves in the simulation result when updating the NPPA statistics. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("No joint moves in the simulation result.");
			}

			if(allMovesInAllStates == null || allMovesInAllStates.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one list of legal moves for all roles.
				GamerLogger.logError("AfterSimulationStrategy", "AdaptivePlayoutAfterSimulation - Found no legal moves for all roles in the simulation result when updating the PPA weights with the playout moves. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("No legal moves for all roles in the simulation result.");
			}

			switch(this.updateType){
				case SCORES:

					double[] goals = simulationResult[resultIndex].getTerminalGoalsIn0_100();

					if(goals == null){
						GamerLogger.logError("AfterSimulationStrategy", "NppaAfterSimulation - Found null terminal goals in the simulation result when updating the NPPA statistics with the playout moves. Probably a wrong combination of strategies has been set!");
						throw new RuntimeException("Null terminal goals in the simulation result.");
					}

					for(int i = 0; i < goals.length; i++){
						goals[i] /= 100.0;
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
					updateAllRolesForPlayout(allJointMoves, allMovesInAllStates, goals);

					break;

				case WINS:

					double[] wins = simulationResult[resultIndex].getTerminalWinsIn0_1();

					if(wins == null){
						GamerLogger.logError("AfterSimulationStrategy", "NppaAfterSimulation - Found null rescaled terminal wins in the simulation result when updating the NPPA statistics with the playout moves. Probably a wrong combination of strategies has been set!");
						throw new RuntimeException("Null rescaled terminal wins in the simulation result.");
					}

					updateAllRolesForPlayout(allJointMoves, allMovesInAllStates, wins);

					break;

			case SINGLE_WINNER:

				int winnerIndex = simulationResult[resultIndex].getSingleWinner();

				if(winnerIndex != -1){

					this.updateWinningRoleForPlayout(allJointMoves, allMovesInAllStates, winnerIndex);

				}

				break;
			}

			//this.logNppaStats();
		}
	}

	/**
	 * Updates the NPPA statistics for the moves and n-grams of all roles in all states traversed during
	 * the playout with the given rewards of all roles.
	 *
	 * @param allJointMoves
	 * @param rewards
	 */
	private void updateAllRolesForPlayout(List<List<Move>> allJointMoves, List<List<List<Move>>> allMovesInAllStates, double[] rewards){

		double discountedAlpha = this.alpha;

		// Iterate over all the joint moves in the playout, starting from the last one.
		for(int jmIndex = allJointMoves.size()-1; jmIndex >= 0; jmIndex--){

			// For a given joint move in the playout, iterate over all the roles to update all their
			// n-grams that end with their current move in the joint move.
			for(int roleIndex = 0; roleIndex < this.gameDependentParameters.getNumRoles(); roleIndex++){

				this.updateNGramsForRoleInState(roleIndex, jmIndex, allJointMoves, allMovesInAllStates, rewards[roleIndex], discountedAlpha);

			}

			discountedAlpha *= this.alphaDiscount;

		}

	}

	/**
	 * Updates the NPPA statistics for the moves of the winning role in all states traversed
	 * during the playout with the maximum reward, i.e. 100.
	 *
	 * @param allJointMoves
	 * @param winnerIndex
	 */
	private void updateWinningRoleForPlayout(List<List<Move>> allJointMoves, List<List<List<Move>>> allMovesInAllStates, int winnerIndex){

		double discountedAlpha = this.alpha;

		// Iterate over all the joint moves in the playout, starting from the last one.
		for(int jmIndex = allJointMoves.size()-1; jmIndex >= 0; jmIndex--){

			this.updateNGramsForRoleInState(winnerIndex, jmIndex, allJointMoves, allMovesInAllStates, 1.0, discountedAlpha);

			discountedAlpha *= this.alphaDiscount;

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
	private void updateNGramsForRoleInState(int roleIndex, int jmIndex, List<List<Move>> allJointMoves,
			List<List<List<Move>>> allMovesInAllStates, double reward, double discountedAlpha){

		// If the role only has one legal move in the state (that thus corresponds to the played one),
		// there is no need to update the weights for the role.
		if(allMovesInAllStates.get(jmIndex).get(roleIndex).size() == 1){
			return;
		}

		// This variable keeps track of the index of the role for which we are adding a move
		// to the current n-gram, that ends with the move for roleIndex.
		int currentRoleIndex = roleIndex;

		// This index keeps track of the simulation step that we are considering to compute the n-gram
		// (i.e. corresponds to the index of the current joint move and of the current list of legal
		// moves for each role)
		int currentJmIndex = jmIndex;

		// This variable keeps track of all the NPPA nodes for the currently considered n-gram length
		// that end with any of the legal moves of roleIndex, and have the other moves corresponding
		// to the ones selected in the joint move.
		// At first, it contains the NPPA nodes of the 1-grams that correspond to each legal action
		// of roleIndex in the current state.
		List<NGramTreeNode<PpaInfo>> currentNppaNodesForNGram = new ArrayList<NGramTreeNode<PpaInfo>>();

		// This variable keeps track of the NPPA nodes for the n-gram that ends with the move of roleIndex
		// and starts with the next move in the sequence of joint moves (going backwards in the sequence).
		List<NGramTreeNode<PpaInfo>> nextNppaNodesForNGram = new ArrayList<NGramTreeNode<PpaInfo>>();

		// This variable keeps track of the length that we are currently considering for the n-grams
		// that we are updating
		int currentNGramLength = 1;

		// This variable keeps track of the current move that we are considering in the n-gram
		Move currentNGramMove;

		// Keeps track of the index of the n-gram of the move that was selected by the role (i.e. roleIndex)
		// during the current step of the simulation (i.e. in the joint move jmIndex).
		int selectedMoveIndex = -1;

		while(currentNGramLength <= this.maxNGramLength && currentJmIndex >= 0){

			if(currentNGramLength == 1){
				// The first time we want to get the NGram nodes that correspond to all the legal moves
				// of the current role. Iterate over all the legal moves for currentRoleIndex in the
				// current state and add their nodes to this list.
				NGramTreeNode<PpaInfo> nppaStatisticsForRole = this.nppaStatistics.get(currentRoleIndex);
				int count = 0;
				for(Move legalMove : allMovesInAllStates.get(currentJmIndex).get(currentRoleIndex)){
					NGramTreeNode<PpaInfo> legalMoveNGram = nppaStatisticsForRole.getNextMoveNode(legalMove);
					if(legalMoveNGram == null){
						legalMoveNGram = new NGramTreeNode<PpaInfo>(new PpaInfo(0.0, 1.0,true, -1));
						nppaStatisticsForRole.addNextMoveNode(legalMove, legalMoveNGram);
					}
					currentNppaNodesForNGram.add(legalMoveNGram);
					// Memorize the index of the move selected in the state by the role.
					if(legalMove.equals(allJointMoves.get(jmIndex).get(roleIndex))){
						selectedMoveIndex = count;
					}
					count++;
				}

				if(selectedMoveIndex == -1){
					GamerLogger.logError("AfterSimulationStrategy", "NppaAfterSimulation - The move selected by the role was not found in the list of its legal moves when updating n-grams for the role in a step!");
					throw new RuntimeException("The move selected by the role was not found in the list of its legal moves when updating n-grams for the role in a step!");
				}
			}else{
				// For n-grams with length > 1, we want to get the previous entry in the list of joint moves,
				// extract the joint move of the currentRoleIndex, and extend all the n-grams that we have
				// so far with this move (i.e. 1 n-gram for each legal move of the initial player).
				// Get current first move in the n-gram
				currentNGramMove = allJointMoves.get(currentJmIndex).get(currentRoleIndex);

				// For each legal move of the initial role, get the NPPA nodes for the n-grams starting
				// with the current move of the current role, continuing with the subsequent moves selected
				// in the playout by other roles and ending with any of the legal moves for the initial role.
				for(NGramTreeNode<PpaInfo> legalMoveNGram : currentNppaNodesForNGram){
					NGramTreeNode<PpaInfo> nextNGram = legalMoveNGram.getNextMoveNode(currentNGramMove);
					if(nextNGram == null){
						nextNGram = new NGramTreeNode<PpaInfo>(new PpaInfo(0.0, 1.0,true, -1));
						legalMoveNGram.addNextMoveNode(currentNGramMove, nextNGram);
					}
					nextNppaNodesForNGram.add(nextNGram);
				}

				currentNppaNodesForNGram = nextNppaNodesForNGram;
				nextNppaNodesForNGram = new ArrayList<NGramTreeNode<PpaInfo>>();

			}

			// Update the weights of the current N-grams
			this.updateNGramWeights(currentNppaNodesForNGram, selectedMoveIndex, reward, discountedAlpha);

			// Update all variables for the next iteration
			// Go to previous role in the order
			currentRoleIndex = (currentRoleIndex-1+this.gameDependentParameters.getNumRoles())%this.gameDependentParameters.getNumRoles();
			// Got to previous joint move
			currentJmIndex--;
			// Increase n-gram length
			currentNGramLength++;

		}

	}

	private void updateNGramWeights(List<NGramTreeNode<PpaInfo>> nGrams, int selectedMoveIndex, double reward, double discountedAlpha){

		int currentIteration = this.gameDependentParameters.getTotIterations();

		// Iterate over all the NGram nodes to compute the sum of the exponential of their weights
		double exponentialSum = 0;
		//System.out.println(exponentialSum);
		for(NGramTreeNode<PpaInfo> nGram : nGrams){
			exponentialSum += nGram.getStatistic().getExpForPolicyAdaptation(currentIteration);
			//System.out.println(exponentialSum);
		}

		// check to be safe
		if(exponentialSum <= 0){ // Should always be positive
			GamerLogger.logError("AfterSimulationStrategy", "NppaAfterSimulation - Found non-positive sum of exponentials when adapting the playout policy!");
			throw new RuntimeException("Found non-positive sum of exponentials when adapting the playout policy.");
		}


		// Iterate over all the NGramTreeNodes to decrease their weight proportionally to their exponential.
		// For the N-gram ending with the selected move also increase the weight by alpha.
		for(int nGramIndex = 0; nGramIndex < nGrams.size(); nGramIndex++){

			if(nGramIndex == selectedMoveIndex){
				nGrams.get(nGramIndex).getStatistic().incrementWeight(currentIteration,
						reward * (discountedAlpha - discountedAlpha * (nGrams.get(nGramIndex).getStatistic().getExpForPolicyAdaptation(currentIteration)/exponentialSum)));
				//System.out.println("detected1");
			}else{
				nGrams.get(nGramIndex).getStatistic().incrementWeight(currentIteration,
						- reward * discountedAlpha * (nGrams.get(nGramIndex).getStatistic().getExpForPolicyAdaptation(currentIteration)/exponentialSum));
			}

		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String nppaStatisticsString;

		if(this.nppaStatistics != null){
			nppaStatisticsString = "[ ";

			int roleIndex = 0;
			for(NGramTreeNode<PpaInfo> roleNppaStats : this.nppaStatistics){
				nppaStatisticsString += (roleNppaStats == null ? "null " : "Tree" + roleIndex + " ");
				roleIndex++;
			}

			nppaStatisticsString += "]";

		}else{
			nppaStatisticsString = "null";
		}

		return indentation + "ALPHA = " + this.alpha +
				indentation + "ALPHA_DISCOUNT = " + this.alphaDiscount +
				indentation + "UPDATE_TYPE = " + this.updateType +
				indentation + "nppa_statistics = " + nppaStatisticsString;

	}

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

	public void printAllMovesForAllRoles(List<List<List<Move>>> allMoves){
		for(List<List<Move>> legalMovesForRolesInState : allMoves){
			this.printJointMoves(legalMovesForRolesInState);
			System.out.println();
		}
	}

	/* CODE FOR DEBUGGING */
	public void logNppaStats(){

		String toLog = "STEP=;" + 1 + ";\n";

		if(nppaStatistics == null){
			for(int roleIndex = 0; roleIndex < nppaStatistics.size(); roleIndex++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(new CompactRole(roleIndex)) + ";\n");
				toLog += "null;\n";
			}
		}else{
			List<MyPair<List<Move>,NGramTreeNode<PpaInfo>>> currentLevel;
			List<MyPair<List<Move>,NGramTreeNode<PpaInfo>>> nextLevel;
			for(int roleIndex = 0; roleIndex < this.nppaStatistics.size(); roleIndex++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(new CompactRole(roleIndex)) + ";\n");
				currentLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<PpaInfo>>>();
				nextLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<PpaInfo>>>();
				// Add the 1-grams to the current level
				for(Entry<Move,NGramTreeNode<PpaInfo>> nGramStats : this.nppaStatistics.get(roleIndex).getNextMoveNodes().entrySet()){
					List<Move> nGram = new ArrayList<Move>();
					nGram.add(nGramStats.getKey());
					currentLevel.add(new MyPair<List<Move>,NGramTreeNode<PpaInfo>>(nGram,nGramStats.getValue()));
				}
				int nGramLength = 1;

				while(!currentLevel.isEmpty()){
					toLog += ("N_GRAM_LENGTH=;" + nGramLength + ";\n");
					for(MyPair<List<Move>,NGramTreeNode<PpaInfo>> nGramTreeNode: currentLevel){
						toLog += ("MOVE=;" + getNGramString2(nGramTreeNode.getFirst()) + ";" + nGramTreeNode.getSecond().getStatistic() + "\n");
						for(Entry<Move,NGramTreeNode<PpaInfo>> nGramStats : nGramTreeNode.getSecond().getNextMoveNodes().entrySet()){
							List<Move> nGram = new ArrayList<Move>(nGramTreeNode.getFirst());
							nGram.add(nGramStats.getKey());
							nextLevel.add(new MyPair<List<Move>,NGramTreeNode<PpaInfo>>(nGram,nGramStats.getValue()));
						}

					}
					currentLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<PpaInfo>>>(nextLevel);
					nextLevel.clear();
					nGramLength++;
				}
			}
		}

		toLog += "\n";

		System.out.println(toLog);

	}

	public String getNGramString2(List<Move> reversedNGram){
		String nGram = "]";

		for(Move m : reversedNGram){
			nGram = this.gameDependentParameters.getTheMachine().convertToExplicitMove(m).toString() + " " + nGram;
		}

		nGram = "[ " + nGram;

		return nGram;
	}

}
