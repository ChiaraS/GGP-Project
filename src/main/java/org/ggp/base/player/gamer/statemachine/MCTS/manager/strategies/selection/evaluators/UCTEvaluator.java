package org.ggp.base.player.gamer.statemachine.MCTS.manager.strategies.selection.evaluators;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.PnMCTSNode;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;

public class UCTEvaluator implements MoveEvaluator {

	private double c;

	public UCTEvaluator(double c) {
		this.c = c;
	}

	@Override
	public double computeMoveValue(PnMCTSNode theNode,
			InternalPropnetMove theMove, MoveStats theMoveStats) {

		double nodeVisits = theNode.getTotVisits();

		// NOTE: this should never happen, but if in the future some decay is applied to node statistics
		// this makes sure that if the node on which we are selecting has 0 visits the selection will
		// evaluate all moves equally (becoming a random selection).
		if(nodeVisits == 0){
			return Double.MAX_VALUE;
		}

		double moveVisits = theMoveStats.getVisits();
		double score = theMoveStats.getScoreSum();


		// NOTE: this should never happen if we use this class together with the InternalPropnetMCTSManager
		// because the selection phase in a node starts only after all moves have been expanded and visited
		// at least once. However a check is performed to keep the computation consistent even when a move
		// has never been visited (i.e. the "infinite" value (Double.MAX_VALUE) is returned).
		if(moveVisits == 0){
			return Double.MAX_VALUE;
		}

		double avgScore = (score / moveVisits) / 100.0;
		double exploration = this.c * (Math.sqrt(Math.log(nodeVisits)/moveVisits));
		return avgScore + exploration;
	}

	@Override
	public String getEvaluatorParameters() {
		return "C_CONSTANT = " + this.c;
	}

	@Override
	public String printEvaluator() {
		String params = this.getEvaluatorParameters();

		if(params != null){
			return "(EVALUATOR_TYPE = " + this.getClass().getSimpleName() + ", " + params + ")";
		}else{
			return "(EVALUATOR_TYPE = " + this.getClass().getSimpleName() + ")";
		}
	}

}
