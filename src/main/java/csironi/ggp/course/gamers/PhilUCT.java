/**
 *
 */
package csironi.ggp.course.gamers;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.MCTSManager;
import csironi.ggp.course.MCTS.expansion.RandomExpansion;
import csironi.ggp.course.MCTS.finalMoveChioce.MaxAvgScoreMoveChoice;
import csironi.ggp.course.MCTS.playout.RandomPlayout;
import csironi.ggp.course.MCTS.selection.UCTSelection;

/**
 * @author C.Sironi
 *
 */
public class PhilUCT extends SampleGamer {


	/**
	 *
	 */
	public PhilUCT() {
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

		GamerLogger.log("Stats", "Selecting move");
		GamerLogger.log("Stats", "Start time: " + start);

		long finishBy = timeout - 100;

		// Get state machine
		StateMachine stateMachine = getStateMachine();

		// Get all available moves
		List<Move> moves = stateMachine.getLegalMoves(getCurrentState(), getRole());

		Move selection = moves.get(0);
		// If there is more than one legal move available search the best one,
		// otherwise return the only one available.
		if(moves.size() != 1){

			MCTSManager manager = new MCTSManager(new UCTSelection(new RandomExpansion(), new RandomPlayout(stateMachine), 1.0/Math.sqrt(2)), new MaxAvgScoreMoveChoice());

			Role myRole = getRole();
			Map<Role, Integer> roleIndexes = stateMachine.getRoleIndices();
			int myRoleIndex = roleIndexes.get(myRole);

			selection = manager.selectBestMove(finishBy, stateMachine, myRoleIndex, getCurrentState());
		}

		// We get the end time
		// It is mandatory that stop<timeout
		long stop = System.currentTimeMillis();

		GamerLogger.log("Stats", "Move selected: " + selection);
		GamerLogger.log("Stats", "End time: " + stop);

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}


}
