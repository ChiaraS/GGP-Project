package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.util.statemachine.structure.Move;

public class AmafDecoupledMctsNode extends DecoupledMctsNode implements AmafNode {

	/**
	 * Table that collects the AMAF statistics for the node for each move.
	 */
	private Map<Move, MoveStats> amafStats;

	public AmafDecoupledMctsNode(DecoupledMctsMoveStats[][] movesStats, double[] goals, boolean terminal, int numRoles) {
		super(movesStats, goals, terminal, numRoles);

		if(!terminal){
			this.amafStats = new HashMap<Move, MoveStats>();
		} // If the node is terminal we'll never use the AMAF stats, so we can leave them pointing to null.
	}

	@Override
	public Map<Move, MoveStats> getAmafStats(){
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
	 * Since we implement RAVE as GRAVE with ref=0, both algorithms will memorize the same amount
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

	public void decayAmafStatistics(double decayFactor){
		if(this.amafStats != null){
			if(decayFactor == 0.0){ // If we want to throw away everything, we just clear all the stats. No need to iterate.
				this.amafStats.clear();
			}else if(decayFactor != 1.0){ // If the decay factor is 1.0 we keep everything without modifying anything.
				Iterator<Entry<Move,MoveStats>> iterator = this.amafStats.entrySet().iterator();
				Entry<Move,MoveStats> theEntry;
				while(iterator.hasNext()){
					theEntry = iterator.next();
					theEntry.getValue().decreaseByFactor(decayFactor);
					if(theEntry.getValue().getVisits() == 0){
						iterator.remove();
					}
				}
			}
		}
	}

	@Override
	public String toString(){
		String superString = super.toString();

		String toReturn = "AMAF" + superString.substring(0, superString.length()-2) + "\n  AmafStats[";

		if(this.amafStats == null){
			toReturn += "null]\n";
		}else{
			for(Entry<Move, MoveStats> amafStat : this.amafStats.entrySet()){
				toReturn += "\n    MOVE(" + amafStat.getKey().toString() + "), " + amafStat.getValue().toString();
			}
			toReturn += "  ]\n";
		}

		toReturn += "]";

		return toReturn;

	}


	/*
	public void printAMAF(){
		System.out.println("AMAF stats of node - size: " + this.amafStats.size());

		System.out.println("");
		System.out.println("[");


		for(Entry<Move, MoveStats> e : this.amafStats.entrySet()){

			System.out.println("(" + e.getKey() + ", " + e.getValue().getVisits() + ", " + e.getValue().getScoreSum() + ")");

		}

		System.out.println("]");
	}
	*/

}
