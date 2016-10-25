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
import org.ggp.base.util.statemachine.proverStructure.ProverMove;

import csironi.ggp.course.algorithms.MinMaxSequence;

/**
 * Minmax gamer realized for the GGP course.
 * This gamer in two-players games at each step searches the whole search space using the minmax algorithm and chooses
 * the legal action that has the highest minmax value.
 *
 * NOTE: if used to play single-player games this gamer will behave as a compulsive deliberation gamer and if used to play
 * 3+ -players games it will behave as a paranoid gamer.
 *
 * NOTE: this gamer doesn't manage time limits, thus if used for games with a big search space it might exceed time limits
 * when looking for the move to play and the game manager will assign it a random chosen action.
 *
 * @author C.Sironi
 *
 */
public class CMinmaxGamer extends SampleGamer {

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public ProverMove stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {
		// We get the current start time
		long start = System.currentTimeMillis();

		StateMachine stateMachine = getStateMachine();
		List<ProverMove> moves = stateMachine.getLegalMoves(getCurrentState(), getRole());

		ProverMove selection = moves.get(0);
		// If there is more than one legal move available search the best one,
		// otherwise return the only one available.
		if(moves.size() != 1){

			MinMaxSequence search = new MinMaxSequence(true, "C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\LOG\\MinmaxLog.txt", stateMachine);
			List<ProverMove> bestPathMoves = search.bestmove(getCurrentState(), getRole());
			selection = bestPathMoves.get(0);
		}

		// We get the end time
		// It is mandatory that stop<timeout
		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

}
