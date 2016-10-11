package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SimulationResult;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
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
	public void update(MCTSNode currentNode, MCTSJointMove jointMove, InternalPropnetMachineState nextState, SimulationResult simulationResult) {

		this.stdBackpropagation.update(currentNode, jointMove, nextState, simulationResult);
		this.mastUpdate.update(currentNode, jointMove, nextState, simulationResult);

	}

	@Override
	public void processPlayoutResult(MCTSNode leafNode, InternalPropnetMachineState leafState,	SimulationResult simulationResult) {

		this.mastUpdate.processPlayoutResult(leafNode, leafState, simulationResult);

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
