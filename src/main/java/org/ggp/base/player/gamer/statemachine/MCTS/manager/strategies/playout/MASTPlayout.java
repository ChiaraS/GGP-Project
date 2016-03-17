package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout.jointmoveselector.EpsilonMASTJointMoveSelector;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MASTPlayout extends MemorizedStandardPlayout{

	private Map<InternalPropnetMove, MoveStats> mastStatistics;

	public MASTPlayout(InternalPropnetStateMachine theMachine, Random random, Map<InternalPropnetMove, MoveStats> mastStatistics, double epsilon, List<List<InternalPropnetMove>> allJointMoves) {
		//this.theMachine = theMachine;
		super(theMachine, new EpsilonMASTJointMoveSelector(theMachine, random, mastStatistics, epsilon), allJointMoves);

		this.mastStatistics = mastStatistics;
	}

	@Override
	public int[] playout(InternalPropnetMachineState state,
			int[] playoutVisitedNodes, int maxDepth) {

		int[] goals = super.playout(state, playoutVisitedNodes, maxDepth);

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
}
