package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.structure.MachineState;

public interface TreeNodeFactory {

	public MCTSNode createNewNode(MachineState state);

}
