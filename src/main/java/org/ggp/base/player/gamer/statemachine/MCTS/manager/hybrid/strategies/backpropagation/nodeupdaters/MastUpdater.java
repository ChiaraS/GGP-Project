package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class MastUpdater extends NodeUpdater{

	private List<Map<Move, MoveStats>> mastStatistics;

	private PLAYOUT_STAT_UPDATE_TYPE updateType;

	public MastUpdater(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.mastStatistics = new ArrayList<Map<Move, MoveStats>>();

		sharedReferencesCollector.setMastStatistics(mastStatistics);

		//if(gamerSettings.specifiesProperty("NodeUpdater.updateType")){
			String updateTypeString = gamerSettings.getPropertyValue("NodeUpdater.updateType");
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
					GamerLogger.logError("SearchManagerCreation", "MastUpdater - The property " + updateTypeString + " is not a valid update type for MAST statistics.");
					throw new RuntimeException("MastUpdater - Invalid update type for MAST statistics " + updateTypeString + ".");
			}
		//}else{
		//	this.updateType = PLAYOUT_STAT_UPDATE_TYPE.SCORES; // Default when nothing is specified
		//}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent() {
		this.mastStatistics.clear();
	}

	@Override
	public void setUpComponent() {
		for(int roleIndex = 0; roleIndex < this.gameDependentParameters.getNumRoles(); roleIndex++){
			this.mastStatistics.add(new HashMap<Move, MoveStats>());
		}
	}

	@Override
	public void update(MctsNode currentNode, MachineState currentState, MctsJointMove jointMove, SimulationResult[] simulationResult) {

		List<Move> internalJointMove = jointMove.getJointMove();

		switch(this.updateType){
			case SCORES:

				double[] goals;

				for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

		    		goals = simulationResult[resultIndex].getTerminalGoalsIn0_100();

		    		if(goals == null){
		    			GamerLogger.logError("NodeUpdater", "MastUpdater - Found null terminal goals in the simulation result when updating the AMAF statistics. Probably a wrong combination of strategies has been set!");
		    			throw new RuntimeException("Null terminal goals in the simulation result.");
		    		}

		    		this.updateAllRolesForState(internalJointMove, goals);

		    	}

				break;

			case WINS:

				double[] wins;

				for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

		    		wins = simulationResult[resultIndex].getTerminalWinsIn0_100();

		    		if(wins == null){
		    			GamerLogger.logError("NodeUpdater", "MastUpdater - Found null rescaled terminal wins in the simulation result when updating the AMAF statistics. Probably a wrong combination of strategies has been set!");
		    			throw new RuntimeException("Null rescaled terminal wins in the simulation result.");
		    		}

		    		this.updateAllRolesForState(internalJointMove, wins);

		    	}

				break;

			case SINGLE_WINNER:

				int winnerIndex;

				for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

					winnerIndex = simulationResult[resultIndex].getSingleWinner();

		    		if(winnerIndex != -1){
		    			this.updateWinningRoleForState(internalJointMove, winnerIndex);
		    		}

		    	}

				break;

		}

	}

	@Override
	public void processPlayoutResult(MctsNode leafNode,	MachineState leafState, SimulationResult[] simulationResult) {

		if(simulationResult == null || simulationResult.length < 1){
			GamerLogger.logError("NodeUpdater", "MastUpdater - No simulation results available to pre-process!");
			throw new RuntimeException("No simulation results available to pre-process!");
		}

		List<List<Move>> allJointMoves;

		switch(this.updateType){
			case SCORES:

				double[] goals;

				for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

					goals = simulationResult[resultIndex].getTerminalGoalsIn0_100();

					allJointMoves = simulationResult[resultIndex].getAllJointMoves();

					if(goals == null){
						GamerLogger.logError("NodeUpdater", "MastUpdater - Found null terminal goals in the simulation result when updating the MAST statistics with the playout moves. Probably a wrong combination of strategies has been set!");
						throw new RuntimeException("Null terminal goals in the simulation result.");
					}

					if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move
						GamerLogger.logError("NodeUpdater", "MastUpdater - Found no joint moves in the simulation result when updating the MAST statistics. Probably a wrong combination of strategies has been set!");
						throw new RuntimeException("No joint moves in the simulation result.");
					}

					updateAllRolesForPlayout(allJointMoves, goals);

				}

				break;

			case WINS:

				double[] wins;

				for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

					wins = simulationResult[resultIndex].getTerminalWinsIn0_100(); // Returns wins but in [0,100] instead of [0,1]

					allJointMoves = simulationResult[resultIndex].getAllJointMoves();

					if(wins == null){
						GamerLogger.logError("NodeUpdater", "MastUpdater - Found null rescaled terminal wins in the simulation result when updating the MAST statistics with the playout moves. Probably a wrong combination of strategies has been set!");
						throw new RuntimeException("Null rescaled terminal wins in the simulation result.");
					}

					if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move
						GamerLogger.logError("NodeUpdater", "MastUpdater - Found no joint moves in the simulation result when updating the MAST statistics. Probably a wrong combination of strategies has been set!");
						throw new RuntimeException("No joint moves in the simulation result.");
					}

					updateAllRolesForPlayout(allJointMoves, wins);

				}

				break;

			case SINGLE_WINNER:

				int winnerIndex;

				for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

					winnerIndex = simulationResult[resultIndex].getSingleWinner();

					if(winnerIndex != -1){

						allJointMoves = simulationResult[resultIndex].getAllJointMoves();

						if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move
							GamerLogger.logError("NodeUpdater", "MastUpdater - Found no joint moves in the simulation result when updating the MAST statistics. Probably a wrong combination of strategies has been set!");
							throw new RuntimeException("No joint moves in the simulation result.");
						}

						this.updateWinningRoleForPlayout(allJointMoves, winnerIndex);

					}
				}
				break;
		}

	}

	/**
	 * Updates the MAST statistics for the moves of all roles in all states traversed during
	 * the playout with the given rewards of all roles.
	 *
	 * @param allJointMoves
	 * @param rewards
	 */
	private void updateAllRolesForPlayout(List<List<Move>> allJointMoves, double[] rewards){

		for(List<Move> jointMove : allJointMoves){
		   	this.updateAllRolesForState(jointMove, rewards);
		}

	}

	/**
	 * Updates the MAST statistics for the moves of all roles in a state with the
	 * given rewards of all roles.
	 *
	 * @param jointMove
	 * @param rewards
	 */
	private void updateAllRolesForState(List<Move> jointMove, double[] rewards){

		MoveStats moveStats;
		for(int i = 0; i<jointMove.size(); i++){
		   	moveStats = this.mastStatistics.get(i).get(jointMove.get(i));
		    if(moveStats == null){
		    	moveStats = new MoveStats();
		    	this.mastStatistics.get(i).put(jointMove.get(i), moveStats);
		    }

		    moveStats.incrementVisits();
		    moveStats.incrementScoreSum(rewards[i]);

		}

	}

	/**
	 * Updates the MAST statistics for the moves of the winning role in all states traversed
	 * during the playout with the maximum reward, i.e. 100.
	 *
	 * @param allJointMoves
	 * @param winnerIndex
	 */
	private void updateWinningRoleForPlayout(List<List<Move>> allJointMoves, int winnerIndex){

		for(List<Move> jointMove : allJointMoves){

			this.updateWinningRoleForState(jointMove, winnerIndex);

		}

	}

	/**
	 * Updates the MAST statistics for the move of the winning role in a state with the
	 * maximum reward, i.e. 100.
	 *
	 * @param jointMove
	 * @param winnerIndex
	 */
	private void updateWinningRoleForState(List<Move> jointMove, int winnerIndex){

		// Get the statistics of the move of the winner
		MoveStats moveStats = this.mastStatistics.get(winnerIndex).get(jointMove.get(winnerIndex));
        if(moveStats == null){
        	moveStats = new MoveStats();
        	this.mastStatistics.get(winnerIndex).put(jointMove.get(winnerIndex), moveStats);
        }

        moveStats.incrementVisits();
        moveStats.incrementScoreSum(100.0); // add 100 for a win (i.e. 1 point, but MAST does not rescale between 0 and 1)


	}

	@Override
	public String getComponentParameters(String indentation) {
		String params = super.getComponentParameters(indentation);

		String mastStatisticsString;

		if(this.mastStatistics != null){
			mastStatisticsString = "[ ";

			for(Map<Move, MoveStats> roleMastStats : this.mastStatistics){
				mastStatisticsString += roleMastStats.size() + " entries, ";
			}

			mastStatisticsString += "]";

		}else{
			mastStatisticsString = "null";
		}

		if(params != null){
			return params + indentation + "UPDATE_TYPE" + this.updateType +
					indentation + "mast_statistics = " + mastStatisticsString;
		}else{
			return indentation + "UPDATE_TYPE" + this.updateType +
					indentation + "mast_statistics = " + mastStatisticsString;
		}
	}

}
