/**
 *
 */
package csironi.ggp.course.gamers;

import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

import csironi.ggp.course.algorithms.MinMax;
import csironi.ggp.course.evalfunctions.EvalProximity;

/**
 * @author C.Sironi
 *
 */
public class CProximityGamer extends SampleGamer {

	/**
	 *
	 */
	public CProximityGamer() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public ExplicitMove stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		// We get the current start time
		long start = System.currentTimeMillis();

		long finishBy = timeout - 1000;

		StateMachine stateMachine = getStateMachine();
		List<ExplicitMove> moves = stateMachine.getLegalMoves(getCurrentState(), getRole());

		ExplicitMove selection = moves.get(0);
		// If there is more than one legal move available search the best one,
		// otherwise return the only one available.
		if(moves.size() != 1){

			MinMax search = new MinMax(true, "C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\LOG\\ProximityLog.txt", stateMachine);
			selection = search.bestmove(finishBy, getCurrentState(), getRole(), true, 0, 100, 12, false, false, new EvalProximity(stateMachine));
		}

		// We get the end time
		// It is mandatory that stop<timeout
		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

}
