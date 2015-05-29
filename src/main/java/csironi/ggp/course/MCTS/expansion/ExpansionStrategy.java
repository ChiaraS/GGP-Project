/**
 *
 */
package csironi.ggp.course.MCTS.expansion;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public interface ExpansionStrategy {

	public MCTNode expand(MCTNode nodeToExpand);

}
