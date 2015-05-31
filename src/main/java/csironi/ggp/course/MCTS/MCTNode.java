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
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * @author C.Sironi
 *
 */
public class MCTNode {

	private StateMachine stateMachine;

	private MachineState state;

	private int playingRoleIndex;

	private int myRoleIndex;

	// Score
	private int scoreSum;

	private int visits;

	private Move moveFromParent;

	private List<Move> jointMoves;

	// Children
	private ArrayList<MCTNode> visitedChildren;

	private ArrayList<MCTNode> unvisitedChildren;


	/**
	 *
	 */
	public MCTNode(StateMachine stateMachine, MachineState state, int playingRoleIndex, int myRoleIndex, Move moveFromParent, List<Move> jointMoves) {

		this.stateMachine = stateMachine;
		this.state = state;
		this.playingRoleIndex = playingRoleIndex;
		this.scoreSum = 0;
		this.visits = 0;
		this.moveFromParent = moveFromParent;
		this.jointMoves = jointMoves;

	}

	public void initializeChildren() throws MoveDefinitionException, TransitionDefinitionException{

		this.visitedChildren = new ArrayList<MCTNode>();
		this.unvisitedChildren = new ArrayList<MCTNode>();

		List<Role> roles = this.stateMachine.getRoles();
		Role playingRole = roles.get(this.playingRoleIndex);
		List<Move> moves = this.stateMachine.getLegalMoves(state, playingRole);

		int nextPlayingRoleIndex = (this.playingRoleIndex+1)%roles.size();

		for(Move move: moves){
			List<Move> childJointMoves = new ArrayList<Move>(this.jointMoves);
			childJointMoves.set(this.playingRoleIndex, move);

			MachineState childState = this.state;

			if(nextPlayingRoleIndex == this.myRoleIndex){
				childState = this.stateMachine.getNextState(this.state, childJointMoves);

				for(int i = 0; i < childJointMoves.size(); i++){
					childJointMoves.set(i, null);
				}
			}

			MCTNode child = new MCTNode(this.stateMachine, childState, nextPlayingRoleIndex, myRoleIndex, move, childJointMoves);
			this.unvisitedChildren.add(child);
		}

	}

	public boolean hasNoChildren(){
		return (this.visitedChildren.size() == 0 && this.unvisitedChildren.size() == 0);
	}

	public boolean hasUnvisitedChildren(){
		return !(this.unvisitedChildren.size() == 0);
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

	public MCTNode childFirstVisit(int index) throws MoveDefinitionException, TransitionDefinitionException{
		MCTNode visitedChild = this.unvisitedChildren.remove(index);
		visitedChild.initializeChildren();
		visitedChild.incrementVisits();
		this.visitedChildren.add(visitedChild);
		return visitedChild;
	}

	public MCTNode childVisit(int index){
		MCTNode visitedChild = this.visitedChildren.get(index);
		visitedChild.incrementVisits();
		return visitedChild;
	}



}
