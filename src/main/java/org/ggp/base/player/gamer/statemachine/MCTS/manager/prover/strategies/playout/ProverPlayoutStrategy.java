package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;
import org.ggp.base.util.statemachine.MachineState;

public interface ProverPlayoutStrategy extends Strategy{

	public int[] playout(MachineState state, int[] playoutVisitedNodes, int maxDepth);
}
