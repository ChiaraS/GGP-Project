/**
 *
 */
package csironi.ggp.course.gamers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.MCTNode;
import csironi.ggp.course.MCTS.expansion.RandomExpansion;
import csironi.ggp.course.MCTS.selection.RandomSelection;
import csironi.ggp.course.MCTS.selection.SelectionStrategy;

/**
 * @author user
 *
 */
public class CMCTSRandomGamer extends SampleGamer {

	/**
	 *
	 */
	public CMCTSRandomGamer() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {

		// Get the current start time
		long start = System.currentTimeMillis();

		// Get state machine
		StateMachine stateMachine = getStateMachine();

		// Get all available moves
		List<Move> moves = stateMachine.getLegalMoves(getCurrentState(), getRole());

		Move selection = moves.get(0);
		// If there is more than one legal move available search the best one,
		// otherwise return the only one available.
		if(moves.size() != 1){

			Role myRole = getRole();
			Map<Role, Integer> roleIndexes = stateMachine.getRoleIndices();
			int myRoleIndex = roleIndexes.get(myRole);

			SelectionStrategy selectionStrategy = new RandomSelection(new RandomExpansion(), new RandomPlayout());

			List<Role> roles = stateMachine.getRoles();
			List<Move> jointMoves = new ArrayList<Move>();
			for(int i = 0; i < roles.size(); i++){
				jointMoves.add(i, null);
			}

			MCTNode root = new MCTNode(stateMachine, stateMachine.getInitialState(), myRoleIndex, myRoleIndex, null, jointMoves);
			root.initializeChildren();
			selectionStrategy.select(root);
		}

		// We get the end time
		// It is mandatory that stop<timeout
		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;

	}

	@Override
	public String getName() {
		return "Phil";
	}

}
