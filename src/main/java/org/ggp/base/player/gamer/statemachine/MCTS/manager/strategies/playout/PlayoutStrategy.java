package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import org.ggp.base.util.statemachine.implementation.internalPropnet.structure.InternalPropnetMachineState;

public interface PlayoutStrategy {

	public int[] playout(InternalPropnetMachineState state, int[] playoutVisitedNodes, int maxDepth);
}
