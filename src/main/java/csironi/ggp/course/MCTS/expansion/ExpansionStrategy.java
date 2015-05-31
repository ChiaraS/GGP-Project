/**
 *
 */
package csironi.ggp.course.MCTS.expansion;

import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public interface ExpansionStrategy {

	public MCTNode expand(MCTNode nodeToExpand) throws MoveDefinitionException, TransitionDefinitionException;

}
