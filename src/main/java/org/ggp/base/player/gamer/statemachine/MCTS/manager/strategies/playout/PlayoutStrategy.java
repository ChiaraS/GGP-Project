package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.playout;

import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public interface PlayoutStrategy {

	public int[] playout(InternalPropnetMachineState state, InternalPropnetRole myRole, int[] playoutVisitedNodes, int maxDepth);
}
