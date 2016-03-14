package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.MAST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation.StandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.PnMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class MASTStrategy extends StandardBackpropagation implements /*BackpropagationStrategy,*/ PlayoutStrategy {

	private InternalPropnetStateMachine theMachine;

	private double epsilon;

	private double decayFactor;

	private Random random;

	private Map<InternalPropnetMove, MoveStats> mastStatistics;

	public MASTStrategy(InternalPropnetStateMachine theMachine, double epsilon, double decayFactor, Random random, int numRoles, InternalPropnetRole myRole) {

		super(numRoles, myRole);

		this.theMachine = theMachine;
		this.epsilon = epsilon;
		this.decayFactor = decayFactor;
		this.random = random;
		this.mastStatistics = new HashMap<InternalPropnetMove, MoveStats>();
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
	public void update(PnMCTSNode node, MCTSJointMove jointMove, int[] goals) {

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
		return super.getStrategyParameters() + "\n[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + ", EPSILON = " + this.epsilon + ", DECAY_FACTOR = " + this.decayFactor + "]";
	}

	@Override
	public void afterMoveAction(){

		// VERSION 1: decrease, then check if the visits became 0 and, if so, remove the statistic
		// for the move. -> This means that if the move will be explored again in the next step of
		// the search, a new entry for the move will be created. However it's highly likely that the
		// number of visits decreases to 0 because this move is never explored again because the real
		// game ended up in a part of the tree where this move will not be legal anymore. In this case
		// we won't keep around statistics that we will never use again, but we risk also to end up
		// removing the statistic object for a move that will be explored again during the next steps
		// and we will have to recreate the object (in this case we'll consider as garbage an object
		// that instead we would have needed again).
		Iterator<Entry<InternalPropnetMove,MoveStats>> iterator = this.mastStatistics.entrySet().iterator();
		Entry<InternalPropnetMove,MoveStats> theEntry;
		while(iterator.hasNext()){
			theEntry = iterator.next();
			theEntry.getValue().decreaseByFactor(this.decayFactor);
			if(theEntry.getValue().getVisits() == 0){
				iterator.remove();
			}
		}

		// VERSION 2: decrease and don't check anything.
		/*
		for(MoveStats m : this.mastStatistics.values()){
			m.decreaseByFactor(this.decayFactor);
		}
		*/

	}

}
