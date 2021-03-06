package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.StandardUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;

public class AdaptivePlayoutBackpropagation extends BackpropagationStrategy {

	private StandardUpdater standardUpdater;

	public AdaptivePlayoutBackpropagation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.standardUpdater = new StandardUpdater(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.standardUpdater.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		this.standardUpdater.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.standardUpdater.setUpComponent();
	}

	@Override
	public void update(MctsNode currentNode, MachineState currentState, MctsJointMove jointMove, SimulationResult[] simulationResult) throws MoveDefinitionException, StateMachineException {

		this.standardUpdater.update(currentNode, currentState, jointMove, simulationResult);

		// Add current joint move and all legal moves for all players in the current state to the simulation result(s)
		for(int i = 0; i < simulationResult.length; i++){
			simulationResult[i].addJointMove(jointMove.getJointMove());
			simulationResult[i].addLegalMovesOfAllRoles(((DecoupledMctsNode)currentNode).getAllLegalMoves());
		}

	}

	@Override
	public void processPlayoutResult(MctsNode leafNode, MachineState leafState,	SimulationResult[] simulationResult) {
		// Do nothing
	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "NODE_UPDATER = " + this.standardUpdater.printComponent(indentation + "  ");
	}


}
