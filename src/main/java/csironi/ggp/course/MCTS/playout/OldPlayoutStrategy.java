/**
 *
 */
package csironi.ggp.course.MCTS.playout;

import java.util.List;

import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public interface OldPlayoutStrategy {

	/**
	 * This methods performs a play-out starting from the given node until it reaches a terminal
	 * state in the game.
	 *
	 * @param expandedNode the node just expanded from where to start the play-out.
	 * @return a tuple of scores. Each entry of the tuple corresponds to the goal value of a different
	 * player in the terminal state.
	 * @throws MoveDefinitionException
	 * @throws TransitionDefinitionException
	 * @throws GoalDefinitionException
	 * @throws StateMachineException
	 */
	public List<Integer> playout(MCTNode expandedNode) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, StateMachineException;

}
