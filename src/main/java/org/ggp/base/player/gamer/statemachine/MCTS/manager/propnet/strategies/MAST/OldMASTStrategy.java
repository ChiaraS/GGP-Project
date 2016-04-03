package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.MAST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.StandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class OldMASTStrategy extends StandardBackpropagation implements /*BackpropagationStrategy,*/ PlayoutStrategy {

	private InternalPropnetStateMachine theMachine;

	private double epsilon;

	private Random random;

	private Map<InternalPropnetMove, MoveStats> mastStatistics;

	public OldMASTStrategy(InternalPropnetStateMachine theMachine, double epsilon, Random random, Map<InternalPropnetMove, MoveStats> mastStatistics, int numRoles, InternalPropnetRole myRole) {

		super(numRoles, myRole);

		this.theMachine = theMachine;
		this.epsilon = epsilon;
		this.random = random;
		this.mastStatistics = mastStatistics;
	}

	@Override
	public int[] playout(InternalPropnetMachineState state,
			int[] playoutVisitedNodes, int maxDepth) {


		//System.out.println("MASTPL");

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

		// Now try to get the goals of the state and update the moves statistics.
        int[] goals = this.theMachine.getSafeGoals(state);
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

        return goals;
	}

	/*
	 *         	List<InternalPropnetMove> jointMove = null;
			try {
				jointMove = getRandomJointMove(state);
			} catch (MoveDefinitionException e) {
				GamerLogger.logError("StateMachine", "Exception getting a joint move while performing safe limited depth charges.");
				GamerLogger.logStackTrace("StateMachine", e);
				break;
			}
	 *
	 *
	 *
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.BackpropagationStrategy#update(org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetMCTSNode, org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove, int[])
	 */

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
	public void update(MCTSNode node, MCTSJointMove jointMove, int[] goals) {

		super.update(node,jointMove, goals);

		//System.out.println("MASTBP");

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

	public int getNumStats(){
		return this.mastStatistics.size();
	}

	@Override
	public String getStrategyParameters() {
		return "EPSILON = " + this.epsilon;
	}

	@Override
	public String printStrategy() {

		String params = this.getStrategyParameters();

		if(params != null){
			return super.printStrategy() + "\n[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return super.printStrategy() + "\n[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
