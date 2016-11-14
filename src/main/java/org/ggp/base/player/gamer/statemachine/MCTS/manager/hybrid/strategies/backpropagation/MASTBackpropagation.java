package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation;

import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.MASTUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.StandardUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class MASTBackpropagation extends BackpropagationStrategy {

	private StandardUpdater standardUpdater;

	private MASTUpdater mastUpdater;

	public MASTBackpropagation(GameDependentParameters gameDependentParameters, Map<Move, MoveStats> mastStatistics) {

		super(gameDependentParameters);

		this.standardUpdater = new StandardUpdater(gameDependentParameters);
		this.mastUpdater = new MASTUpdater(gameDependentParameters, mastStatistics);
	}

	@Override
	public void clearComponent() {
		this.standardUpdater.clearComponent();
		this.mastUpdater.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.standardUpdater.setUpComponent();
		this.mastUpdater.setUpComponent();
	}

	@Override
	public void update(MCTSNode currentNode, MachineState currentState, MCTSJointMove jointMove, SimulationResult simulationResult) {

		this.standardUpdater.update(currentNode, currentState, jointMove, simulationResult);
		this.mastUpdater.update(currentNode, currentState, jointMove, simulationResult);

	}

	@Override
	public void processPlayoutResult(MCTSNode leafNode, MachineState leafState,	SimulationResult simulationResult) {

		this.mastUpdater.processPlayoutResult(leafNode, leafState, simulationResult);

	}

	@Override
	public String getStrategyParameters() {
		return "(UPDATER_1 = " + this.standardUpdater.printNodeUpdater() + ", UPDATER_2 = " + this.mastUpdater.printNodeUpdater() + ")";
	}

}
