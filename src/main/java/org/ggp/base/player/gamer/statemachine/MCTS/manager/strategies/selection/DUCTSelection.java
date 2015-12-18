package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.DUCTJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.InternalPropnetDUCTMCTreeNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class DUCTSelection implements SelectionStrategy {

	private Random random;

	private double uctOffset;

	private double c;

	public DUCTSelection(Random random, double uctOffset, double c) {
		this.random = random;
		this.uctOffset = uctOffset;
		this.c = c;
	}

	@Override
	public DUCTJointMove select(InternalPropnetDUCTMCTreeNode currentNode) {

		DUCTMove[][] actionsStats = currentNode.getActions();

		List<InternalPropnetMove> selectedJointMove = new ArrayList<InternalPropnetMove>();
		int[] movesIndices = new int[actionsStats.length];

		double maxDUCTvalue;
		double DUCTvalue;

		int nodeVisits = currentNode.getTotVisits();

		// For each role check the statistics and pick an action
		for(int i = 0; i < actionsStats.length; i++){

			// Compute UCT value for all actions
			maxDUCTvalue = -1;

			// For each legal action check the DUCTvalue
			for(int j = 0; j < actionsStats[i].length; j++){

				// Compute the DUCT value
				DUCTvalue = this.computeDUCTvalue(actionsStats[i][j].getScoreSum(), actionsStats[i][j].getVisits(), nodeVisits);

				actionsStats[i][j].setUct(DUCTvalue);

				// If it's higher than the current maximum one, replace the max value
				if(DUCTvalue > maxDUCTvalue){
					maxDUCTvalue = DUCTvalue;
				}
			}

			// Now that we have the maximum UCT value we can look for all actions that have their UCT value
			// in the interval [maxDUCTvalue-offset, maxDUCTvalue]
			List<Integer> selectedMovesIndices = new ArrayList<Integer>();

			for(int j = 0; j < actionsStats[i].length; j++){
				if(actionsStats[i][j].getUct() >= (maxDUCTvalue-this.uctOffset)){
					selectedMovesIndices.add(new Integer(j));
				}
			}

			movesIndices[i] = selectedMovesIndices.get(this.random.nextInt(selectedMovesIndices.size())).intValue();
			selectedJointMove.add(actionsStats[i][movesIndices[i]].getTheMove());
		}

		return new DUCTJointMove(selectedJointMove, movesIndices);
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
