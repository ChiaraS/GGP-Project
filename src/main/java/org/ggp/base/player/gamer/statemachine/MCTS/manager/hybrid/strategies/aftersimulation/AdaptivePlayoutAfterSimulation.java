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

	// Parameter that decides how much the weight changes
	private double alpha;

	// THIS PARAMETER HAS AN EFFECT ONLY WHEN WEIGHTS CAN HAVE A NEGATIVE UPDATE FOR THE
	// MOVES PLAYED IN THE SIMULATION (i.e. for the following types of updates: RESCALED_SCORES,
	// RESCALED_WINS, SINGLE_WINNER_AND_LOSERS, ALL_WINNERS_AND_LOSERS, PROPORTIONAL_SINGLE_WINNER_AND_LOSERS,
	// PROPORTIONAL_ALL_WINNERS_AND_LOSERS, LOSERS_WITH_SINGLE_WINNER, LOSERS_WITH_ALL_WINNERS
	// Parameter that decides how much the weight changes when the roles is losing.
	// Can be specified as equal to alpha, when the update of weights is performed only
	// for the winner.
	// When updating the weights for the losing player, some weights might explode in value
	// over time. This parameter can be used to keep those values in check.
	private double alphaLoss;

	// THIS PARAMETER HAS AN EFFECT ONLY WHEN WEIGHTS CAN AVE A NEGATIVE UPDATE FOR THE
	// MOVES PLAYED IN THE SIMULATION (i.e. for the following types of updates: RESCALED_SCORES,
	// RESCALED_WINS, SINGLE_WINNER_AND_LOSERS, ALL_WINNERS_AND_LOSERS, PROPORTIONAL_SINGLE_WINNER_AND_LOSERS,
	// PROPORTIONAL_ALL_WINNERS_AND_LOSERS, LOSERS_WITH_SINGLE_WINNER, LOSERS_WITH_ALL_WINNERS
	// If true, when updating weights for a loss (i.e. with a negative update) the update formulas
	// are inverted for the played move and the non-played moves in a state.
	private boolean invert;

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

	public AdaptivePlayoutAfterSimulation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.ppaWeights = new PpaWeights();

		sharedReferencesCollector.setPpaWeights(ppaWeights);

		this.alpha = gamerSettings.getDoublePropertyValue("AfterSimulationStrategy.alpha");

		this.alphaLoss = gamerSettings.getDoublePropertyValue("AfterSimulationStrategy.alphaLoss");

		this.invert = gamerSettings.getBooleanPropertyValue("AfterSimulationStrategy.invert");

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
				case "rescaled_scores":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.RESCALED_SCORES;
					break;
				case "rescaled_wins":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.RESCALED_WINS;
					break;
				case "single_winner": case "winner_only":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.SINGLE_WINNER;
					break;
				case "all_winners":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.ALL_WINNERS;
					break;
				case "single_winner_and_losers":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.SINGLE_WINNER_AND_LOSERS;
					break;
				case "all_winners_and_losers":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.ALL_WINNERS_AND_LOSERS;
					break;
				case "proportional_single_winner":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.PROPORTIONAL_SINGLE_WINNER;
					break;
				case "proportional_all_winners":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.PROPORTIONAL_ALL_WINNERS;
					break;
				case "proportional_single_winner_and_losers":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.PROPORTIONAL_SINGLE_WINNER_AND_LOSERS;
					break;
				case "proportional_all_winners_and_losers":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.PROPORTIONAL_ALL_WINNERS_AND_LOSERS;
					break;
				case "losers_with_single_winner":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.LOSERS_WITH_SINGLE_WINNER;
					break;
				case "losers_with_all_winners":
					this.updateType = PLAYOUT_STAT_UPDATE_TYPE.LOSERS_WITH_ALL_WINNERS;
					break;
				default:
					GamerLogger.logError("SearchManagerCreation", "AfterSimulationStrategy - The property " + updateTypeString + " is not a valid update type for PPA weights.");
					throw new RuntimeException("AfterSimulationStrategy - Invalid  update type for PPA weights " + updateTypeString + ".");
			}
		//}else{
		//	this.updateType = PLAYOUT_STAT_UPDATE_TYPE.SCORES; // Default when nothing is specified
		//}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// Do nothing
	}

	@Override
	public void clearComponent() {
		this.ppaWeights.clear();
	}

	@Override
	public void setUpComponent() {
		this.ppaWeights.setUp(this.gameDependentParameters.getNumRoles());
	}

	@Override
	public void afterSimulationActions(SimulationResult[] simulationResult) {

		if(simulationResult == null || simulationResult.length < 1){
			GamerLogger.logError("AfterSimulationStrategy", "AdaptivePlayoutAfterSimulation - No simulation results available to perform after simulation actions!");
			throw new RuntimeException("No simulation results available to perform after simulation actions!");
		}

		List<List<Move>> allJointMoves;
		List<List<List<Move>>> allMovesInAllStates;

		for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

			// All joint moves played in the current simulation
			allJointMoves = simulationResult[resultIndex].getAllJointMoves();

			// All legal moves for all the roles in each state traversed by the current simulation
			allMovesInAllStates = simulationResult[resultIndex].getAllLegalMovesOfAllRoles();

			//System.out.println("Joint moves and siblings match?:" + (allJointMoves.size() == allMovesInAllStates.size()));

			if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move.
				GamerLogger.logError("AfterSimulationStrategy", "AdaptivePlayoutAfterSimulation - Found no joint moves in the simulation result when updating the PPA weights with the playout moves. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("No joint moves in the simulation result.");
			}

			if(allMovesInAllStates == null || allMovesInAllStates.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one list of legal moves for all roles.
				GamerLogger.logError("AfterSimulationStrategy", "AdaptivePlayoutAfterSimulation - Found no legal moves for all roles in the simulation result when updating the PPA weights with the playout moves. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("No legal moves for all roles in the simulation result.");
			}

			double[] updateFactors;

			switch(this.updateType){
				case SCORES:

					updateFactors = simulationResult[resultIndex].getTerminalGoalsIn0_1();

					if(updateFactors == null){
						GamerLogger.logError("AfterSimulationStrategy", "AdaptivePlayoutAfterSimulation - Found null terminal goals in the simulation result when updating the PPA weights with the playout moves. Probably a wrong combination of strategies has been set!");
						throw new RuntimeException("Null terminal goals in the simulation result.");
					}

					break;

				case WINS:

					updateFactors = simulationResult[resultIndex].getTerminalWinsIn0_1();

					break;

				case SINGLE_WINNER:

					updateFactors = simulationResult[resultIndex].getSingleWin();

					break;
				case ALL_WINNERS:

					updateFactors = simulationResult[resultIndex].getAllWins();

					break;
				case ALL_WINNERS_AND_LOSERS:

					updateFactors = simulationResult[resultIndex].getAllWinsAndLosses();

					break;
				case LOSERS_WITH_ALL_WINNERS:

					updateFactors = simulationResult[resultIndex].getLossesWithAllWins();

					break;
				case LOSERS_WITH_SINGLE_WINNER:

					updateFactors = simulationResult[resultIndex].getLossesWithSingleWin();

					break;
				case PROPORTIONAL_ALL_WINNERS:

					updateFactors = simulationResult[resultIndex].getProportionalAllWins();

					break;
				case PROPORTIONAL_ALL_WINNERS_AND_LOSERS:

					updateFactors = simulationResult[resultIndex].getProportionalAllWinsAndLosses();

					break;
				case PROPORTIONAL_SINGLE_WINNER:

					updateFactors = simulationResult[resultIndex].getProportionalSingleWin();

					break;
				case PROPORTIONAL_SINGLE_WINNER_AND_LOSERS:

					updateFactors = simulationResult[resultIndex].getProportionalSingleWinAndLosses();

					//System.out.println(Arrays.toString(updateFactors));

					break;
				case RESCALED_SCORES:

					updateFactors = simulationResult[resultIndex].getRescaledTerminalGoals(-1.0, 1.0);

					break;
				case RESCALED_WINS:

					updateFactors = simulationResult[resultIndex].getRescaledTerminalWins(-1.0, 1.0);

					break;
				case SINGLE_WINNER_AND_LOSERS:

					updateFactors = simulationResult[resultIndex].getSingleWinAndLosses();

					break;
				default:

					GamerLogger.logError("AfterSimulationStrategy", "AdaptivePlayoutAfterSimulation - Unexisting update type for the weights: " + this.updateType + "!");
					throw new RuntimeException("Unexisting update type for the weights: " + this.updateType + "!");

			}

			double discountedAlpha = this.alpha;
			double discountedAlphaLoss = this.alphaLoss;

			for(int i = allMovesInAllStates.size()-1; i >= 0; i--){

				//System.out.println();

				for(int roleIndex = 0; roleIndex < updateFactors.length; roleIndex++){

					//System.out.println("Role = " + roleIndex);

					if(updateFactors[roleIndex] != 0){
		    			List<Move> legalMovesForRole = allMovesInAllStates.get(i).get(roleIndex);
		    			Move roleMove = allJointMoves.get(i).get(roleIndex);
		    			this.ppaWeights.adaptPolicyForRole(roleIndex, legalMovesForRole,
		    					roleMove, updateFactors[roleIndex], this.invert, discountedAlpha,
		    					discountedAlphaLoss, this.gameDependentParameters.getTotIterations(),
		    					this.gameDependentParameters.getTheMachine());
					}

	    		}

				discountedAlpha *= this.alphaDiscount;
				discountedAlphaLoss *= this.alphaDiscount;

			}

		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "ALPHA = " + this.alpha +
				indentation + "ALPHA_LOSS = " + this.alphaLoss +
				indentation + "INVERT = " + this.invert +
				indentation + "ALPHA_DISCOUNT = " + this.alphaDiscount +
				indentation + "UPDATE_TYPE = " + this.updateType;

		if(this.ppaWeights != null){
			params += indentation + "ppa_weights = " + this.ppaWeights.getMinimalInfo();
		}else{
			params += indentation + "ppa_weights = null";
		}

		return params;
	}

}
