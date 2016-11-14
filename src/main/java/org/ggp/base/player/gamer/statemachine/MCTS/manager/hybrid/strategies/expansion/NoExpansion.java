package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.expansion;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;

/**
 * Attention!: for the decoupled version of MCTS the choice of expanding a node and of the action to use
 * for doing so can be made directly by the selection strategy. This class exists to be used together with
 * the decoupled TreeNodeFactory to nullify the effects of the expansion strategy and always use only the
 * selection strategy to decide which move to investigate next (either already visited or not).
 * The expansion strategy classes cannot disappear from the code yet, because of the existence of the sequential
 * MCTS. Using the selection to choose if to expand a node or just explore an already explored action in the
 * sequential MCTS requires additional changes of the code not performed yet (TODO!).
 *
 *
 * @author C.Sironi
 *
 */
public class NoExpansion extends ExpansionStrategy {

	public NoExpansion(GameDependentParameters gameDependentParameters) {
		super(gameDependentParameters);
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	@Override
	public String getStrategyParameters() {
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
	public boolean expansionRequired(MCTSNode node) {
		return false;
	}

	@Override
	public MCTSJointMove expand(MCTSNode node) {
		return null;
	}
}
