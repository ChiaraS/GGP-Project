package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.amafdecoulped;

import java.util.HashMap;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.decoupled.ProverDecoupledMCTSMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.prover.decoupled.ProverDecoupledMCTSNode;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public class ProverAMAFDecoupledMCTSNode  extends ProverDecoupledMCTSNode implements ProverAMAFNode{

	/**
	 * Table that collects the AMAF statistics for the node for each move.
	 */
	private Map<ExplicitMove, MoveStats> amafStats;

	public ProverAMAFDecoupledMCTSNode(ProverDecoupledMCTSMoveStats[][] movesStats, int[] goals, boolean terminal) {
		super(movesStats, goals, terminal);

		if(!terminal){
			this.amafStats = new HashMap<ExplicitMove, MoveStats>();
		} // If the node is terminal we'll never use the AMAF stats, so we can leave them pointing to null.
	}

	@Override
	public Map<ExplicitMove, MoveStats> getAmafStats(){
		return this.amafStats;
	}

	/**
	 * Returns the number of moves for which the AMAF statistics are recorded in this node.
	 *
	 * This is the number of move statistics that the GRAVE algorithm memorizes.
	 *
	 * @return
	 */
	public int getGraveAMAFStatsNumber(){
		if(this.amafStats != null){
			return this.amafStats.size();
		}else{
			return 0;
		}
	}

	/**
	 * Returns the number of moves for which the plain RAVE algorithm would memorize the AMAF statistics.
	 * Since we implement RAVE as GRAVE with minAMAFVisits=0, both algorithms will memorize the same amount
	 * of statistics per node; i.e. each node memorizes the AMAF statistics for every move encountered in all
	 * the simulations performed from that node on, because we don't know in which other node those statistics
	 * will be used. However, RAVE could be implemented separately to optimize its memory usage, memorizing
	 * in a node only the AMAF statistics of the moves that are legal in the node (the number of AMAF statistics
	 * will be equal to the number of legal actions in the node only after all of those actions will have been
	 * encountered at least once visiting the children of this node).
	 *
	 * @return
	 */
	public int getRaveAMAFStatsNumber(){

		if(this.amafStats == null || this.movesStats == null){
			return 0;
		}else{
			int totalStats = 0;
			for(int i = 0; i < this.movesStats.length; i++){
				for(int j = 0; j < this.movesStats[i].length; j++){
					if(this.amafStats.get(this.movesStats[i][j].getTheMove()) != null){
						totalStats++;
					}
				}
			}
			return totalStats;
		}
	}

	/**
	 * Returns the number of moves' statistics in this node (it always corresponds to the total
	 * number of legal moves in the node).
	 *
	 * @return
	 */
	public int getActionsStatsNumber(){

		if(this.movesStats == null){
			return 0;
		}else{
			int totalStats = 0;
			for(int i = 0; i < this.movesStats.length; i++){
				totalStats += this.movesStats[i].length;
			}
			return totalStats;
		}
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
