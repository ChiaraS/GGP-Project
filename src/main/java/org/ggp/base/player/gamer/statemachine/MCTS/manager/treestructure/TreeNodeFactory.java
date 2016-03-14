package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

public interface TreeNodeFactory {

	public PnMCTSNode createNewNode(InternalPropnetMachineState state);

}
