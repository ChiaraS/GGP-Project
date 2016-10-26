package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public class PnMASTUpdate {

	private Map<CompactMove, MoveStats> mastStatistics;

	public PnMASTUpdate(Map<CompactMove, MoveStats> mastStatistics) {
		this.mastStatistics = mastStatistics;
	}

	public void update(MCTSNode currentNode, CompactMachineState currentState, PnMCTSJointMove jointMove, PnSimulationResult simulationResult) {

		//System.out.println("MASTBP");

		int[] goals = simulationResult.getTerminalGoals();

		if(goals == null){
			GamerLogger.logError("MCTSManager", "Found null terminal goals in the simulation result when updating the AMAF statistics. Probably a wrong combination of strategies has been set!");
			throw new RuntimeException("Null terminal goals in the simulation result.");
		}

		List<CompactMove> internalJointMove = jointMove.getJointMove();
		MoveStats moveStats;

		for(int i = 0; i < internalJointMove.size(); i++){
        	moveStats = this.mastStatistics.get(internalJointMove.get(i));
        	if(moveStats == null){
        		moveStats = new MoveStats();
        		this.mastStatistics.put(internalJointMove.get(i), moveStats);
        	}
       		moveStats.incrementVisits();
       		moveStats.incrementScoreSum(goals[i]);
       	}

	}

	public void processPlayoutResult(MCTSNode leafNode,	CompactMachineState leafState, PnSimulationResult simulationResult) {

		int[] goals = simulationResult.getTerminalGoals();

		List<List<CompactMove>> allJointMoves = simulationResult.getAllJointMoves();

		if(goals == null){
			GamerLogger.logError("MCTSManager", "Found null terminal goals in the simulation result when updating the MAST statistics with the playout moves. Probably a wrong combination of strategies has been set!");
			throw new RuntimeException("Null terminal goals in the simulation result.");
		}

		if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move
			GamerLogger.logError("MCTSManager", "Found no joint moves in the simulation result when updating the MAST statistics. Probably a wrong combination of strategies has been set!");
			throw new RuntimeException("No joint moves in the simulation result.");
		}

	    MoveStats moveStats;
	    for(List<CompactMove> jM : allJointMoves){
	    	for(int i = 0; i<jM.size(); i++){
	    		moveStats = this.mastStatistics.get(jM.get(i));
	        	if(moveStats == null){
	        		moveStats = new MoveStats();
	        		this.mastStatistics.put(jM.get(i), moveStats);
	        	}

	        	moveStats.incrementVisits();
	        	moveStats.incrementScoreSum(goals[i]);
	        }
	    }
	}

}
