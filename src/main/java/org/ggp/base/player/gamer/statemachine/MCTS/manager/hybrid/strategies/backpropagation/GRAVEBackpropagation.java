package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.GRAVEUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.StandardUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;

public class GRAVEBackpropagation extends BackpropagationStrategy {

	private StandardUpdater standardUpdater;

	private GRAVEUpdater graveUpdater;

	public GRAVEBackpropagation(GameDependentParameters gameDependentParameters) {

		super(gameDependentParameters);

		this.standardUpdater = new StandardUpdater(gameDependentParameters);
		this.graveUpdater = new GRAVEUpdater(gameDependentParameters);
	}

	@Override
	public void clearComponent() {
		this.standardUpdater.clearComponent();
		this.graveUpdater.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.standardUpdater.setUpComponent();
		this.graveUpdater.setUpComponent();
	}


	@Override
	public void update(MCTSNode currentNode, MachineState currentState, MCTSJointMove jointMove, SimulationResult simulationResult) {

		this.standardUpdater.update(currentNode, currentState, jointMove, simulationResult);
		this.graveUpdater.update(currentNode, currentState, jointMove, simulationResult);

	}

	@Override
	public void processPlayoutResult(MCTSNode leafNode, MachineState leafState, SimulationResult simulationResult) {

		this.graveUpdater.processPlayoutResult(leafNode, leafState, simulationResult);

	}

	@Override
	public String getStrategyParameters() {
		return "(UPDATER_1 = " + this.standardUpdater.printNodeUpdater() + ", UPDATER_2 = " + this.graveUpdater.printNodeUpdater() + ")";
	}

}
