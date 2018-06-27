/**
 *
 */
package csironi.ggp.course.gamers;

import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

import csironi.ggp.course.MCTS.MCTSController;
import csironi.ggp.course.MCTS.expansion.OldRandomExpansion;
import csironi.ggp.course.MCTS.finalMoveChioce.OldMaxAvgScoreMoveChoice;
import csironi.ggp.course.MCTS.playout.OldRandomPlayout;
import csironi.ggp.course.MCTS.selection.OldUCTSelection;

/**
 * @author C.Sironi
 *
 */
public class PhilUCT extends SampleGamer {

	//Logger l = LogManager.getLogger();

	/**
	 *
	 */
	public PhilUCT() {
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public ExplicitMove stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		// Get the current start time
		long start = System.currentTimeMillis();

		GamerLogger.log("Stats", "Selecting move");
		GamerLogger.log("Stats", "Start time: " + start);

		long finishBy = timeout - 100;

		// Get state machine
		AbstractStateMachine stateMachine = getStateMachine();

		// Get all available moves
		List<Move> moves = stateMachine.getLegalMoves(getCurrentState(), getRole());

		Move selection = moves.get(0);
		// If there is more than one legal move available search the best one,
		// otherwise return the only one available.
		if(moves.size() != 1){

			MCTSController manager = new MCTSController(new OldUCTSelection(new OldRandomExpansion(), new OldRandomPlayout(stateMachine.getActualStateMachine()), 1.0/Math.sqrt(2)), new OldMaxAvgScoreMoveChoice());

			Role myRole = getRole();
			Map<Role, Integer> roleIndexes = stateMachine.getRoleIndices();
			int myRoleIndex = roleIndexes.get(myRole);

			selection = manager.selectBestMove(finishBy, stateMachine.getActualStateMachine(), myRoleIndex, stateMachine.convertToExplicitMachineState(getCurrentState()));
		}

		// We get the end time
		// It is mandatory that stop<timeout
		long stop = System.currentTimeMillis();

		GamerLogger.log("Stats", "Move selected: " + selection);
		GamerLogger.log("Stats", "End time: " + stop);

		notifyObservers(new GamerSelectedMoveEvent(stateMachine.convertToExplicitMoves(moves), stateMachine.convertToExplicitMove(selection), stop - start));
		return stateMachine.convertToExplicitMove(selection);
	}


}
