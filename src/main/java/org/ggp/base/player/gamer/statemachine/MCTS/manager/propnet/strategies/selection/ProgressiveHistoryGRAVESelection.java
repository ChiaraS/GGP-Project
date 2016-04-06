package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.ProgressiveHistoryGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.AMAFDecoupled.PnAMAFNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class ProgressiveHistoryGRAVESelection extends GRAVESelection{

	public ProgressiveHistoryGRAVESelection(int numRoles, InternalPropnetRole myRole,
			Random random, double valueOffset, int minAMAFVisits, ProgressiveHistoryGRAVEEvaluator moveEvaluator) {
		super(numRoles, myRole, random, valueOffset, minAMAFVisits, moveEvaluator);

	}

	@Override
	public MCTSJointMove select(MCTSNode currentNode) {

		if(currentNode instanceof PnAMAFNode){

			/** 1. Set the AMAF table of the root for the progressive history */

			if(((ProgressiveHistoryGRAVEEvaluator)this.moveEvaluator).getCurrentRootAmafStats() == null){

				// First time we are selecting for the current game step, thus this node must be the root
				((ProgressiveHistoryGRAVEEvaluator)this.moveEvaluator).setCurrentRootAmafStats(((PnAMAFNode)currentNode).getAmafStats());

			}

			/** 2. Call the GRAVE selection **/
			return super.select(currentNode);

		}else{
			throw new RuntimeException("ProgressiveHistoryGRAVESelection-select(): detected a node not implementing interface PnGRAVENode.");
		}
	}

	public void resetCurrentRootAmafStats(){
		((ProgressiveHistoryGRAVEEvaluator)this.moveEvaluator).setCurrentRootAmafStats(null);
	}

}
