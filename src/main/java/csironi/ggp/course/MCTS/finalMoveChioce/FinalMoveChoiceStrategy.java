/**
 *
 */
package csironi.ggp.course.MCTS.finalMoveChioce;

import org.ggp.base.util.statemachine.Move;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public interface FinalMoveChoiceStrategy {

	public Move chooseFinalMove(MCTNode root);

}
