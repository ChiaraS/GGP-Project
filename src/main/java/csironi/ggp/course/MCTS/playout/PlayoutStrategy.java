/**
 *
 */
package csironi.ggp.course.MCTS.playout;

import java.util.List;

import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public interface PlayoutStrategy {

	public List<Integer> playout(MCTNode expandedNode) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException;

}
