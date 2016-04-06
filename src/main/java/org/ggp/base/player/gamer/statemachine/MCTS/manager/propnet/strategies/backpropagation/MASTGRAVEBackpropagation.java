package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class MASTGRAVEBackpropagation implements BackpropagationStrategy {

	private StandardBackpropagation stdBackpropagation;

	private MASTUpdate mastUpdate;

	private GRAVEUpdate graveUpdate;

	public MASTGRAVEBackpropagation(int numRoles, InternalPropnetRole myRole,  Map<InternalPropnetMove, MoveStats> mastStatistics, List<List<InternalPropnetMove>> allJointMoves) {
		this.stdBackpropagation = new StandardBackpropagation(numRoles, myRole);
		this.mastUpdate = new MASTUpdate(mastStatistics);
		this.graveUpdate = new GRAVEUpdate(allJointMoves);
	}

	@Override
	public void update(MCTSNode node, MCTSJointMove jointMove, int[] goals) {

		this.stdBackpropagation.update(node, jointMove, goals);
		this.mastUpdate.update(node, jointMove, goals);
		this.graveUpdate.update(node, jointMove, goals);

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
