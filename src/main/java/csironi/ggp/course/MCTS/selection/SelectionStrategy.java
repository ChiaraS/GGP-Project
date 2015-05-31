/**
 *
 */
package csironi.ggp.course.MCTS.selection;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public interface SelectionStrategy {

	public int[] select(MCTNode node);

}
