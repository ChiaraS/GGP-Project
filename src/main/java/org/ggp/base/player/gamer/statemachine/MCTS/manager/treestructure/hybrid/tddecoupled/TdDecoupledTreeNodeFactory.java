package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.tddecoupled;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledTreeNodeFactory;

public class TdDecoupledTreeNodeFactory extends DecoupledTreeNodeFactory {

	public TdDecoupledTreeNodeFactory(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerConfiguration, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);
	}

	@Override
	protected MctsNode createActualNewNode(DecoupledMctsMoveStats[][] ductMovesStats, int[] goals, boolean terminal) {
		return new TdDecoupledMctsNode(ductMovesStats, goals, terminal, this.gameDependentParameters.getTheMachine().getRoles().size());
	}

}
