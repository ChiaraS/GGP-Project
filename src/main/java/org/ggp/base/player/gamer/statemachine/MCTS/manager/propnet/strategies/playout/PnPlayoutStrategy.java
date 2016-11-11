package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.PnStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;

public interface PnPlayoutStrategy extends PnStrategy{

	public PnSimulationResult playout(CompactMachineState state, int maxDepth);
}
