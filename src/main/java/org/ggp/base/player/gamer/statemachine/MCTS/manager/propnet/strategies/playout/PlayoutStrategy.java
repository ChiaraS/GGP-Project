package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SimulationResult;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

public interface PlayoutStrategy extends Strategy{

	public SimulationResult playout(InternalPropnetMachineState state, int maxDepth);
}
