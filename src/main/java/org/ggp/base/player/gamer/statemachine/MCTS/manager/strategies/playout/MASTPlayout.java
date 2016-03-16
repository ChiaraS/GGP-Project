package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MASTPlayout extends RandomPlayout {

	//private InternalPropnetStateMachine theMachine;

	private double epsilon;

	private Random random;

	private Map<InternalPropnetMove, MoveStats> mastStatistics;

    private List<List<InternalPropnetMove>> allJointMoves;

	public MASTPlayout(InternalPropnetStateMachine theMachine, double epsilon, Random random, Map<InternalPropnetMove, MoveStats> mastStatistics, List<List<InternalPropnetMove>> allJointMoves) {
		//this.theMachine = theMachine;
		super(theMachine);
		this.epsilon = epsilon;
		this.random = random;
		this.mastStatistics = mastStatistics;
		this.allJointMoves = allJointMoves;
	}

	@Override
	public int[] playout(InternalPropnetMachineState state,
			int[] playoutVisitedNodes, int maxDepth) {

		this.allJointMoves.clear();

		int[] goals = super.playout(state, playoutVisitedNodes, maxDepth);

		//System.out.println("MASTPL");

		/*
        int nDepth = 0;
        List<InternalPropnetMove> jointMove = null;
        List<List<InternalPropnetMove>> allLegalMoves;

        List<List<InternalPropnetMove>> allJointMoves = new ArrayList<List<InternalPropnetMove>>();

        while(nDepth < maxDepth && !this.theMachine.isTerminal(state)) {

        	if(this.random.nextDouble() < this.epsilon){
        		// Choose random action with probability epsilon
        		try {
					jointMove = this.theMachine.getRandomJointMove(state);
				} catch (MoveDefinitionException e) {
					GamerLogger.logError("MCTSManager", "Exception getting a random joint move while performing MAST playout.");
					GamerLogger.logStackTrace("StateMachine", e);
					break;
				}
        	}else{
        		// Choose move with highest average score

        		jointMove = new ArrayList<InternalPropnetMove>();

        		try {
					allLegalMoves = this.theMachine.getAllLegalMoves(state);
				} catch (MoveDefinitionException e) {
					GamerLogger.logError("MCTSManager", "Exception getting all legal moves while performing MAST playout.");
					GamerLogger.logStackTrace("StateMachine", e);
					break;
				}

        		for(List<InternalPropnetMove> moves : allLegalMoves){
        			jointMove.add(this.getMASTMove(moves));
        		}
        	}

        	allJointMoves.add(jointMove);

			state = this.theMachine.getInternalNextState(state, jointMove);

			nDepth++;
        }
        if(playoutVisitedNodes != null)
        	playoutVisitedNodes[0] = nDepth;
        */

		// Now try to get the goals of the state and update the moves statistics.
        //int[] goals = this.theMachine.getSafeGoals(state);

		MoveStats moveStats;
        for(List<InternalPropnetMove> jM : this.allJointMoves){
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

        return goals;
	}

	@Override
	public List<InternalPropnetMove> getJointMove(InternalPropnetMachineState state) throws MoveDefinitionException{

        List<InternalPropnetMove> jointMove = null;
        List<List<InternalPropnetMove>> allLegalMoves;

		if(this.random.nextDouble() < this.epsilon){
    		// Choose random action with probability epsilon
    		try {
				jointMove = this.theMachine.getRandomJointMove(state);
			} catch (MoveDefinitionException e) {
				GamerLogger.logError("MCTSManager", "Exception getting a random joint move while performing MAST playout.");
				throw e;
			}
    	}else{
    		// Choose move with highest average score

    		jointMove = new ArrayList<InternalPropnetMove>();

    		try {
				allLegalMoves = this.theMachine.getAllLegalMoves(state);
			} catch (MoveDefinitionException e) {
				GamerLogger.logError("MCTSManager", "Exception getting all legal moves while performing MAST playout.");
				throw e;
			}

    		for(List<InternalPropnetMove> moves : allLegalMoves){
    			jointMove.add(this.getMASTMove(moves));
    		}
    	}

		this.allJointMoves.add(jointMove);

		return jointMove;
	}

	private InternalPropnetMove getMASTMove(List<InternalPropnetMove> moves) {

		List<InternalPropnetMove> chosenMoves = new ArrayList<InternalPropnetMove>();
		MoveStats moveStats;
		double maxAvgScore = -1;
		double currentAvgScore;

		// For each legal move check the average score
		for(InternalPropnetMove move : moves){

			moveStats = this.mastStatistics.get(move);

			if(moveStats != null && moveStats.getVisits() != 0){
				currentAvgScore = ((double) moveStats.getScoreSum()) / ((double) moveStats.getVisits());
			}else{
				currentAvgScore = 100;
			}

			// If it's higher than the current maximum one, replace the max value and delete all best moves found so far
			if(currentAvgScore > maxAvgScore){
				maxAvgScore = currentAvgScore;
				chosenMoves.clear();
				chosenMoves.add(move);
			}else if(currentAvgScore == maxAvgScore){
				chosenMoves.add(move);
			}
		}

		return chosenMoves.get(this.random.nextInt(chosenMoves.size()));

	}

	@Override
	public String getStrategyParameters() {
		return "EPSILON = " + this.epsilon;
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
