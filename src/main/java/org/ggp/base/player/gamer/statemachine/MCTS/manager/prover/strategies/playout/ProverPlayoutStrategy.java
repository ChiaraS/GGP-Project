package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.PnStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverSimulationResult;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;

public interface ProverPlayoutStrategy extends PnStrategy{

	public ProverSimulationResult playout(ExplicitMachineState state, int maxDepth);
}
