/**
 *
 */
package csironi.ggp.course.MCTS.expansion;

import java.util.Random;

import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public class RandomExpansion implements ExpansionStrategy {

	Random random;

	/**
	 *
	 */
	public RandomExpansion() {
		this.random = new Random();
	}

	/* (non-Javadoc)
	 * @see csironi.ggp.course.MCTS.expansion.ExpansionStrategy#expand(csironi.ggp.course.MCTS.MCTNode)
	 */
	@Override
	public MCTNode expand(MCTNode nodeToExpand) throws MoveDefinitionException, TransitionDefinitionException {

		return nodeToExpand.childFirstVisit(this.random.nextInt(nodeToExpand.getUnvisitedChildrenNumber()));

	}

}
