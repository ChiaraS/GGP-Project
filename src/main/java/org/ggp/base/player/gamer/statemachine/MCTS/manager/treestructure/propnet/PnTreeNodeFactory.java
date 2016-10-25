package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

public interface PnTreeNodeFactory {

	public MCTSNode createNewNode(InternalPropnetMachineState state);

}
