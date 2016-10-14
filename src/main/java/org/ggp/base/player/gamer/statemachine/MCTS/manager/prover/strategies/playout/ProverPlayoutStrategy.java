package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverSimulationResult;
import org.ggp.base.util.statemachine.MachineState;

public interface ProverPlayoutStrategy extends Strategy{

	public ProverSimulationResult playout(MachineState state, int maxDepth);
}
