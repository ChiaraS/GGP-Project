/**
 *
 */
package csironi.ggp.course.MCTS.finalMoveChioce;

import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public interface OldFinalMoveChoiceStrategy {

	/**
	 * Given the root node of a Monte Carlo Tree after the search has been performed,
	 * this method chooses the move to be returned to the game manager.
	 *
	 * @param root the root node of the MCT.
	 * @return the move to be played for this turn by the player.
	 */
	public ExplicitMove chooseFinalMove(MCTNode root);

}
