/**
 *
 */
package csironi.ggp.course.MCTS.playout;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public interface PlayoutStrategy {

	public int[] playout(MCTNode expandedNode);

}
