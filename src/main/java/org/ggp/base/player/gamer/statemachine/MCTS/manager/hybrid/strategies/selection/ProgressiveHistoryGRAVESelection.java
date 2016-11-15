package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.util.Properties;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.ProgressiveHistoryGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled.AMAFNode;

public class ProgressiveHistoryGRAVESelection extends GRAVESelection {

	public ProgressiveHistoryGRAVESelection(GameDependentParameters gameDependentParameters, Random random, Properties properties, SharedReferencesCollector sharedReferencesCollector, double valueOffset,
			int minAMAFVisits, ProgressiveHistoryGRAVEEvaluator moveEvaluator) {
		super(gameDependentParameters, random, properties, sharedReferencesCollector, valueOffset, minAMAFVisits, moveEvaluator);

	}

	@Override
	public MCTSJointMove select(MCTSNode currentNode) {

		if(currentNode instanceof AMAFNode){

			/** 1. Set the AMAF table of the root for the progressive history */

			if(((ProgressiveHistoryGRAVEEvaluator)this.moveEvaluator).getCurrentRootAmafStats() == null){

				// First time we are selecting for the current game step, thus this node must be the root
				((ProgressiveHistoryGRAVEEvaluator)this.moveEvaluator).setCurrentRootAmafStats(((AMAFNode)currentNode).getAmafStats());

			}

			/** 2. Call the GRAVE selection **/
			return super.select(currentNode);

		}else{
			throw new RuntimeException("ProgressiveHistoryGRAVESelection-select(): detected a node not implementing interface MCTSNode.");
		}
	}

	public void resetCurrentRootAmafStats(){
		((ProgressiveHistoryGRAVEEvaluator)this.moveEvaluator).setCurrentRootAmafStats(null);
	}

}
