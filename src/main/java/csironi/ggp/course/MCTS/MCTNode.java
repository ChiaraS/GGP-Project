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
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.backpropagation.BackpropagationStrategy;

/**
 * @author C.Sironi
 *
 */
public class MCTNode implements BackpropagationStrategy {

	/**
	 * The state machine that answers queries about the states of the game.
	 */
	private StateMachine stateMachine;

	/**
	 * The state being analyzed in this MCT node.
	 */
	private MachineState state;

	/**
	 * The index in the list of roles of the player whose moves are being analyzed in this MCT node.
	 * NOTE: the order of the roles in the list of roles is always the same. Also the state machine,
	 * when queried for the list of roles returns them always in the same order.
	 */
	private int playingRoleIndex;

	/**
	 * The index in the list of roles of my player.
	 */
	private int myRoleIndex;

	/**
	 * The sum of all scores seen so far investigating paths starting from this node.
	 */
	private int scoreSum;

	/**
	 * Number of times this node has been visited.
	 */
	private int visits;

	/**
	 * The move that from the parent allowed to reach this tree node.
	 */
	private Move moveFromParent;

	/**
	 * List containing a move for each player. The entry for a player contains the move that
	 * the player has chosen from the current state if this player has already been analyzed,
	 * null otherwise.
	 */
	private List<Move> jointMoves;

	/**
	 * List of all the children that have been visited at least once (and thus added to the MCT).
	 */
	private ArrayList<MCTNode> visitedChildren;

	/**
	 * List of children of the node not visited yet.
	 */
	private ArrayList<MCTNode> unvisitedChildren;


	/**
	 *
	 */
	public MCTNode(StateMachine stateMachine, MachineState state, int playingRoleIndex, int myRoleIndex, Move moveFromParent, List<Move> jointMoves) {

		this.stateMachine = stateMachine;
		this.state = state;
		this.playingRoleIndex = playingRoleIndex;
		this.myRoleIndex = myRoleIndex;
		this.scoreSum = 0;
		this.visits = 0;
		this.moveFromParent = moveFromParent;
		this.jointMoves = jointMoves;

	}


	/**
	 * This method initializes all the possible children that this node can have.
	 * There will be one child for each possible action that is legal for the player of this node
	 * in the state of this turn. The children will be added to the parent's unvisited children
	 * list but they will not be considered as added to the Monte Carlo Tree yet.
	 *
	 * @throws MoveDefinitionException
	 * @throws TransitionDefinitionException
	 * @throws StateMachineException
	 */
	public void initializeChildren() throws MoveDefinitionException, TransitionDefinitionException, StateMachineException{

		// INITIALIZE ONLY SCORE AND VISITS! DON'T SIMULATE EACH MOVE

		this.visitedChildren = new ArrayList<MCTNode>();
		this.unvisitedChildren = new ArrayList<MCTNode>();

		// If state is terminal there are no children to add because there are no legal moves
		// If it is not terminal, add all children
		if(!this.stateMachine.isTerminal(this.state)){
			List<Role> roles = this.stateMachine.getRoles();
			Role playingRole = roles.get(this.playingRoleIndex);
			// Get all legal moves for this player in this game state
			List<Move> moves = this.stateMachine.getLegalMoves(state, playingRole);

			int nextPlayingRoleIndex = (this.playingRoleIndex+1)%roles.size();

			// Add a child for each move
			for(Move move: moves){
				// In the list of joint moves for the child, memorize which action this player took to get to the child.
				List<Move> childJointMoves = new ArrayList<Move>(this.jointMoves);
				childJointMoves.set(this.playingRoleIndex, move);

				MachineState childState = this.state;

				// If for this turn each player has already selected a move, advance to the next state
				// using the list of joint moves that now is complete. Then reset to null values all
				// the moves in the list of joint moves for the children.

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

	public MachineState getState(){
		return this.state;
	}

	public List<Move> getJointMoves(){
		return this.jointMoves;
	}

	public boolean isMyTurn(){
		return this.myRoleIndex == this.playingRoleIndex;
	}

	/**
	 * This method officially adds a child of this node to the MCT. Moreover, it initializes its unvisited children.
	 *
	 * @param index the index of the child to be visited
	 * @return the node added to the MCT
	 * @throws MoveDefinitionException
	 * @throws TransitionDefinitionException
	 * @throws StateMachineException
	 */
	public MCTNode childFirstVisit(int index) throws MoveDefinitionException, TransitionDefinitionException, StateMachineException{
		MCTNode visitedChild = this.unvisitedChildren.remove(index);
		visitedChild.initializeChildren();
		this.visitedChildren.add(visitedChild);
		return visitedChild;
	}

	public MCTNode getVisitedChild(int index){
		return this.visitedChildren.get(index);
	}

	public List<Integer> getTerminalGoals() throws GoalDefinitionException, StateMachineException{
		return this.stateMachine.getGoals(this.state);
	}

	public List<MCTNode> getVisitedChildren(){
		return this.visitedChildren;
	}

	@Override
	public void update(List<Integer> goals){

		int numberOfRoles = this.jointMoves.size();

		int updateIndex = (this.playingRoleIndex-1+numberOfRoles)%numberOfRoles;
		this.scoreSum += goals.get(updateIndex);
		this.visits = this.visits+1;

	}

	public int getVisits() {
		return this.visits;
	}

	public int getScoreSum() {
		return this.scoreSum;
	}

	public Move getMoveFromParent(){
		return this.moveFromParent;
	}



}
