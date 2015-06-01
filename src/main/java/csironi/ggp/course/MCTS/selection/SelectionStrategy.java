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

	public List<Integer> select(MCTNode node) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException;

}
