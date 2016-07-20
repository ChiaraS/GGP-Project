package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.GRAVE.ProverGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.AMAFDecoupled.ProverAMAFNode;
import org.ggp.base.util.statemachine.Role;

public class ProverGRAVESelection extends ProverMoveValueSelection {

	/**
	 * Minimum number of visits that the node must have to be allowed to use its own AMAF statistics.
	 */
	private int minAMAFVisits;

	public ProverGRAVESelection(int numRoles, Role myRole,
			Random random, double valueOffset, int minAMAFVisits, ProverGRAVEEvaluator moveEvaluator) {

		super(numRoles, myRole, random, valueOffset, moveEvaluator);

		this.minAMAFVisits = minAMAFVisits;

	}

	@Override
	public ProverMCTSJointMove select(MCTSNode currentNode) {

		//System.out.println("GRAVE selection");

		if(currentNode instanceof ProverAMAFNode){

			//System.out.println("tot node visits: " + currentNode.getTotVisits());

			// TODO: uncomment the check. This will make sure that if no stats have visits higher than the threshold at least
			// the root stats will be used rather than ignoring amaf values.
			if((((ProverGRAVEEvaluator)this.moveEvaluator).getCloserAmafStats()) == null || currentNode.getTotVisits() >= this.minAMAFVisits){

				//if((((GRAVEEvaluator)this.moveEvaluator).getCloserAmafStats()) == null){
				//	System.out.print("Null reference: ");
				//}
				//System.out.println("change");
				((ProverGRAVEEvaluator)this.moveEvaluator).setCloserAmafStats(((ProverAMAFNode)currentNode).getAmafStats());
			}

			return super.select(currentNode);

		}else{
			throw new RuntimeException("ProverGRAVESelection-select(): detected a node not implementing interface PnGRAVENode.");
		}
	}

	public void resetCloserAmafStats(){
		((ProverGRAVEEvaluator)this.moveEvaluator).setCloserAmafStats(null);
	}

	@Override
	public String getStrategyParameters(){
		String params = super.getStrategyParameters();

		if(params == null){
			return "MIN_AMAF_VISITS = " + this.minAMAFVisits;
		}else{
			return params + ", MIN_AMAF_VISITS = " + this.minAMAFVisits;
		}
	}

}