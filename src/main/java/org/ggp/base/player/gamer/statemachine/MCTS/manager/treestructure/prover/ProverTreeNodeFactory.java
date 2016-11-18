package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;

public interface ProverTreeNodeFactory {

	public MctsNode createNewNode(ExplicitMachineState state);

}
