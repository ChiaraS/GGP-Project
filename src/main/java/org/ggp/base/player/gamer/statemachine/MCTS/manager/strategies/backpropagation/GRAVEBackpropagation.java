package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.PnMCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class GRAVEBackpropagation extends StandardBackpropagation {

	private List<List<InternalPropnetMove>> allJointMoves;

	public GRAVEBackpropagation(int numRoles, InternalPropnetRole myRole, List<List<InternalPropnetMove>> allJointMoves) {
		super(numRoles, myRole);
		this.allJointMoves = allJointMoves;
	}

	@Override
	public void update(PnMCTSNode node, MCTSJointMove jointMove, int[] goals) {

		super.update(node,jointMove, goals);

		//System.out.println("MASTBP");


		// TODO: fix!
		List<InternalPropnetMove> internalJointMove = jointMove.getJointMove();
		MoveStats moveStats;

		for(int i = 0; i < internalJointMove.size(); i++){
        	//moveStats = this.mastStatistics.get(internalJointMove.get(i));
        	//if(moveStats == null){
        		moveStats = new MoveStats();
        		//this.mastStatistics.put(internalJointMove.get(i), moveStats);
        	//}
       		//moveStats.incrementVisits();
       		//moveStats.incrementScoreSum(goals[i]);
       	}

	}

	@Override
	public String getStrategyParameters() {

		// TODO: fix
		String params = super.getStrategyParameters();
		if(params != null){
			return params;
		}else{
			return null;
		}
	}

	@Override
	public String printStrategy() {

		// TODO: fix
		String params = this.getStrategyParameters();

		if(params != null){
			return "[BACKPROPAGATION_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[BACKPROPAGATION_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
