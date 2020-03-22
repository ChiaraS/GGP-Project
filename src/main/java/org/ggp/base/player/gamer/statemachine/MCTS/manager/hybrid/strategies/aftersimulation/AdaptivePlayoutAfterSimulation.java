package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation;

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

		if(simulationResult == null || simulationResult.length < 1){
			GamerLogger.logError("AfterSimulationStrategy", "AdaptivePlayoutAfterSimulation - No simulation results available to perform after simulation actions!");
			throw new RuntimeException("No simulation results available to perform after simulation actions!");
		}

		double[] goals;
		List<List<Move>> allJointMoves;
		List<List<List<Move>>> allMovesInAllStates;

		for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

			// Terminal goals for current simulation
			goals = simulationResult[resultIndex].getTerminalGoals();

			// All joint moves played in the current simulation
			allJointMoves = simulationResult[resultIndex].getAllJointMoves();

			// All legal moves for all the roles in each state traversed by the current simulation
			allMovesInAllStates = simulationResult[resultIndex].getAllLegalMovesOfAllRoles();

			//System.out.println("Joint moves and siblings match?:" + (allJointMoves.size() == allMovesInAllStates.size()));

			if(goals == null){
				GamerLogger.logError("AfterSimulationStrategy", "AdaptivePlayoutAfterSimulation - Found null terminal goals in the simulation result when updating the PPA weights with the playout moves. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("Null terminal goals in the simulation result.");
			}

			if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move.
				GamerLogger.logError("AfterSimulationStrategy", "AdaptivePlayoutAfterSimulation - Found no joint moves in the simulation result when updating the MAST statistics. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("No joint moves in the simulation result.");
			}

			if(allMovesInAllStates == null || allMovesInAllStates.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one list of legal moves for all roles.
				GamerLogger.logError("AfterSimulationStrategy", "AdaptivePlayoutAfterSimulation - Found no legal moves for all roles in the simulation result when updating the PPA weights with the playout moves. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("No legal moves for all roles in the simulation result.");
			}

			int winnerIndex = simulationResult[resultIndex].getSingleWinner();

			// Use the simulation result to figure out for which player to change the weights.
			// For the player with the highest score, weights are increased by alpha for the simulated
			// move and decreased for all other moves proportionally to their probability.
			// If more than 1 player has highest score nothing happens.
			/*List<Integer> maxIndices = new ArrayList<Integer>();
			double max = -Double.MAX_VALUE;
			for(int roleIndex = 0; roleIndex < goals.length; roleIndex++){
			    if(goals[roleIndex] > max){
			    	max = goals[roleIndex];
			    	maxIndices.clear();
			    	maxIndices.add(roleIndex);
			    }else if(goals[roleIndex] == max){
			    	maxIndices.add(roleIndex);
			    }
			}*/

			if(winnerIndex != -1){ // Only one player with highest score

			    for(int i = 0; i < allMovesInAllStates.size(); i++){

	    			List<Move> legalMovesForWinner = allMovesInAllStates.get(i).get(winnerIndex);
	    			Move winnerMove = allJointMoves.get(i).get(winnerIndex);
	    			this.ppaWeights.adaptPolicy(winnerIndex, legalMovesForWinner, winnerMove, this.alpha, this.gameDependentParameters.getTotIterations());

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
