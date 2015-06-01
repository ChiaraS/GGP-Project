/**
 *
 */
package csironi.ggp.course.MCTS.selection;

import java.util.List;
import java.util.Random;

import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.MCTNode;
import csironi.ggp.course.MCTS.expansion.ExpansionStrategy;
import csironi.ggp.course.MCTS.playout.PlayoutStrategy;

/**
 * @author C.Sironi
 *
 */
public class RandomSelection implements SelectionStrategy {

	ExpansionStrategy expansionStrategy;
	PlayoutStrategy playoutStrategy;

	Random random;

	/**
	 *
	 */
	public RandomSelection(ExpansionStrategy expansionStrategy, PlayoutStrategy playoutStrategy) {
		this.expansionStrategy = expansionStrategy;
		this.playoutStrategy = playoutStrategy;
		this.random = new Random();
	}

	/* (non-Javadoc)
	 * @see csironi.ggp.course.MCTS.selection.SelectionStrategy#select(csironi.ggp.course.MCTS.MCTNode, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public List<Integer> select(MCTNode node) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {

		List<Integer> goals;

		// If the node has no children it means it is a leaf node of the MCTS tree
		if(node.hasNoChildren()){
			return node.getTerminalGoals();
		}

		MCTNode selectedChild;

		if(node.hasUnvisitedChildren()){
			selectedChild = this.expansionStrategy.expand(node);
			goals = this.playoutStrategy.playout(selectedChild);
		}else{
			selectedChild = node.getVisitedChild(this.random.nextInt(node.getVisitedChildrenNumber()));
			goals = select(selectedChild);
		}

		selectedChild.update(goals);

		return goals;
	}

}
