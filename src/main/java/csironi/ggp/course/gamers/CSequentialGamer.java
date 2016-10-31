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

import csironi.ggp.course.algorithms.MinMaxSequence;

/**
 * Sequential planning gamer realized for the GGP course.
 * This gamer during the start clock searches the whole search space and builds a plan with all the legal actions
 * that lead to the best treminal state (i.e. the one with highest utility). Then for each game step returns the
 * corresponding action in the computed plan.
 *
 * NOTE: this player works for single-player games. If used to play multi-player games the uncertainty about other
 * players' actions might cause the plan to be inconsistent after the first step. This is because the plan is based on
 * certain opponents' actions that might be different from what the opponents actually chose to do.
 *
 * NOTE: this gamer doesn't manage time limits, thus if used for games with a big search space it might exceed time limits
 * when computing the sequence of actions and the game manager will assign it a random chosen action for every move not
 * returned in time.
 *
 * @author C.Sironi
 *
 */
public class CSequentialGamer extends SampleGamer {

	/**
	 * Sequence of the best moves to play for each step of the game.
	 */
	private List<ExplicitMove> bestPlan;

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.sample.SampleGamer#stateMachineMetaGame(long)
	 */
	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException, StateMachineException
	{
		// Get the state machine
		StateMachine stateMachine = getStateMachine();

		// Search the bast sequence of action to play during the whole game
		MinMaxSequence search = new MinMaxSequence(true, "C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\LOG\\SequentialLog.txt", stateMachine);
		this.bestPlan = search.bestmove(getCurrentState(), getRole());

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

		// Get state machine and list of available legal moves for the player
		StateMachine stateMachine = getStateMachine();
		List<ExplicitMove> moves = stateMachine.getExplicitLegalMoves(getCurrentState(), getRole());

		// Return and remove the best move for the current step from the sequence of best moves
		ExplicitMove selection = bestPlan.remove(0);

		// We get the end time
		// It is mandatory that stop<timeout
		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));

		return selection;
	}

}
