package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.Strategy;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

public interface PlayoutStrategy extends Strategy{

	public int[] playout(InternalPropnetMachineState state, int[] playoutVisitedNodes, int maxDepth);
}
