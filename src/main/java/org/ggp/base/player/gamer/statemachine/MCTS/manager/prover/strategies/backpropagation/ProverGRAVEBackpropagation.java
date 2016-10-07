package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverSimulationResult;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;

public class ProverGRAVEBackpropagation implements ProverBackpropagationStrategy {

	private ProverStandardBackpropagation stdBackpropagation;

	private ProverGRAVEUpdate graveUpdate;

	public ProverGRAVEBackpropagation(int numRoles, Role myRole) {
		this.stdBackpropagation = new ProverStandardBackpropagation(numRoles, myRole);
		this.graveUpdate = new ProverGRAVEUpdate();
	}

	@Override
	public void update(MCTSNode currentNode, ProverMCTSJointMove jointMove, MachineState nextState, ProverSimulationResult simulationResult) {

		this.stdBackpropagation.update(currentNode, jointMove, nextState, simulationResult);
		this.graveUpdate.update(currentNode, jointMove, nextState, simulationResult);

	}

	@Override
	public void processPlayoutResult(MCTSNode leafNode,	ProverSimulationResult simulationResult) {

		this.graveUpdate.processPlayoutResult(leafNode, simulationResult);

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
