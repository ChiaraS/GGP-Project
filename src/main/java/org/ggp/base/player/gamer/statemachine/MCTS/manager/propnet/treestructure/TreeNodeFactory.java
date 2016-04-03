package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

public interface TreeNodeFactory {

	public MCTSNode createNewNode(InternalPropnetMachineState state);

}
