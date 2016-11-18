package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;

public abstract class NodeUpdater extends SearchManagerComponent {

	public NodeUpdater(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	public abstract void update(MctsNode currentNode, MachineState currentState, MctsJointMove jointMove, SimulationResult simulationResult);

	public abstract void processPlayoutResult(MctsNode leafNode, MachineState leafState, SimulationResult simulationResult);

	@Override
	public String getComponentParameters() {
		return null;
	}

	@Override
	public String printComponent() {
		String params = this.getComponentParameters();
		if(params != null){
			return "[NODE_UPDATER = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[NODE_UPDATER = " + this.getClass().getSimpleName() + "]";
		}
	}

}
