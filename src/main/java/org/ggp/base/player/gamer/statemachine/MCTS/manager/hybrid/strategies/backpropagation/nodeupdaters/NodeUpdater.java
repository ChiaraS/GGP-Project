package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;

public abstract class NodeUpdater extends SearchManagerComponent {

	public NodeUpdater(GameDependentParameters gameDependentParameters) {
		super(gameDependentParameters);
	}

	public abstract void update(MCTSNode currentNode, MachineState currentState, MCTSJointMove jointMove, SimulationResult simulationResult);

	public abstract void processPlayoutResult(MCTSNode leafNode, MachineState leafState, SimulationResult simulationResult);

	public String getNodeUpdaterParameters() {
		return null;
	}

	public String printNodeUpdater() {
		String params = this.getNodeUpdaterParameters();
		if(params != null){
			return "[NODE_UPDATER = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[NODE_UPDATER = " + this.getClass().getSimpleName() + "]";
		}
	}

}
