package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

public interface PlayoutStrategy extends Strategy{

	public int[] playout(InternalPropnetMachineState state, int[] playoutVisitedNodes, int maxDepth);
}
