package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters;

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

	private Map<Move, MoveStats> mastStatistics;

	public MastUpdater(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.mastStatistics = new HashMap<Move, MoveStats>();

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
		// Do nothing
	}

	@Override
	public void update(MctsNode currentNode, MachineState currentState, MctsJointMove jointMove, SimulationResult simulationResult) {

		int[] goals = simulationResult.getTerminalGoals();

		if(goals == null){
			GamerLogger.logError("MctsManager", "Found null terminal goals in the simulation result when updating the AMAF statistics. Probably a wrong combination of strategies has been set!");
			throw new RuntimeException("Null terminal goals in the simulation result.");
		}

		List<Move> internalJointMove = jointMove.getJointMove();
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

	@Override
	public void processPlayoutResult(MctsNode leafNode,	MachineState leafState, SimulationResult simulationResult) {

		int[] goals = simulationResult.getTerminalGoals();

		List<List<Move>> allJointMoves = simulationResult.getAllJointMoves();

		if(goals == null){
			GamerLogger.logError("MctsManager", "Found null terminal goals in the simulation result when updating the MAST statistics with the playout moves. Probably a wrong combination of strategies has been set!");
			throw new RuntimeException("Null terminal goals in the simulation result.");
		}

		if(allJointMoves == null || allJointMoves.size() == 0){ // This method should be called only if the playout has actually been performed, so there must be at least one joint move
			GamerLogger.logError("MctsManager", "Found no joint moves in the simulation result when updating the MAST statistics. Probably a wrong combination of strategies has been set!");
			throw new RuntimeException("No joint moves in the simulation result.");
		}

	    MoveStats moveStats;
	    for(List<Move> jM : allJointMoves){
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
