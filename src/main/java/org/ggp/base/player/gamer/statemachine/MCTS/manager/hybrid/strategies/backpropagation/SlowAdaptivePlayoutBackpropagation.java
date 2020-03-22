package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.SlowAdaptivePlayoutUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.StandardUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;

public class SlowAdaptivePlayoutBackpropagation extends BackpropagationStrategy {

	private StandardUpdater standardUpdater;

	private SlowAdaptivePlayoutUpdater adaptivePlayoutUpdater;

	public SlowAdaptivePlayoutBackpropagation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.standardUpdater = new StandardUpdater(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		this.adaptivePlayoutUpdater = new SlowAdaptivePlayoutUpdater(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.standardUpdater.setReferences(sharedReferencesCollector);
		this.adaptivePlayoutUpdater.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		this.standardUpdater.clearComponent();
		this.adaptivePlayoutUpdater.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.standardUpdater.setUpComponent();
		this.adaptivePlayoutUpdater.setUpComponent();
	}

	@Override
	public void update(MctsNode currentNode, MachineState currentState, MctsJointMove jointMove, SimulationResult[] simulationResult) throws MoveDefinitionException, StateMachineException {

		this.standardUpdater.update(currentNode, currentState, jointMove, simulationResult);
		this.adaptivePlayoutUpdater.update(currentNode, currentState, jointMove, simulationResult);

	}

	@Override
	public void processPlayoutResult(MctsNode leafNode, MachineState leafState,	SimulationResult[] simulationResult) {

		this.adaptivePlayoutUpdater.processPlayoutResult(leafNode, leafState, simulationResult);

	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "NODE_UPDATER_1 = " + this.standardUpdater.printComponent(indentation + "  ") + indentation + "NODE_UPDATER_2 = " + this.adaptivePlayoutUpdater.printComponent(indentation + "  ");
	}


}
