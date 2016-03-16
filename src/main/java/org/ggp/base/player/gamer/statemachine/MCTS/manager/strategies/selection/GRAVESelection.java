package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.evaluators.GRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.PnMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.AMAFDecoupled.PnAMAFNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class GRAVESelection extends MoveValueSelection {

	/**
	 * Minimum number of visits that the node must have to be allowed to use its own AMAF statistics.
	 */
	private int minAMAFVisits;

	public GRAVESelection(int numRoles, InternalPropnetRole myRole,
			Random random, double valueOffset, int minAMAFVisits, double bias) {

		super(numRoles, myRole, random, valueOffset, new GRAVEEvaluator(bias));

		this.minAMAFVisits =  minAMAFVisits;

	}

	@Override
	public MCTSJointMove select(PnMCTSNode currentNode) {
		if(currentNode instanceof PnAMAFNode){

			if(currentNode.getTotVisits() > this.minAMAFVisits){
				((GRAVEEvaluator)this.moveEvaluator).setAmafStats(((PnAMAFNode)currentNode).getAmafStats());
			}

			return super.select(currentNode);

		}else{
			throw new RuntimeException("GRAVESelection-select(): detected a node not implementing interface PnGRAVENode.");
		}
	}

	public void resetAmafStats(){
		((GRAVEEvaluator)this.moveEvaluator).setAmafStats(null);
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
