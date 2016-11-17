package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.tddecoupled;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledTreeNodeFactory;

public class TDDecoupledTreeNodeFactory extends DecoupledTreeNodeFactory {

	public TDDecoupledTreeNodeFactory(GameDependentParameters gameDependentParameters, Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);
	}

	@Override
	protected MCTSNode createActualNewNode(DecoupledMCTSMoveStats[][] ductMovesStats, int[] goals, boolean terminal) {
		return new TDDecoupledMCTSNode(ductMovesStats, goals, terminal, this.gameDependentParameters.getTheMachine().getRoles().size());
	}

}
