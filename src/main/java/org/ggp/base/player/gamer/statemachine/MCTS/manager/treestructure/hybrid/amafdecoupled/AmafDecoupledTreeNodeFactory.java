package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledTreeNodeFactory;

public class AmafDecoupledTreeNodeFactory extends DecoupledTreeNodeFactory {

	public AmafDecoupledTreeNodeFactory(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerConfiguration, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);
	}

	@Override
	protected MctsNode createActualNewNode(DecoupledMctsMoveStats[][] ductMovesStats, int[] goals, boolean terminal) {
		return new AmafDecoupledMctsNode(ductMovesStats, goals, terminal);
	}

}
