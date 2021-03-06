/**
 *
 */
package csironi.ggp.course.MCTS.selection;

import java.util.List;
import java.util.Random;

import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.MCTNode;
import csironi.ggp.course.MCTS.expansion.OldExpansionStrategy;
import csironi.ggp.course.MCTS.playout.OldPlayoutStrategy;

/**
 * @author C.Sironi
 *
 */
public class OldRandomSelection implements OldSelectionStrategy {

	/**
	 * Strategy that the player uses to expand a node.
	 */
	OldExpansionStrategy expansionStrategy;

	/**
	 * Strategy that the player uses to perform play-out from a node.
	 */
	OldPlayoutStrategy playoutStrategy;

	Random random;

	/**
	 *
	 */
	public OldRandomSelection(OldExpansionStrategy expansionStrategy, OldPlayoutStrategy playoutStrategy) {
		this.expansionStrategy = expansionStrategy;
		this.playoutStrategy = playoutStrategy;
		this.random = new Random();
	}

	/* (non-Javadoc)
	 * @see csironi.ggp.course.MCTS.selection.SelectionStrategy#select(csironi.ggp.course.MCTS.MCTNode, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public List<Double> select(MCTNode node) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException, StateMachineException {

		List<Double> goals;

		// If the node has no children it means it is a leaf node of the MCTS tree
		if(node.hasNoChildren()){
			return node.getTerminalGoals();
		}

		MCTNode selectedChild;

		// This strategy expands the MCT and starts play-out whenever the current node in the MCT
		// has at least one unvisited child.
		if(node.hasUnvisitedChildren()){
			// Add to the tree one of the unvisited children of the current node
			selectedChild = this.expansionStrategy.expand(node);
			// Perform play-out starting from the added node.
			goals = this.playoutStrategy.playout(selectedChild);
		}else{
			// If all children of the node have been added to the tree, select randomly the next
			// one to investigate.
			selectedChild = node.getVisitedChild(this.random.nextInt(node.getVisitedChildrenNumber()));
			goals = select(selectedChild);
		}

		// Update the statistics of this node with the values backpropagated from the play-out.
		selectedChild.update(goals);

		return goals;
	}

}
