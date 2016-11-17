package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.MASTUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.StandardUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;

public class MASTBackpropagation extends BackpropagationStrategy {

	private StandardUpdater standardUpdater;

	private MASTUpdater mastUpdater;

	public MASTBackpropagation(GameDependentParameters gameDependentParameters, Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

		this.standardUpdater = new StandardUpdater(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);
		this.mastUpdater = new MASTUpdater(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.standardUpdater.setReferences(sharedReferencesCollector);
		this.mastUpdater.setReferences(sharedReferencesCollector);
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
	public String getComponentParameters() {
		return "(UPDATER_1 = " + this.standardUpdater.printComponent() + ", UPDATER_2 = " + this.mastUpdater.printComponent() + ")";
	}

}
