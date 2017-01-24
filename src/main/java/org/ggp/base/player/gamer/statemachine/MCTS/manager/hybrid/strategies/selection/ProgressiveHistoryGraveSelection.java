package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.ProgressiveHistoryGraveEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled.AmafNode;

public class ProgressiveHistoryGraveSelection extends GraveSelection {

	public ProgressiveHistoryGraveSelection(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		sharedReferencesCollector.setProgressiveHistoryGraveSelection(this);

	}

	@Override
	public void preSelectionActions(MctsNode currentNode) {
		super.preSelectionActions(currentNode);

		if(currentNode instanceof AmafNode){

			if(((ProgressiveHistoryGraveEvaluator)this.moveEvaluator).getCurrentRootAmafStats() == null){

				// First time we are selecting for the current game step, thus this node must be the root
				((ProgressiveHistoryGraveEvaluator)this.moveEvaluator).setCurrentRootAmafStats(((AmafNode)currentNode).getAmafStats());

			}

		}else{
			throw new RuntimeException("ProgressiveHistoryGraveSelection-preSelectionActions(): detected a node not implementing interface AmafNode.");
		}

	}

	public void resetCurrentRootAmafStats(){
		((ProgressiveHistoryGraveEvaluator)this.moveEvaluator).setCurrentRootAmafStats(null);
	}

}
