package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverSimulationResult;

public interface ProverAfterSimulationStrategy extends Strategy {

	public void afterSimulationActions(ProverSimulationResult simulationResult);

}
