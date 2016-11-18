package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverMCTSJointMove;

public class ProverNoExpansion implements ProverExpansionStrategy {

	public ProverNoExpansion() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getStrategyParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[EXPANSION_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[EXPANSION_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

	@Override
	public boolean expansionRequired(MctsNode node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ProverMCTSJointMove expand(MctsNode node) {
		// TODO Auto-generated method stub
		return null;
	}

}
