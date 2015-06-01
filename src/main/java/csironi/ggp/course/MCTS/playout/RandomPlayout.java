/**
 *
 */
package csironi.ggp.course.MCTS.playout;

import java.util.List;
import java.util.Random;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public class RandomPlayout implements PlayoutStrategy {

	StateMachine stateMachine;

	Random random;

	/**
	 *
	 */
	public RandomPlayout(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
		this.random = new Random();
	}

	/* (non-Javadoc)
	 * @see csironi.ggp.course.MCTS.playout.PlayoutStrategy#playout(csironi.ggp.course.MCTS.MCTNode)
	 */
	@Override
	public List<Integer> playout(MCTNode expandedNode) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {

		MachineState currentState = expandedNode.getState();

		if(!expandedNode.isMyTurn()){

			List<Move> jointMoves = expandedNode.getJointMoves();

			List<Role> roles = this.stateMachine.getRoles();

			// Randomly complete the list of joint moves
			for(int i=0; i < jointMoves.size(); i++){
				if(jointMoves.get(i) == null){
					Role moveRole = roles.get(i);
					List<Move> legalMoves = stateMachine.getLegalMoves(currentState, moveRole);
					Move randomMove = legalMoves.get(random.nextInt(legalMoves.size()));
					jointMoves.set(i, randomMove);
				}
			}

			currentState = stateMachine.getNextState(currentState, jointMoves);

		}

		return this.continuePlayout(currentState);

	}

	private List<Integer> continuePlayout(MachineState state) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{

		if(stateMachine.isTerminal(state)){
			return stateMachine.getGoals(state);
		}

		List<List<Move>> allJointMoves = stateMachine.getLegalJointMoves(state);

		List<Move> randomJointMoves = allJointMoves.get(this.random.nextInt(allJointMoves.size()));

		MachineState nextState = this.stateMachine.getNextState(state, randomJointMoves);

		return this.continuePlayout(nextState);

	}

}
