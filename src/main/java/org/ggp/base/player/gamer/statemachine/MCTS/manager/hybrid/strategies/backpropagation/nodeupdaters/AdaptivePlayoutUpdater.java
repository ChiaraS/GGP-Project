package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public class AdaptivePlayoutUpdater extends NodeUpdater {

	private List<Map<Move, Double>> weightsPerMove;

	// Parameter that decides how much the weight changes
	private double alpha = 1; // TODO: make possible to set from the properties file.

	public AdaptivePlayoutUpdater(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.weightsPerMove = new ArrayList<Map<Move, Double>>();

		sharedReferencesCollector.setWeightsPerMove(weightsPerMove);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent() {
		this.weightsPerMove.clear();
	}

	@Override
	public void setUpComponent() {
		for(int roleIndex = 0; roleIndex < this.gameDependentParameters.getNumRoles(); roleIndex++){
			this.weightsPerMove.add(new HashMap<Move, Double>());
		}
	}

	@Override
	public void update(MctsNode currentNode, MachineState currentState, MctsJointMove jointMove, SimulationResult[] simulationResult) throws MoveDefinitionException, StateMachineException {

		List<Move> internalJointMove = jointMove.getJointMove();

		double[] goals;

		// We might have the result of more than one simulation
		// For each result of a simulation we check which is the winning player (i.e. the one
		// with highest score). If there is one, we update its weights.

    	for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

    		goals = simulationResult[resultIndex].getTerminalGoals();

			// Use the simulation result to figure out for which player to change the weights.
			// For the player with the highest score, weights are increased by alpha for the simulated
			// move and decreased for all other moves proportionally to their probability.
			// If more than 1 player has highest score nothing happens.
    		List<Integer> maxIndices = new ArrayList<Integer>();
    		double max = -Double.MAX_VALUE;
    		for(int roleIndex = 0; roleIndex < internalJointMove.size(); roleIndex++){
    			if(goals[roleIndex] > max){
    				max = goals[roleIndex];
    				maxIndices.clear();
    				maxIndices.add(roleIndex);
    			}else if(goals[roleIndex] == max){
    				maxIndices.add(roleIndex);
    			}
    		}

    		if(maxIndices.size() == 1){ // Only one player with highest score
    			int winnerIndex = maxIndices.get(0);

    			List<Move> legalMoves;

    			if(currentNode != null && currentNode instanceof DecoupledMctsNode) {
    				legalMoves = ((DecoupledMctsNode)currentNode).getLegalMovesForRole(winnerIndex);
    			}else {
    				Role role = this.gameDependentParameters.getTheMachine().getRoles().get(winnerIndex);
    				legalMoves = this.gameDependentParameters.getTheMachine().getLegalMoves(currentState, role);
    			}

    			//System.out.println(this.gameDependentParameters.getTheMachine().convertToExplicitMove(legalMoves.get(0)).getContents().toString());

    			if(legalMoves.size() != 1 || !this.gameDependentParameters.getTheMachine().convertToExplicitMove(legalMoves.get(0)).getContents().toString().equals("noop")){

	    			// Iterate over all legal moves to compute the sum of the exponential of their probabilities
	    			double[] exponentialPerMove = new double[legalMoves.size()];
	    			double probabilitySum = 0;
	    			for(int i = 0; i < legalMoves.size(); i++){

	    				Double currentWeight = this.weightsPerMove.get(winnerIndex).get(legalMoves.get(i));
	    				if(currentWeight == null){
	    					currentWeight = 0.0;
	    					this.weightsPerMove.get(winnerIndex).put(legalMoves.get(i), currentWeight);
	    				}

	    				exponentialPerMove[i] = Math.exp(currentWeight);
	    				probabilitySum += exponentialPerMove[i];

	    			}

	    			// Iterate over all legal moves, if they are the selected one, increase the weight otherwise decrease it
	    			for(int i = 0; i < legalMoves.size(); i++){

	    				double newWeight = this.weightsPerMove.get(winnerIndex).get(legalMoves.get(i));

	    				if(internalJointMove.get(winnerIndex).equals(legalMoves.get(i))){
	    					newWeight += this.alpha;
	    				}
	    				if(probabilitySum > 0){
	    					newWeight -= (this.alpha * (exponentialPerMove[i]/probabilitySum));
	    				}

	    				this.weightsPerMove.get(winnerIndex).put(legalMoves.get(i), newWeight);
	    			}
    			}
    		}
    	}
	}

	@Override
	public void processPlayoutResult(MctsNode leafNode,	MachineState leafState, SimulationResult[] simulationResult) {

		if(simulationResult == null || simulationResult.length < 1){
			GamerLogger.logError("NodeUpdater", "AdaptivePlayoutUpdater - No simulation results available to pre-process!");
			throw new RuntimeException("No simulation results available to pre-process!");
		}

		double[] goals;
		List<List<Move>> allJointMoves;
		List<List<List<Move>>> allMovesInAllStates;

		for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

			goals = simulationResult[resultIndex].getTerminalGoals();

			allJointMoves = simulationResult[resultIndex].getAllJointMoves();

			allMovesInAllStates = simulationResult[resultIndex].getAllLegalMovesOfAllRoles();

			//System.out.println("Joint moves and siblings match?:" + (allJointMoves.size() == allMovesInAllStates.size()));

			if(goals == null){
				GamerLogger.logError("NodeUpdater", "AdaptivePlayoutUpdater - Found null terminal goals in the simulation result when updating the MAST statistics with the playout moves. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("Null terminal goals in the simulation result.");
			}

			if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move
				GamerLogger.logError("NodeUpdater", "AdaptivePlayoutUpdater - Found no joint moves in the simulation result when updating the MAST statistics. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("No joint moves in the simulation result.");
			}

			// Use the simulation result to figure out for which player to change the weights.
			// For the player with the highest score, weights are increased by alpha for the simulated
			// move and decreased for all other moves proportionally to their probability.
			// If more than 1 player has highest score nothing happens.
			List<Integer> maxIndices = new ArrayList<Integer>();
			double max = -Double.MAX_VALUE;
			for(int roleIndex = 0; roleIndex < goals.length; roleIndex++){
			    if(goals[roleIndex] > max){
			    	max = goals[roleIndex];
			    	maxIndices.clear();
			    	maxIndices.add(roleIndex);
			    }else if(goals[roleIndex] == max){
			    	maxIndices.add(roleIndex);
			    }
			}

			if(maxIndices.size() == 1){ // Only one player with highest score
				int winnerIndex = maxIndices.get(0);

			    for(int i = 0; i < allMovesInAllStates.size(); i++){

	    			List<Move> legalMoves = allMovesInAllStates.get(i).get(winnerIndex);
	    			Move winnerMove = allJointMoves.get(i).get(winnerIndex);

	    			if(legalMoves.size() != 1 || !this.gameDependentParameters.getTheMachine().convertToExplicitMove(legalMoves.get(0)).getContents().toString().equals("noop")){

		    			// Iterate over all legal moves to compute the sum of the exponential of their probabilities
		    			double[] exponentialPerMove = new double[legalMoves.size()];
		    			double probabilitySum = 0;
		    			for(int j = 0; j < legalMoves.size(); j++){

		    				Double currentWeight = this.weightsPerMove.get(winnerIndex).get(legalMoves.get(j));
		    				if(currentWeight == null){
		    					currentWeight = 0.0;
		    					this.weightsPerMove.get(winnerIndex).put(legalMoves.get(j), currentWeight);
		    				}

		    				exponentialPerMove[j] = Math.exp(currentWeight);
		    				probabilitySum += exponentialPerMove[j];

		    			}

		    			//for(Move m : legalMoves){
		    			//	System.out.print(m + " ");
		    			//}
		    			//System.out.println();
		    			//System.out.println(winnerMove);

		    			// Iterate over all legal moves, if they are the selected one, increase the weight otherwise decrease it
		    			for(int j = 0; j < legalMoves.size(); j++){

		    				double newWeight = this.weightsPerMove.get(winnerIndex).get(legalMoves.get(j));

		    				//System.out.println(this.weightsPerMove.get(winnerIndex).get(legalMoves.get(j)));

		    				if(winnerMove.equals(legalMoves.get(j))){
		    					newWeight += this.alpha;
		    					//System.out.println("detected1");
		    				}
		    				if(probabilitySum > 0){
		    					newWeight -=  (this.alpha * (exponentialPerMove[j]/probabilitySum));
		    					//System.out.println("detected2");
		    				}
		    				this.weightsPerMove.get(winnerIndex).put(legalMoves.get(j), newWeight);

		    				//System.out.println("The weight: " + this.weightsPerMove.get(winnerIndex).get(legalMoves.get(j)));

		    			}

		    			//for(Map<Move, Double> weightOfPlayer : this.weightsPerMove){
		    				//double sum = 0;
		    				//for(Entry<Move,Double> weight : weightOfPlayer.entrySet()){
		    				//	sum += weight.getValue();
		    			//	}
		    			//	System.out.println(sum);
		    		//	}
		    		//	System.out.println();

	    			}
	    		}
			}
		}
	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "ALPHA = " + this.alpha;

		if(this.weightsPerMove != null){
			String weightsPerMoveString = "[ ";

			for(Map<Move, Double> roleWeightsPerMove : this.weightsPerMove){
				weightsPerMoveString += roleWeightsPerMove.size() + " entries, ";
			}

			weightsPerMoveString += "]";

			params += indentation + "weights_per_move = " + weightsPerMoveString;
		}else{
			params += indentation + "weights_per_move = null";
		}

		return params;

	}

}
