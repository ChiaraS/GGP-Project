package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.structure.MachineState;

public abstract class TreeNodeFactory extends SearchManagerComponent{

	public TreeNodeFactory(GameDependentParameters gameDependentParameters,	Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);
	}

	public abstract MCTSNode createNewNode(MachineState state);

	@Override
	public String printComponent() {
		String params = this.getComponentParameters();

		if(params != null){
			return "[TREE_NODE_FACTORY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[TREE_NODE_FACTORY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
