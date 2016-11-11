package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;

public abstract class PlayoutStrategy extends Strategy {

	public PlayoutStrategy(GameDependentParameters gameDependentParameters) {
		super(gameDependentParameters);
	}

	public abstract SimulationResult playout(MachineState state, int maxDepth);

}
