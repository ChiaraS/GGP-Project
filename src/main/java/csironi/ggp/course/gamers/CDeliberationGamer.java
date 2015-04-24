/**
 *
 */
package csironi.ggp.course.gamers;

import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.algorithms.MinMax;
import csironi.ggp.course.algorithms.SearchAlgorithm;

/**
 * Compulsive deliberation gamer realized for the GGP course.
 * This gamer at each step searches the whole search space and chooses the legal action that has the highest utility.
 *
 * NOTE: if used to play multi-player games this gamer will behave as a minmax gamer (2-players game)
 * or as a paranoid gamer (3+ -players game).
 *
 * @author C.Sironi
 *
 */
public class CDeliberationGamer extends SampleGamer {

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {

		// We get the current start time
		long start = System.currentTimeMillis();

		StateMachine stateMachine = getStateMachine();
		List<Move> moves = stateMachine.getLegalMoves(getCurrentState(), getRole());

		Move selection = moves.get(0);
		// If there is more than one legal move available search the best one,
		// otherwise return the only one available.
		if(moves.size() != 1){

			SearchAlgorithm search = new MinMax(true, "C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\LOG\\DeliberationLog.txt", stateMachine);
			List<Move> bestPathMoves = search.bestmove(getCurrentState(), getRole());
			selection = bestPathMoves.get(0);
		}

		// We get the end time
		// It is mandatory that stop<timeout
		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

}
