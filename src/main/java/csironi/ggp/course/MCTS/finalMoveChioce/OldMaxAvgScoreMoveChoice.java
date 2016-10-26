/**
 *
 */
package csironi.ggp.course.MCTS.finalMoveChioce;

import java.util.List;

import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public class OldMaxAvgScoreMoveChoice implements OldFinalMoveChoiceStrategy {

	/* (non-Javadoc)
	 * @see csironi.ggp.course.MCTS.finalMoveChioce.FinalMoveChoiceStrategy#chooseFinalMove(csironi.ggp.course.MCTS.MCTNode)
	 */
	@Override
	public ExplicitMove chooseFinalMove(MCTNode root) {

		List<MCTNode> visitedChildren = root.getVisitedChildren();
		int maxAvgScore = Integer.MIN_VALUE;
		ExplicitMove selection = null;

		// For all children of the root (i.e. for each move of my player)
		// check if it is the one with the highest average score.
		for(MCTNode node: visitedChildren){
			int avgScore = node.getScoreSum()/node.getVisits();
			if(avgScore >= maxAvgScore){
				maxAvgScore = avgScore;
				selection = node.getMoveFromParent();
			}
		}

		return selection;
	}

}
