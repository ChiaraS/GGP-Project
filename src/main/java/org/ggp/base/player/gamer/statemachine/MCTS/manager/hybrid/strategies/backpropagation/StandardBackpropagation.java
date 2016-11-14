package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.StandardUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;

public class StandardBackpropagation extends BackpropagationStrategy {

	private StandardUpdater standardUpdater;

	public StandardBackpropagation(GameDependentParameters gameDependentParameters, StandardUpdater standardUpdater){
		super(gameDependentParameters);

		this.standardUpdater = standardUpdater;
	}

	@Override
	public void clearComponent() {
		this.standardUpdater.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.standardUpdater.setUpComponent();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.BackpropagationStrategy#update(org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode, org.ggp.base.util.statemachine.structure.MachineState, org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove, org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult)
	 */
	@Override
	public void update(MCTSNode currentNode, MachineState currentState, MCTSJointMove jointMove, SimulationResult simulationResult){
		this.standardUpdater.update(currentNode, currentState, jointMove, simulationResult);
	}

	@Override
	public void processPlayoutResult(MCTSNode leafNode, MachineState leafState,	SimulationResult simulationResult) {
		this.standardUpdater.processPlayoutResult(leafNode, leafState, simulationResult);
	}

	@Override
	public String getStrategyParameters() {
		return "(UPDATER = " + this.standardUpdater.printNodeUpdater() + ")";
	}

}
