package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnProgressiveHistoryGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.amafdecoupled.PnAMAFNode;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;

public class PnProgressiveHistoryGRAVESelection extends PnGRAVESelection{

	public PnProgressiveHistoryGRAVESelection(int numRoles, CompactRole myRole,
			Random random, double valueOffset, int minAMAFVisits, PnProgressiveHistoryGRAVEEvaluator moveEvaluator) {
		super(numRoles, myRole, random, valueOffset, minAMAFVisits, moveEvaluator);

	}

	@Override
	public PnMCTSJointMove select(MctsNode currentNode) {

		if(currentNode instanceof PnAMAFNode){

			/** 1. Set the AMAF table of the root for the progressive history */

			if(((PnProgressiveHistoryGRAVEEvaluator)this.moveEvaluator).getCurrentRootAmafStats() == null){

				// First time we are selecting for the current game step, thus this node must be the root
				((PnProgressiveHistoryGRAVEEvaluator)this.moveEvaluator).setCurrentRootAmafStats(((PnAMAFNode)currentNode).getAmafStats());

			}

			/** 2. Call the GRAVE selection **/
			return super.select(currentNode);

		}else{
			throw new RuntimeException("ProgressiveHistoryGRAVESelection-select(): detected a node not implementing interface PnGRAVENode.");
		}
	}

	public void resetCurrentRootAmafStats(){
		((PnProgressiveHistoryGRAVEEvaluator)this.moveEvaluator).setCurrentRootAmafStats(null);
	}

}
