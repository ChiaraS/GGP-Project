package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTActionsStatistics;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class DUCTSelection implements SelectionStrategy {

	private Random random;

	private double c;

	public DUCTSelection(Random random, double c) {
		this.random = random;
		this.c = c;
	}

	@Override
	public List<InternalPropnetMove> select(InternalPropnetDUCTMCTreeNode currentNode) {

		List<InternalPropnetMove> selectedJointMove = new ArrayList<InternalPropnetMove>();

		double maxDUCTvalue;
		double DUCTvalue;

		int nodeVisits = currentNode.getTotVisits();

		// For each role check the statistics and pick an action
		for(DUCTActionsStatistics stats : currentNode.getActionsStatistics()){

			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			maxDUCTvalue = -1;

			List<InternalPropnetMove> legalMoves = stats.getLegalMoves();
			int[] scores = stats.getScores();
			int[] visits = stats.getVisits();

			// For each legal action check the DUCTvalue
			for(int i = 0; i < legalMoves.size(); i++){
				// Compute the DUCT value
				DUCTvalue = this.computeDUCTvalue(scores[i], visits[i], nodeVisits);

				// If it's higher than the current maximum one, replace the max value and delete all best moves found so far
				if(DUCTvalue > maxDUCTvalue){
					maxDUCTvalue = DUCTvalue;
					selectedMovesIndices.clear();
					selectedMovesIndices.add(new Integer(i));
				}else if(DUCTvalue == maxDUCTvalue){
					selectedMovesIndices.add(new Integer(i));
				}
			}

			if(selectedMovesIndices.size() > 1){
				selectedJointMove.add(legalMoves.get(selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size()))));
			}else{
				selectedJointMove.add(legalMoves.get(selectedMovesIndices.get(0)));
			}
		}

		return selectedJointMove;
	}

	private double computeDUCTvalue(int score, int actionVisits, int nodeVisits){

		// NOTE: this should never happen if we use this class together with the InternalPropnetMCTSManager
		// because the selection phase in a node starts only after all actions have been expanded and visited
		// at least once. However a check is performed to keep the computation consistent even when an action
		// has never been visited (i.e. the "infinite" value (Double.MAX_VALUE) is returned).
		if(actionVisits == 0){
			return Double.MAX_VALUE;
		}

		double avgScore = (double) score / ((double)(actionVisits * 100));
		double exploration = this.c * (Math.sqrt(Math.log(nodeVisits)/(double)actionVisits));
		return  avgScore + exploration;

	}

}
