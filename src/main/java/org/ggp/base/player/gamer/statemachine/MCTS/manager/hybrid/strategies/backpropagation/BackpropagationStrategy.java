package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.Strategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;

public abstract class BackpropagationStrategy extends Strategy {

	public BackpropagationStrategy(GameDependentParameters gameDependentParameters, Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);
	}

	/**
	 * Method that updates the given node in the tree with the result of the simulation.
	 *
	 * @param currentNode the node to update.
	 * @param jointMove the joint move played in the node.
	 * @param nextState the state reached by playing the given joint move.
	 * @param simulationResult the result obtained by the simulation that passed by this node.
	 */
	public abstract void update(MCTSNode currentNode, MachineState currentState, MCTSJointMove jointMove, SimulationResult simulationResult);

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
	public abstract void processPlayoutResult(MCTSNode leafNode, MachineState leafState, SimulationResult simulationResult);


	@Override
	public String printComponent() {
		String params = this.getComponentParameters();
		if(params != null){
			return "[BACKPROPAGATION_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[BACKPROPAGATION_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
