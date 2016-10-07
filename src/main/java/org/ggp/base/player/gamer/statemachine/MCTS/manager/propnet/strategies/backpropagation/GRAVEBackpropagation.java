package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SimulationResult;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class GRAVEBackpropagation implements BackpropagationStrategy {

	private StandardBackpropagation stdBackpropagation;

	private GRAVEUpdate graveUpdate;

	public GRAVEBackpropagation(int numRoles, InternalPropnetRole myRole) {
		this.stdBackpropagation = new StandardBackpropagation(numRoles, myRole);
		this.graveUpdate = new GRAVEUpdate();
	}

	@Override
	public void update(MCTSNode currentNode, MCTSJointMove jointMove, InternalPropnetMachineState nextState, SimulationResult simulationResult) {

		this.stdBackpropagation.update(currentNode, jointMove, nextState, simulationResult);
		this.graveUpdate.update(currentNode, jointMove, nextState, simulationResult);

	}

	@Override
	public void processPlayoutResult(MCTSNode leafNode,
			SimulationResult simulationResult) {

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
