package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class PnMASTBackpropagation implements PnBackpropagationStrategy {

	private PnStandardBackpropagation stdBackpropagation;

	private PnMASTUpdate mastUpdate;

	public PnMASTBackpropagation(int numRoles, InternalPropnetRole myRole, Map<InternalPropnetMove, MoveStats> mastStatistics) {

		this.stdBackpropagation = new PnStandardBackpropagation(numRoles, myRole);
		this.mastUpdate = new PnMASTUpdate(mastStatistics);
	}

	@Override
	public void update(MCTSNode currentNode, InternalPropnetMachineState currentState, PnMCTSJointMove jointMove, PnSimulationResult simulationResult) {

		this.stdBackpropagation.update(currentNode, currentState, jointMove, simulationResult);
		this.mastUpdate.update(currentNode, currentState, jointMove, simulationResult);

	}

	@Override
	public void processPlayoutResult(MCTSNode leafNode, InternalPropnetMachineState leafState,	PnSimulationResult simulationResult) {

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
