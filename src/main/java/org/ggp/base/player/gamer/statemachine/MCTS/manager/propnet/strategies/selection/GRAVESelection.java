package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.GRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.AMAFDecoupled.PnAMAFNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class GRAVESelection extends MoveValueSelection {

	/**
	 * Minimum number of visits that the node must have to be allowed to use its own AMAF statistics.
	 */
	private int minAMAFVisits;

	public GRAVESelection(int numRoles, InternalPropnetRole myRole,
			Random random, double valueOffset, int minAMAFVisits, GRAVEEvaluator moveEvaluator) {

		super(numRoles, myRole, random, valueOffset, moveEvaluator);

		this.minAMAFVisits = minAMAFVisits;

	}

	@Override
	public MCTSJointMove select(MCTSNode currentNode) {

		//System.out.println("GRAVE selection");

		if(currentNode instanceof PnAMAFNode){

			//System.out.println("tot node visits: " + currentNode.getTotVisits());

			// TODO: uncomment the check. This will make sure that if no stats have visits higher than the threshold at least
			// the root stats will be used rather than ignoring amaf values.
			if((((GRAVEEvaluator)this.moveEvaluator).getCloserAmafStats()) == null || currentNode.getTotVisits() >= this.minAMAFVisits){

				//if((((GRAVEEvaluator)this.moveEvaluator).getCloserAmafStats()) == null){
				//	System.out.print("Null reference: ");
				//}
				//System.out.println("change");
				((GRAVEEvaluator)this.moveEvaluator).setCloserAmafStats(((PnAMAFNode)currentNode).getAmafStats());
			}

			return super.select(currentNode);

		}else{
			throw new RuntimeException("GRAVESelection-select(): detected a node not implementing interface PnGRAVENode.");
		}
	}

	public void resetCloserAmafStats(){
		((GRAVEEvaluator)this.moveEvaluator).setCloserAmafStats(null);
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
