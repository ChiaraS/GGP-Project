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
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.MCTSController;
import csironi.ggp.course.MCTS.expansion.RandomExpansion;
import csironi.ggp.course.MCTS.finalMoveChioce.MaxAvgScoreMoveChoice;
import csironi.ggp.course.MCTS.playout.RandomPlayout;
import csironi.ggp.course.MCTS.selection.RandomSelection;

/**
 * @author C.Sironi
 *
 */
public class CMCTSRandomGamer extends SampleGamer {

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		// Get the current start time
		long start = System.currentTimeMillis();

		GamerLogger.log("Phil", "Selecting move");
		GamerLogger.log("Phil", "Start time: " + start);

		long finishBy = timeout - 100;

		// Get state machine
		StateMachine stateMachine = getStateMachine();

		// Get all available moves
		List<Move> moves = stateMachine.getLegalMoves(getCurrentState(), getRole());

		Move selection = moves.get(0);
		// If there is more than one legal move available search the best one,
		// otherwise return the only one available.
		if(moves.size() != 1){

			//create only once!!!!
			MCTSController manager = new MCTSController(new RandomSelection(new RandomExpansion(), new RandomPlayout(stateMachine)), new MaxAvgScoreMoveChoice());

			Role myRole = getRole();
			Map<Role, Integer> roleIndexes = stateMachine.getRoleIndices();
			int myRoleIndex = roleIndexes.get(myRole);

			selection = manager.selectBestMove(finishBy, stateMachine, myRoleIndex, getCurrentState());
		}

		// We get the end time
		// It is mandatory that stop<timeout
		long stop = System.currentTimeMillis();

		GamerLogger.log("Phil", "Move selected: " + selection);
		GamerLogger.log("Phil", "End time: " + stop);

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;

	}

	@Override
	public String getName() {
		return "Phil";
	}


	//!!!! ONLY ONE RANDOM!

}
