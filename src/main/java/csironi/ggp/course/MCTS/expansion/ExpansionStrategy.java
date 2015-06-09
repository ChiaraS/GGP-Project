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

	/**
	 * This method expands the given node, i.e. it chooses which child node(s) of the node to add to the
	 * Monte Carlo Tree, and returns the one from where to start the play-out.
	 *
	 * @param nodeToExpand node to be expanded.
	 * @return the child node of the nodeToExpand from where to start the play-out.
	 * @throws MoveDefinitionException
	 * @throws TransitionDefinitionException
	 */
	public MCTNode expand(MCTNode nodeToExpand) throws MoveDefinitionException, TransitionDefinitionException;

}
