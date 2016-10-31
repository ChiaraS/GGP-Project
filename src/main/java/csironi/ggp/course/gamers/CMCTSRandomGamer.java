/**
 *
 */
package csironi.ggp.course.gamers;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import csironi.ggp.course.MCTS.MCTSController;
import csironi.ggp.course.MCTS.expansion.OldRandomExpansion;
import csironi.ggp.course.MCTS.finalMoveChioce.OldMaxAvgScoreMoveChoice;
import csironi.ggp.course.MCTS.playout.OldRandomPlayout;
import csironi.ggp.course.MCTS.selection.OldRandomSelection;

/**
 * @author C.Sironi
 *
 */
public class CMCTSRandomGamer extends SampleGamer {

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public ExplicitMove stateMachineSelectMove(long timeout)
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
		List<ExplicitMove> moves = stateMachine.getExplicitLegalMoves(getCurrentState(), getRole());

		ExplicitMove selection = moves.get(0);
		// If there is more than one legal move available search the best one,
		// otherwise return the only one available.
		if(moves.size() != 1){

			//create only once!!!!
			MCTSController manager = new MCTSController(new OldRandomSelection(new OldRandomExpansion(), new OldRandomPlayout(stateMachine)), new OldMaxAvgScoreMoveChoice());

			ExplicitRole myRole = getRole();
			Map<ExplicitRole, Integer> roleIndexes = stateMachine.getRoleIndices();
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
