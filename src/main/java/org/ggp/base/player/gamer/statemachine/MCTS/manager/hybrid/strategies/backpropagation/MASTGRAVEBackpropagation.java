package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation;

import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.GRAVEUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.MASTUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.StandardUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class MASTGRAVEBackpropagation extends BackpropagationStrategy {

	private StandardUpdater standardUpdater;

	private MASTUpdater mastUpdater;

	private GRAVEUpdater graveUpdater;

	public MASTGRAVEBackpropagation(GameDependentParameters gameDependentParameters, Random random, Properties properties, SharedReferencesCollector sharedReferencesCollector, Map<Move, MoveStats> mastStatistics) {

		super(gameDependentParameters, random, properties, sharedReferencesCollector);

		this.standardUpdater = new StandardUpdater(gameDependentParameters, random, properties, sharedReferencesCollector);
		this.mastUpdater = new MASTUpdater(gameDependentParameters, random, properties, sharedReferencesCollector, mastStatistics);
		this.graveUpdater = new GRAVEUpdater(gameDependentParameters, random, properties, sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		this.standardUpdater.clearComponent();
		this.mastUpdater.clearComponent();
		this.graveUpdater.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.standardUpdater.setUpComponent();
		this.mastUpdater.setUpComponent();
		this.graveUpdater.setUpComponent();
	}

	@Override
	public void update(MCTSNode currentNode, MachineState currentState, MCTSJointMove jointMove, SimulationResult simulationResult) {

		this.standardUpdater.update(currentNode, currentState, jointMove, simulationResult);
		this.mastUpdater.update(currentNode, currentState, jointMove, simulationResult);
		this.graveUpdater.update(currentNode, currentState, jointMove, simulationResult);

	}

	@Override
	public void processPlayoutResult(MCTSNode leafNode, MachineState leafState, SimulationResult simulationResult) {

		//this.standardUpdater.processPlayoutResult(leafNode, leafState, simulationResult);
		this.mastUpdater.processPlayoutResult(leafNode, leafState, simulationResult);
		this.graveUpdater.processPlayoutResult(leafNode, leafState, simulationResult);


	}

	@Override
	public String getComponentParameters() {
		return "(UPDATER_1 = " + this.standardUpdater.printComponent() + ", UPDATER_2 = "  + this.mastUpdater.printComponent() + ", UPDATER_3 = " + this.graveUpdater.printComponent() + ")";
	}

}
