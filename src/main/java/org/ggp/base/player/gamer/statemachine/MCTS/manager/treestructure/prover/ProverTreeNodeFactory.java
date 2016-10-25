package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;

public interface ProverTreeNodeFactory {

	public MCTSNode createNewNode(ProverMachineState state);

}
