package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverSimulationResult;
import org.ggp.base.util.statemachine.MachineState;

public interface ProverBackpropagationStrategy  extends Strategy {

	public void update(MCTSNode node, ProverMCTSJointMove jointMove, MachineState nextState, ProverSimulationResult simulationResult);

	public void processPlayoutResult(MCTSNode leafNode, ProverSimulationResult simulationResult);

}
