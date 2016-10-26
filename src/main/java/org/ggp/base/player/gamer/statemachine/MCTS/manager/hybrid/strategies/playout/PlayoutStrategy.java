package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.structure.MachineState;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;

public interface PlayoutStrategy extends Strategy {

	public PnSimulationResult playout(MachineState state, int maxDepth);

}
