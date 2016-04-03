package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.util.statemachine.MachineState;

public interface ProverTreeNodeFactory {

	public MCTSNode createNewNode(MachineState state);

}
