package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters;

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
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled.AmafNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class GraveUpdater extends NodeUpdater{

	public GraveUpdater(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	@Override
	public void update(MctsNode currentNode, MachineState currentState, MctsJointMove jointMove, SimulationResult simulationResult) {

		if(currentNode instanceof AmafNode){

			List<List<Move>> allJointMoves = simulationResult.getAllJointMoves();

			int[] goals = simulationResult.getTerminalGoals();

			if(goals == null){
				GamerLogger.logError("MctsManager", "Found null terminal goals in the simulation result when updating the AMAF statistics.");
				throw new RuntimeException("Null terminal goals in the simulation result.");
			}

			if(allJointMoves == null /*|| allJointMoves.size() == 0*/){
				GamerLogger.logError("MctsManager", "Found null joint moves in the simulation result when updating the AMAF statistics.");
				throw new RuntimeException("Null joint moves in the simulation result.");
			}

			Map<Move, MoveStats> amafStats = ((AmafNode)currentNode).getAmafStats();

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
			throw new RuntimeException("GraveUpdate-update(): detected a node not implementing interface AmafNode.");
		}
	}

	@Override
	public void processPlayoutResult(MctsNode leafNode,	MachineState leafState, SimulationResult simulationResult) {

		if(leafNode instanceof AmafNode){

			List<List<Move>> allJointMoves = simulationResult.getAllJointMoves();

			int[] goals = simulationResult.getTerminalGoals();

			if(goals == null){
				GamerLogger.logError("MctsManager", "Found null terminal goals in the simulation result when updating the AMAF statistics of the last node added to the tree. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("Null terminal goals in the simulation result.");
			}

			if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move
				GamerLogger.logError("MctsManager", "Found no joint moves in the simulation result when updating the AMAF statistics of the last node added to the tree. Probably a wrong combination of strategies has been set!");
				throw new RuntimeException("No joint moves in the simulation result.");
			}

			Map<Move, MoveStats> amafStats = ((AmafNode)leafNode).getAmafStats();

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
			throw new RuntimeException("GraveUpdate-processPlayoutResult(): detected a node not implementing interface AmafNode.");
		}

	}

}
