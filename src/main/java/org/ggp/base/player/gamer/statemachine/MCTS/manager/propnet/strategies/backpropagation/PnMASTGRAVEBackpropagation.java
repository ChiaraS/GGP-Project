package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;

public class PnMASTGRAVEBackpropagation implements PnBackpropagationStrategy {

	private PnStandardBackpropagation stdBackpropagation;

	private PnMASTUpdate mastUpdate;

	private PnGRAVEUpdate graveUpdate;

	public PnMASTGRAVEBackpropagation(int numRoles, CompactRole myRole,  Map<CompactMove, MoveStats> mastStatistics) {
		this.stdBackpropagation = new PnStandardBackpropagation(numRoles, myRole);
		this.mastUpdate = new PnMASTUpdate(mastStatistics);
		this.graveUpdate = new PnGRAVEUpdate();
	}

	@Override
	public void update(MCTSNode currentNode, CompactMachineState currentState, PnMCTSJointMove jointMove, PnSimulationResult simulationResult) {

		this.stdBackpropagation.update(currentNode, currentState, jointMove, simulationResult);
		this.mastUpdate.update(currentNode, currentState, jointMove, simulationResult);
		this.graveUpdate.update(currentNode, currentState, jointMove, simulationResult);

	}

	@Override
	public void processPlayoutResult(MCTSNode leafNode, CompactMachineState leafState, PnSimulationResult simulationResult) {

		this.stdBackpropagation.processPlayoutResult(leafNode, leafState, simulationResult);
		this.mastUpdate.processPlayoutResult(leafNode, leafState, simulationResult);
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
