package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class MASTUpdate {

	private Map<InternalPropnetMove, MoveStats> mastStatistics;

	public MASTUpdate(Map<InternalPropnetMove, MoveStats> mastStatistics) {
		this.mastStatistics = mastStatistics;
	}

	public void update(MCTSNode node, MCTSJointMove jointMove, int[] goals) {

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

}
