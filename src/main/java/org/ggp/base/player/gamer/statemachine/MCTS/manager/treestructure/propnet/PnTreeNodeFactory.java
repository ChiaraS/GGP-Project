package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;

public interface PnTreeNodeFactory {

	public MCTSNode createNewNode(CompactMachineState state);

}
