package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.PpaWeights;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;

public class AdaptivePlayoutAfterSimulation extends AfterSimulationStrategy {

	private PpaWeights ppaWeights;

	// Parameter that decides how much the weight changes (temperautre)
	private double alpha;

	public AdaptivePlayoutAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.ppaWeights = new PpaWeights();

		sharedReferencesCollector.setPpaWeights(ppaWeights);

		this.alpha = gamerSettings.getDoublePropertyValue("AfterSimulationStrategy.alpha");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// Do nothing
	}

	@Override
	public void clearComponent() {
		this.ppaWeights = null;
	}

	@Override
	public void setUpComponent() {
		this.ppaWeights.initialize(this.gameDependentParameters.getNumRoles());
	}

	@Override
	public void afterSimulationActions(SimulationResult[] simulationResult) {

		//TODO: fix

		if(simulationResult == null || simulationResult.length < 1){
			GamerLogger.logError("AfterSimulationStrategy", "AdaptivePlayoutAfterSimulation - No simulation results available to perform after simulation actions!");
			throw new RuntimeException("No simulation results available to perform after simulation actions!");
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

		if(this.ppaWeights != null){
			params += indentation + "ppa_weights = " + this.ppaWeights.getMinimalInfo();
		}else{
			params += indentation + "ppa_weights = null";
		}

		return params;
	}

}
