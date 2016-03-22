package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.AMAFDecoupled;

import java.util.HashMap;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.decoupled.DecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.decoupled.PnDecoupledMCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class PnAMAFDecoupledMCTSNode extends PnDecoupledMCTSNode implements PnAMAFNode{

	/**
	 * Table that collects the AMAF statistics for the node for each move.
	 */
	private Map<InternalPropnetMove, MoveStats> amafStats;

	public PnAMAFDecoupledMCTSNode(DecoupledMCTSMoveStats[][] movesStats, int[] goals, boolean terminal) {
		super(movesStats, goals, terminal);

		if(!terminal){
			this.amafStats = new HashMap<InternalPropnetMove, MoveStats>();
		} // If the node is terminal we'll never use the AMAF stats, so we can leave them pointing to null.
	}

	@Override
	public Map<InternalPropnetMove, MoveStats> getAmafStats(){
		return this.amafStats;
	}

	/*
	public void printAMAF(){
		System.out.println("AMAF stats of node - size: " + this.amafStats.size());

		System.out.println("");
		System.out.println("[");


		for(Entry<InternalPropnetMove, MoveStats> e : this.amafStats.entrySet()){

			System.out.println("(" + e.getKey() + ", " + e.getValue().getVisits() + ", " + e.getValue().getScoreSum() + ")");

		}

		System.out.println("]");
	}
	*/

}
