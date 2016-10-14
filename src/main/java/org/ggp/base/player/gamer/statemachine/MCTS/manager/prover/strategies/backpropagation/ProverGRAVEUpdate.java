package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverSimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.AMAFDecoupled.ProverAMAFNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;

public class ProverGRAVEUpdate {

	public void update(MCTSNode currentNode, MachineState currentState, ProverMCTSJointMove jointMove, ProverSimulationResult simulationResult) {

		if(currentNode instanceof ProverAMAFNode){

			List<List<Move>> allJointMoves = simulationResult.getAllJointMoves();

			int[] goals = simulationResult.getTerminalGoals();

			if(goals == null){
				GamerLogger.logError("MCTSManager", "Found null terminal goals in the simulation result when updating the AMAF statistics. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("Null terminal goals in the simulation result.");
			}

			if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move
				GamerLogger.logError("MCTSManager", "Found no joint moves in the simulation result when updating the AMAF statistics. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("No joint moves in the simulation result.");
			}

			Map<Move, MoveStats> amafStats = ((ProverAMAFNode)currentNode).getAmafStats();

			allJointMoves.add(jointMove.getJointMove());

			MoveStats moveStats;

	        for(List<Move> jM : allJointMoves){
	        	for(int i = 0; i<jM.size(); i++){
	        		moveStats = amafStats.get(jM.get(i));
	        		if(moveStats == null){
	        			moveStats = new MoveStats();
	        			amafStats.put(jM.get(i), moveStats);
	        		}

	        		moveStats.incrementVisits();
	        		moveStats.incrementScoreSum(goals[i]);
	        	}
	        }

		}else{
			throw new RuntimeException("GRAVEUpdate-update(): detected a node not implementing interface ProverAMAFNode.");
		}
	}

	public void processPlayoutResult(MCTSNode leafNode,	MachineState leafState, ProverSimulationResult simulationResult) {

		if(leafNode instanceof ProverAMAFNode){

			List<List<Move>> allJointMoves = simulationResult.getAllJointMoves();

			int[] goals = simulationResult.getTerminalGoals();

			if(goals == null){
				GamerLogger.logError("MCTSManager", "Found null terminal goals in the simulation result when updating the AMAF statistics of the last node added to the tree. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("Null terminal goals in the simulation result.");
			}

			if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move
				GamerLogger.logError("MCTSManager", "Found no joint moves in the simulation result when updating the AMAF statistics of the last node added to the tree. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("No joint moves in the simulation result.");
			}

			Map<Move, MoveStats> amafStats = ((ProverAMAFNode)leafNode).getAmafStats();

			MoveStats moveStats;

	        for(List<Move> jM : allJointMoves){
	        	for(int i = 0; i<jM.size(); i++){
	        		moveStats = amafStats.get(jM.get(i));
	        		if(moveStats == null){
	        			moveStats = new MoveStats();
	        			amafStats.put(jM.get(i), moveStats);
	        		}

	        		moveStats.incrementVisits();
	        		moveStats.incrementScoreSum(goals[i]);
	        	}
	        }

		}else{
			throw new RuntimeException("GRAVEUpdate-processPlayoutResult(): detected a node not implementing interface ProverAMAFNode.");
		}

	}

}
