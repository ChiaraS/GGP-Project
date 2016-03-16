package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.backpropagation;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.PnMCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class MASTBackpropagation implements BackpropagationStrategy {

	private StandardBackpropagation stdBackpropagation;

	private MASTUpdate mastUpdate;

	public MASTBackpropagation(int numRoles, InternalPropnetRole myRole, Map<InternalPropnetMove, MoveStats> mastStatistics) {

		this.stdBackpropagation = new StandardBackpropagation(numRoles, myRole);
		this.mastUpdate = new MASTUpdate(mastStatistics);
	}

	@Override
	public void update(PnMCTSNode node, MCTSJointMove jointMove, int[] goals) {

		this.stdBackpropagation.update(node, jointMove, goals);
		this.mastUpdate.update(node, jointMove, goals);

	}

	@Override
	public String getStrategyParameters() {
		return null;
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[BACKPROPAGATION_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[BACKPROPAGATION_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
