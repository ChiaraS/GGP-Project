package org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverMCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.AMAFDecoupled.ProverAMAFNode;
import org.ggp.base.util.statemachine.Move;

public class ProverGRAVEUpdate {

	private List<List<Move>> allJointMoves;

	public ProverGRAVEUpdate(List<List<Move>> allJointMoves) {
		this.allJointMoves = allJointMoves;
	}

	public void update(MCTSNode node, ProverMCTSJointMove jointMove, int[] goals) {

		if(node instanceof ProverAMAFNode){

			Map<Move, MoveStats> amafStats = ((ProverAMAFNode)node).getAmafStats();

			this.allJointMoves.add(jointMove.getJointMove());

			MoveStats moveStats;

	        for(List<Move> jM : this.allJointMoves){
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
