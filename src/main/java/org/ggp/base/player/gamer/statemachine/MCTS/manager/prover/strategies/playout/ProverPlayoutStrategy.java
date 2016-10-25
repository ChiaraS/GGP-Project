package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverSimulationResult;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;

public interface ProverPlayoutStrategy extends Strategy{

	public ProverSimulationResult playout(ProverMachineState state, int maxDepth);
}
