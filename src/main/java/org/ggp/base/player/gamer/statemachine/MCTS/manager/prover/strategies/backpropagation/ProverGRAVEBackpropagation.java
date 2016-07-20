package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverMCTSJointMove;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;

public class ProverGRAVEBackpropagation implements ProverBackpropagationStrategy {

	private ProverStandardBackpropagation stdBackpropagation;

	private ProverGRAVEUpdate graveUpdate;

	public ProverGRAVEBackpropagation(int numRoles, Role myRole, List<List<Move>> allJointMoves) {
		this.stdBackpropagation = new ProverStandardBackpropagation(numRoles, myRole);
		this.graveUpdate = new ProverGRAVEUpdate(allJointMoves);
	}

	@Override
	public void update(MCTSNode node, ProverMCTSJointMove jointMove, int[] goals) {

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
