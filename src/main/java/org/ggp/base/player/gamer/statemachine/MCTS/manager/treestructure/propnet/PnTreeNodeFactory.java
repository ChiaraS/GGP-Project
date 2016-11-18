package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;

public interface PnTreeNodeFactory {

	public MctsNode createNewNode(CompactMachineState state);

}
