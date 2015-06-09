/**
 *
 */
package csironi.ggp.course.MCTS.selection;

import java.util.List;

import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public interface SelectionStrategy {

	/**
	 * This method performs the selection of a child of the given node and decides when
	 * to perform expansion and start the play-out.
	 *
	 * @param node the node for which to select the child to investigate next.
	 * @return the tuple of scores to be backpropagated in the Monte Carlo Tree.
	 * @throws GoalDefinitionException
	 * @throws MoveDefinitionException
	 * @throws TransitionDefinitionException
	 */
	public List<Integer> select(MCTNode node) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException;

}
