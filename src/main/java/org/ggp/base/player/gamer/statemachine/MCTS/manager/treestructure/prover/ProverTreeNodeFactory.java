package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;

public interface ProverTreeNodeFactory {

	public MCTSNode createNewNode(ExplicitMachineState state);

}
