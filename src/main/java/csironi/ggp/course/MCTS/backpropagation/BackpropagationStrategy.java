/**
 *
 */
package csironi.ggp.course.MCTS.backpropagation;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public interface BackpropagationStrategy {

	public void backpropagate(MCTNode expandedNode, int[] rewards);

}
