package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MASTUpdate {

	private Map<InternalPropnetMove, MoveStats> mastStatistics;

	public MASTUpdate(Map<InternalPropnetMove, MoveStats> mastStatistics) {
		this.mastStatistics = mastStatistics;
	}

	public void update(MCTSNode currentNode, InternalPropnetMachineState currentState, MCTSJointMove jointMove, SimulationResult simulationResult) {

		//System.out.println("MASTBP");

		int[] goals = simulationResult.getTerminalGoals();

		if(goals == null){
			GamerLogger.logError("MCTSManager", "Found null terminal goals in the simulation result when updating the AMAF statistics. Probably a wrong combination of strategies has been set!");
			throw new RuntimeException("Null terminal goals in the simulation result.");
		}

		List<InternalPropnetMove> internalJointMove = jointMove.getJointMove();
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

	public void processPlayoutResult(MCTSNode leafNode,	InternalPropnetMachineState leafState, SimulationResult simulationResult) {

		int[] goals = simulationResult.getTerminalGoals();

		List<List<InternalPropnetMove>> allJointMoves = simulationResult.getAllJointMoves();

		if(goals == null){
			GamerLogger.logError("MCTSManager", "Found null terminal goals in the simulation result when updating the MAST statistics with the playout moves. Probably a wrong combination of strategies has been set!");
			throw new RuntimeException("Null terminal goals in the simulation result.");
		}

		if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move
			GamerLogger.logError("MCTSManager", "Found no joint moves in the simulation result when updating the MAST statistics. Probably a wrong combination of strategies has been set!");
			throw new RuntimeException("No joint moves in the simulation result.");
		}

	    MoveStats moveStats;
	    for(List<InternalPropnetMove> jM : allJointMoves){
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
