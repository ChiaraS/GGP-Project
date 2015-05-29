/**
 *
 */
package csironi.ggp.course.MCTS;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;

/**
 * @author C.Sironi
 *
 */
public class MCTNode {

	private StateMachine stateMachine;

	private List<Role> roles;

	private MachineState state;

	private int scoreSum;

	private int visits;

	private MCTNode parent;

	private Move moveFromParent;

	private ArrayList<MCTNode> visitedChildren;

	private ArrayList<MCTNode> unvisitedChildren;

	private int playingRoleIndex;

	/**
	 *
	 */
	public MCTNode() {

	}

	public boolean hasNoChildren(){
		return (this.visitedChildren.size() == 0 && this.unvisitedChildren.size() == 0);
	}

	public int getVisitedChildrenNumber(){
		return this.visitedChildren.size();
	}

	public int getUnvisitedChildrenNumber(){
		return this.unvisitedChildren.size();
	}

	public void incrementVisits(){
		this.visits++;
	}

	public MCTNode childFirstVisit(int index) throws MoveDefinitionException{
		MCTNode visitedChild = this.unvisitedChildren.remove(index);
		visitedChild.initializeChildren();
		this.visitedChildren.add(visitedChild);
		return visitedChild;
	}

	public MCTNode childVisit(int index){
		MCTNode visitedChild = this.visitedChildren.get(index);
		visitedChild.incrementVisits();
		return visitedChild;
	}

	public void initializeChildren() throws MoveDefinitionException{

		List<Move> moves = this.stateMachine.getLegalMoves(state, roles.get(this.playingRoleIndex));

		//TODO INITIALIZE CHILDREN!!!
	}

}
