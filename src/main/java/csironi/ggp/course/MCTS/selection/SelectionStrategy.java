/**
 *
 */
package csironi.ggp.course.MCTS.selection;

import org.ggp.base.util.statemachine.Role;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public interface SelectionStrategy {

	public int[] select(MCTNode node, Role role);

}
