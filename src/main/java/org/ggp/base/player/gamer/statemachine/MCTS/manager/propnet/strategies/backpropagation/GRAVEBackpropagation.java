package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.PnMCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class GRAVEBackpropagation implements BackpropagationStrategy {

	private StandardBackpropagation stdBackpropagation;

	private GRAVEUpdate graveUpdate;

	public GRAVEBackpropagation(int numRoles, InternalPropnetRole myRole, List<List<InternalPropnetMove>> allJointMoves) {
		this.stdBackpropagation = new StandardBackpropagation(numRoles, myRole);
		this.graveUpdate = new GRAVEUpdate(allJointMoves);
	}

	@Override
	public void update(PnMCTSNode node, MCTSJointMove jointMove, int[] goals) {

		this.stdBackpropagation.update(node, jointMove, goals);
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
