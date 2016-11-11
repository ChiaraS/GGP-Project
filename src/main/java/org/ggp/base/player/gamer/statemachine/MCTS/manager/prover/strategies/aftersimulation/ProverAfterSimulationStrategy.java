package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.PnStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.ProverSimulationResult;

public interface ProverAfterSimulationStrategy extends PnStrategy {

	public void afterSimulationActions(ProverSimulationResult simulationResult);

}
