package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

public interface PlayoutStrategy {

	public int[] playout(InternalPropnetMachineState state, int[] playoutVisitedNodes, int maxDepth);
}
