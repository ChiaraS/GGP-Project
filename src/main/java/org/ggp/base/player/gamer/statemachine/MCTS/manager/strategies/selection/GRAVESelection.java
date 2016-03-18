package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.evaluators.GRAVE.BetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.evaluators.GRAVE.GRAVEEvaluator;
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
			Random random, double valueOffset, double c, double defaultValue, int minAMAFVisits, BetaComputer betaComputer) {

		super(numRoles, myRole, random, valueOffset, new GRAVEEvaluator(c, defaultValue, betaComputer));

		this.minAMAFVisits =  minAMAFVisits;

	}

	@Override
	public MCTSJointMove select(PnMCTSNode currentNode) {

		//System.out.println("GRAVE selection");

		if(currentNode instanceof PnAMAFNode){

			//System.out.println("tot node visits: " + currentNode.getTotVisits());

			// TODO: uncomment the check. This will make sure that if no stats have visits higher than the threshold at least
			// the root stats will be used rather than ignoring amaf values.
			if(/*(((GRAVEEvaluator)this.moveEvaluator).getAmafStats()) == null ||*/ currentNode.getTotVisits() > this.minAMAFVisits){

				//System.out.println("change");
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
