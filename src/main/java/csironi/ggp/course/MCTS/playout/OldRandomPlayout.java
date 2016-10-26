/**
 *
 */
package csironi.ggp.course.MCTS.playout;

import java.util.List;
import java.util.Random;

import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import csironi.ggp.course.MCTS.MCTNode;

/**
 * @author C.Sironi
 *
 */
public class OldRandomPlayout implements OldPlayoutStrategy {

	/**
	 * The state machine of the game that this strategy needs to get next states during play-out.
	 */
	StateMachine stateMachine;

	/**
	 * Random number generator used to implement the random choice.
	 */
	Random random;

	/**
	 * Constructor that initializes the state machine and the random number generator.
	 *
	 * @param stateMachine the state machine of the game that this strategy needs to get
	 * next states during play-out.
	 */
	public OldRandomPlayout(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
		this.random = new Random();
	}

	/* (non-Javadoc)
	 * @see csironi.ggp.course.MCTS.playout.PlayoutStrategy#playout(csironi.ggp.course.MCTS.MCTNode)
	 */
	@Override
	public List<Integer> playout(MCTNode expandedNode) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, StateMachineException {


		ExplicitMachineState currentState = expandedNode.getState();

		// For each new game state, my moves are always investigated first.
		// It might be that the play-out starts from a node where it is not my turn to play.
		// In this case the list with the joint moves will be partially filled (i.e. for some players
		// (mine included) the chosen move is already specified). This means I can choose randomly
		// only the action of players not yet investigated for this state.
		if(!expandedNode.isMyTurn()){

			// Joint moves list built so far.
			List<ExplicitMove> jointMoves = expandedNode.getJointMoves();

			// List of all roles (players) in the game.
			List<ExplicitRole> roles = this.stateMachine.getRoles();

			// Randomly complete the list of joint moves.
			for(int i=0; i < jointMoves.size(); i++){
				// If the move for a player has not been added yet in the list of random moves,
				// add a random one.
				if(jointMoves.get(i) == null){
					ExplicitRole moveRole = roles.get(i);
					List<ExplicitMove> legalMoves = stateMachine.getLegalMoves(currentState, moveRole);
					ExplicitMove randomMove = legalMoves.get(random.nextInt(legalMoves.size()));
					jointMoves.set(i, randomMove);
				}
			}

			// Go to the next state applying the joint move
			currentState = stateMachine.getNextState(currentState, jointMoves);

		}

		// Continue the play-out.
		return this.continuePlayout(currentState);

	}

	/**
	 * This method, given a game state, keeps getting a next state applying random joint moves
	 * until it gets to a terminal state.
	 *
	 * @param state the state from where to continue the play-out.
	 * @return a tuple of scores. Each entry of the tuple corresponds to the goal value of a different
	 * player in the terminal state.
	 * @throws GoalDefinitionException
	 * @throws MoveDefinitionException
	 * @throws TransitionDefinitionException
	 * @throws StateMachineException
	 */
	private List<Integer> continuePlayout(ExplicitMachineState state) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException, StateMachineException{

		if(stateMachine.isTerminal(state)){
			return stateMachine.getGoals(state);
		}

		// Get all possible combinations of moves for the players in this state.
		List<List<ExplicitMove>> allJointMoves = stateMachine.getLegalJointMoves(state);

		List<ExplicitMove> randomJointMoves = allJointMoves.get(this.random.nextInt(allJointMoves.size()));

		ExplicitMachineState nextState = this.stateMachine.getNextState(state, randomJointMoves);

		return this.continuePlayout(nextState);

	}

}
