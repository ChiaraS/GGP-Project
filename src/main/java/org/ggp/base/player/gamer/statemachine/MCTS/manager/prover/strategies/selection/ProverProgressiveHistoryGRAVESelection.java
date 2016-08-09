package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.GRAVE.ProverProgressiveHistoryGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.AMAFDecoupled.ProverAMAFNode;
import org.ggp.base.util.statemachine.Role;

public class ProverProgressiveHistoryGRAVESelection extends
		ProverGRAVESelection {

	public ProverProgressiveHistoryGRAVESelection(int numRoles, Role myRole,
			Random random, double valueOffset, int minAMAFVisits,
			ProverProgressiveHistoryGRAVEEvaluator moveEvaluator) {
		super(numRoles, myRole, random, valueOffset, minAMAFVisits,
				moveEvaluator);
	}

	@Override
	public ProverMCTSJointMove select(MCTSNode currentNode) {

		if(currentNode instanceof ProverAMAFNode){

			/** 1. Set the AMAF table of the root for the progressive history */

			if(((ProverProgressiveHistoryGRAVEEvaluator)this.moveEvaluator).getCurrentRootAmafStats() == null){

				// First time we are selecting for the current game step, thus this node must be the root
				((ProverProgressiveHistoryGRAVEEvaluator)this.moveEvaluator).setCurrentRootAmafStats(((ProverAMAFNode)currentNode).getAmafStats());

			}

			/** 2. Call the GRAVE selection **/
			return super.select(currentNode);

		}else{
			throw new RuntimeException("ProverProgressiveHistoryGRAVESelection-select(): detected a node not implementing interface ProverGRAVENode.");
		}
	}

	public void resetCurrentRootAmafStats(){
		((ProverProgressiveHistoryGRAVEEvaluator)this.moveEvaluator).setCurrentRootAmafStats(null);
	}

}
