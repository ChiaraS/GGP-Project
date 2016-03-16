package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection;

import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.PnMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.GRAVEDUCT.PnGRAVENode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class GRAVESelection extends MoveValueSelection {

	/**
	 * Minimum number of visits that the node must have to be allowed to use its own AMAF statistics.
	 */
	private int minAMAFVisits;

	/**
	 * Reference to the AMAF statistics of the ancestor of this node that has enough visits
	 * to make the statistics reliable. This reference will be updated every time the current
	 * node being checked has enough visits to use its own AMAF statistics.
	 */
	private Map<InternalPropnetMove, MoveStats> amafStats;

	private double bias;

	public GRAVESelection(int numRoles, InternalPropnetRole myRole,
			Random random, double valueOffset, int minAMAFVisits, double bias) {
		super(numRoles, myRole, random, valueOffset);

		this.minAMAFVisits =  minAMAFVisits;
		this.amafStats = null;
		this.bias = bias;
	}

	@Override
	public MCTSJointMove select(PnMCTSNode currentNode) {
		if(currentNode instanceof PnGRAVENode){

			if(currentNode.getTotVisits() > this.minAMAFVisits){
				this.amafStats = ((PnGRAVENode)currentNode).getAmafStats();
			}

			return super.select(currentNode);

		}else{
			throw new RuntimeException("GRAVESelection-select(): detected a node not implementing interface PnGRAVENode.");
		}
	}

	@Override
	public double computeMoveValue(PnMCTSNode theNode,
			InternalPropnetMove theMove, MoveStats theMoveStats) {

		double moveVisits = theMoveStats.getVisits();
		double moveScore = theMoveStats.getScoreSum();
		double amafVisits = 0.0;
		double amafScore = 0.0;

		if(this.amafStats != null){
			MoveStats moveAmaf = this.amafStats.get(theMove);
			if(moveAmaf != null){
				amafVisits = moveAmaf.getVisits();
				amafScore = moveAmaf.getScoreSum();
			}
		}

		if(moveVisits == 0){
			if(amafVisits == 0){
				// If the move hasn't been explored ever (i.e. not in this node nor in any descendant of the node
				// whose AMAF table we are using), return the maximum value to encourage exploration of this move.
				return Double.MAX_VALUE;
			}else{
				// If average cannot be computed, return only AMAF average.
				return ((amafScore / amafVisits) / 100.0);
			}
		}else{
			if(amafVisits == 0){
				// If the AMAF average cannot be computed, return only the move average.
				return ((moveScore / moveVisits) / 100.0);
			}else{
				double beta = amafVisits / (amafVisits + moveVisits + (this.bias * amafVisits * moveVisits));
				return ((1.0 - beta) * ((moveScore / moveVisits) / 100.0)) + (beta * ((amafScore / amafVisits) / 100.0));
			}
		}

	}


	public void resetAmafStats(){
		this.amafStats = null;
	}

	@Override
	public String getStrategyParameters(){
		String params = super.getStrategyParameters();

		if(params == null){
			return "MIN_AMAF_VISITS = " + this.minAMAFVisits + ", BIAS = " + this.bias;
		}else{
			return params + ", MIN_AMAF_VISITS = " + this.minAMAFVisits + ", BIAS = " + this.bias;
		}
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[SELECTION_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[SELECTION_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
