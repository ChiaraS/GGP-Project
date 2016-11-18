package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.PnStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;

public interface PnBackpropagationStrategy extends PnStrategy {

	/**
	 * Method that updates the given node in the tree with the result of the simulation.
	 *
	 * @param currentNode the node to update.
	 * @param jointMove the joint move played in the node.
	 * @param nextState the state reached by playing the given joint move.
	 * @param simulationResult the result obtained by the simulation that passed by this node.
	 */
	public void update(MctsNode currentNode, CompactMachineState currentState, PnMCTSJointMove jointMove, PnSimulationResult simulationResult);

	/**
	 * Method that processes the result of the playout if needed for the chosen selection and playout strategies.
	 * This action should take place only once at the end of the playout and before starting the backpropagation
	 * of the results in the tree.
	 *
	 * E.g. use here the result of the playout to update the MAST statistics, or decay here the results depending
	 * on the length of the playout, ecc...
	 *
	 * @param leafNode the node from which the playout was started.
	 * @param leafState state corresponding to the leaf node.
	 * @param simulationResult the result of the playout starting from the terminal state up to the leaf node.
	 */
	public void processPlayoutResult(MctsNode leafNode, CompactMachineState leafState, PnSimulationResult simulationResult);

}
