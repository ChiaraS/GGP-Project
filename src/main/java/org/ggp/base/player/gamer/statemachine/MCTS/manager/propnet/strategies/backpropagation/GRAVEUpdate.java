package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.PnMCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.AMAFDecoupled.PnAMAFNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class GRAVEUpdate {

	private List<List<InternalPropnetMove>> allJointMoves;

	public GRAVEUpdate(List<List<InternalPropnetMove>> allJointMoves) {
		this.allJointMoves = allJointMoves;
	}

	public void update(PnMCTSNode node, MCTSJointMove jointMove, int[] goals) {

		if(node instanceof PnAMAFNode){

			Map<InternalPropnetMove, MoveStats> amafStats = ((PnAMAFNode)node).getAmafStats();

			this.allJointMoves.add(jointMove.getJointMove());

			MoveStats moveStats;

	        for(List<InternalPropnetMove> jM : this.allJointMoves){
	        	for(int i = 0; i<jM.size(); i++){
	        		moveStats = amafStats.get(jM.get(i));
	        		if(moveStats == null){
	        			moveStats = new MoveStats();
	        			amafStats.put(jM.get(i), moveStats);
	        		}

	        		moveStats.incrementVisits();
	        		moveStats.incrementScoreSum(goals[i]);
	        	}
	        }

		}else{
			throw new RuntimeException("GRAVEUpdate-update(): detected a node not implementing interface PnGRAVENode.");
		}
	}

}
