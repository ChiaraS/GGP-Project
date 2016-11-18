package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverSimulationResult;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

public class ProverGRAVEBackpropagation implements ProverBackpropagationStrategy {

	private ProverStandardBackpropagation stdBackpropagation;

	private ProverGRAVEUpdate graveUpdate;

	public ProverGRAVEBackpropagation(int numRoles, ExplicitRole myRole) {
		this.stdBackpropagation = new ProverStandardBackpropagation(numRoles, myRole);
		this.graveUpdate = new ProverGRAVEUpdate();
	}

	@Override
	public void update(MctsNode currentNode, ExplicitMachineState currentState, ProverMCTSJointMove jointMove, ProverSimulationResult simulationResult) {

		this.stdBackpropagation.update(currentNode, currentState, jointMove, simulationResult);
		this.graveUpdate.update(currentNode, currentState, jointMove, simulationResult);

	}

	@Override
	public void processPlayoutResult(MctsNode leafNode,	ExplicitMachineState leafState, ProverSimulationResult simulationResult) {

		this.graveUpdate.processPlayoutResult(leafNode, leafState, simulationResult);

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
