/**
 *
 */
package csironi.ggp.course.MCTS.selection;

import java.util.ArrayList;
import java.util.Random;

import org.ggp.base.util.statemachine.Role;

import csironi.ggp.course.MCTS.MCTNode;
import csironi.ggp.course.MCTS.backpropagation.BackpropagationStrategy;
import csironi.ggp.course.MCTS.expansion.ExpansionStrategy;
import csironi.ggp.course.MCTS.playout.PlayoutStrategy;

/**
 * @author C.Sironi
 *
 */
public class RandomSelection implements SelectionStrategy {

	ExpansionStrategy expansionStrategy;
	PlayoutStrategy playoutStrategy;
	BackpropagationStrategy backpropagationStrategy;

	Random random;
	ArrayList<Role> roles;

	/**
	 *
	 */
	public RandomSelection(ExpansionStrategy expansionStrategy, PlayoutStrategy playoutStrategy) {
		this.expansionStrategy = expansionStrategy;
		this.playoutStrategy = playoutStrategy;
	}

	/* (non-Javadoc)
	 * @see csironi.ggp.course.MCTS.selection.SelectionStrategy#select(csironi.ggp.course.MCTS.MCTNode, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public int[] select(MCTNode node) {

		int[] goals;

		if(node.hasNoChildren()){
			goals = node.getTerminalGoals();
		}else{
			if(node.hasUnvisitedChildren()){
				MCTNode nodeAddedToTree = this.expansionStrategy.expand(node);
				goals = this.playoutStrategy.playout(nodeAddedToTree);
				this.backpropagationStrategy.update(nodeAddedToTree, goals);
			}else{
				MCTNode selectedChild = childVisit(this.random.nextInt(node.getVisitedChildrenNumber()));
				select(selectedChild);
			}
		}

		return goals;
	}

}
