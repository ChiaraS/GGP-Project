package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.GRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled.AMAFNode;

public class GRAVESelection extends MoveValueSelection implements OnlineTunableComponent{

	/**
	 * Minimum number of visits that the node must have to be allowed to use its own AMAF statistics.
	 */
	private int minAMAFVisits;

	public GRAVESelection(int numRoles, int myRoleIndex, Random random,	double valueOffset,
			int minAMAFVisits, GRAVEEvaluator moveEvaluator) {

		super(numRoles, myRoleIndex, random, valueOffset, moveEvaluator);

		this.minAMAFVisits = minAMAFVisits;

	}

	@Override
	public MCTSJointMove select(MCTSNode currentNode) {

		//System.out.println("GRAVE selection");

		if(currentNode instanceof AMAFNode){

			//System.out.println("tot node visits: " + currentNode.getTotVisits());

			// This will make sure that if no stats have visits higher than the threshold at least
			// the root stats will be used rather than ignoring amaf values.
			if((((GRAVEEvaluator)this.moveEvaluator).getCloserAmafStats()) == null || currentNode.getTotVisits() >= this.minAMAFVisits){

				//if((((GRAVEEvaluator)this.moveEvaluator).getCloserAmafStats()) == null){
				//	System.out.print("Null reference: ");
				//}
				//System.out.println("change");
				((GRAVEEvaluator)this.moveEvaluator).setCloserAmafStats(((AMAFNode)currentNode).getAmafStats());
			}

			return super.select(currentNode);

		}else{
			throw new RuntimeException("GRAVESelection-select(): detected a node not implementing interface AMAFNode.");
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

	@Override
	public void setNewValue(double newValue) {

		this.minAMAFVisits = (int) newValue;

	}

	@Override
	public String printOnlineTunableComponent() {

		return "(ONLINE_TUNABLE_COMPONENT = " + this.printStrategy() + ")";

	}

}
