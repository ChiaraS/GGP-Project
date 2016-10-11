package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.SimulationResult;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;

public class IntermediateTDBackpropagation extends TDBackpropagation {

	/**
	 * Index of the episode being currently considered in the computation of the backpropagation values.
	 * Corresponds to (simulationLength - episode depth in the game tree).
	 */
	protected int playoutIndex;

	public IntermediateTDBackpropagation(InternalPropnetStateMachine theMachine, int numRoles, double qPlayout,
			double lambda, double gamma) {
		super(theMachine, numRoles, qPlayout, lambda, gamma);

		this.playoutIndex = 0;

	}

	@Override
	public void update(MCTSNode currentNode, MCTSJointMove jointMove,
			InternalPropnetMachineState nextState,
			SimulationResult simulationResult) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processPlayoutResult(MCTSNode leafNode,
			InternalPropnetMachineState leafState,
			SimulationResult simulationResult) {
		// TODO Auto-generated method stub

	}

	@Override
	public int[] getReturnValuesForRolesInPlayout(
			SimulationResult simulationResult) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetSimulationParameters(){

		super.resetSimulationParameters();

		this.playoutIndex = 0;

	}

}
