package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.amafdecoupled.PnAMAFNode;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;

public class PnGRAVESelection extends PnMoveValueSelection implements OnlineTunableComponent{

	/**
	 * Minimum number of visits that the node must have to be allowed to use its own AMAF statistics.
	 */
	private int minAMAFVisits;

	public PnGRAVESelection(int numRoles, CompactRole myRole,
			Random random, double valueOffset, int minAMAFVisits, PnGRAVEEvaluator moveEvaluator) {

		super(numRoles, myRole, random, valueOffset, moveEvaluator);

		this.minAMAFVisits = minAMAFVisits;

	}

	@Override
	public PnMCTSJointMove select(MCTSNode currentNode) {

		//System.out.println("GRAVE selection");

		if(currentNode instanceof PnAMAFNode){

			//System.out.println("tot node visits: " + currentNode.getTotVisits());

			// This will make sure that if no stats have visits higher than the threshold at least
			// the root stats will be used rather than ignoring amaf values.
			if((((PnGRAVEEvaluator)this.moveEvaluator).getCloserAmafStats()) == null || currentNode.getTotVisits() >= this.minAMAFVisits){

				//if((((GRAVEEvaluator)this.moveEvaluator).getCloserAmafStats()) == null){
				//	System.out.print("Null reference: ");
				//}
				//System.out.println("change");
				((PnGRAVEEvaluator)this.moveEvaluator).setCloserAmafStats(((PnAMAFNode)currentNode).getAmafStats());
			}

			return super.select(currentNode);

		}else{
			throw new RuntimeException("GRAVESelection-select(): detected a node not implementing interface PnGRAVENode.");
		}
	}

	public void resetCloserAmafStats(){
		((PnGRAVEEvaluator)this.moveEvaluator).setCloserAmafStats(null);
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
	public void setNewValues(double[] newValue) {

		this.minAMAFVisits = (int) newValue[0];

	}

	@Override
	public String printOnlineTunableComponent() {

		return "(ONLINE_TUNABLE_COMPONENT = " + this.printStrategy() + ")";

	}

}
