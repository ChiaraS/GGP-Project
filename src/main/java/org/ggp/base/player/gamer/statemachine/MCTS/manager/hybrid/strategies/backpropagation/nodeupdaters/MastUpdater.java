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

	public MastUpdater(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.mastStatistics = new ArrayList<Map<Move, MoveStats>>();

		sharedReferencesCollector.setMastStatistics(mastStatistics);

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

		MoveStats moveStats;

		int[] goals;

		for(int roleIndex = 0; roleIndex < internalJointMove.size(); roleIndex++){

	       	moveStats = this.mastStatistics.get(roleIndex).get(internalJointMove.get(roleIndex));
	       	if(moveStats == null){
	       		moveStats = new MoveStats();
	       		this.mastStatistics.get(roleIndex).put(internalJointMove.get(roleIndex), moveStats);
	       	}

	    	for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

	    		goals = simulationResult[resultIndex].getTerminalGoals();

	    		if(goals == null){
	    			GamerLogger.logError("NodeUpdater", "MastUpdater - Found null terminal goals in the simulation result when updating the AMAF statistics. Probably a wrong combination of strategies has been set!");
	    			throw new RuntimeException("Null terminal goals in the simulation result.");
	    		}

		   		moveStats.incrementVisits();
		   		moveStats.incrementScoreSum(goals[roleIndex]);
	    	}
		}

	}

	@Override
	public void processPlayoutResult(MctsNode leafNode,	MachineState leafState, SimulationResult[] simulationResult) {

		if(simulationResult == null || simulationResult.length < 1){
			GamerLogger.logError("NodeUpdater", "MastUpdater - No simulation results available to pre-process!");
			throw new RuntimeException("No simulation results available to pre-process!");
		}

		int[] goals;
		List<List<Move>> allJointMoves;

		for(int resultIndex = 0; resultIndex < simulationResult.length; resultIndex++){

			goals = simulationResult[resultIndex].getTerminalGoals();

			allJointMoves = simulationResult[resultIndex].getAllJointMoves();

			if(goals == null){
				GamerLogger.logError("NodeUpdater", "MastUpdater - Found null terminal goals in the simulation result when updating the MAST statistics with the playout moves. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("Null terminal goals in the simulation result.");
			}

			if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move
				GamerLogger.logError("NodeUpdater", "MastUpdater - Found no joint moves in the simulation result when updating the MAST statistics. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("No joint moves in the simulation result.");
			}

		    MoveStats moveStats;
		    for(List<Move> jM : allJointMoves){
		    	for(int i = 0; i<jM.size(); i++){
		    		moveStats = this.mastStatistics.get(i).get(jM.get(i));
		        	if(moveStats == null){
		        		moveStats = new MoveStats();
		        		this.mastStatistics.get(i).put(jM.get(i), moveStats);
		        	}

		        	moveStats.incrementVisits();
		        	moveStats.incrementScoreSum(goals[i]);
		        }
		    }
		}
	}

}
