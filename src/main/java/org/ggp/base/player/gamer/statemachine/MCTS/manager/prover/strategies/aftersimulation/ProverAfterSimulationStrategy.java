package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverSimulationResult;

public interface ProverAfterSimulationStrategy extends Strategy {

	public void afterSimulationActions(ProverSimulationResult simulationResult);

}
