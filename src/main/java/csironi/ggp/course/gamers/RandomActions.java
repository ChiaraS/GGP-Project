/**
 *
 */
package csironi.ggp.course.gamers;

import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

/**
 * Random gamer realized for test.
 * This gamer chooses at each step a random legal action.
 * This gamer checks the goals of a state during every step of the game.
 *
 * @author C.Sironi
 *
 */
public class RandomActions extends RandomTerminal {


	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public ExplicitMove stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		this.callGetGoals();

		long start = System.currentTimeMillis();

		List<ExplicitMove> moves = getStateMachine().convertToExplicitMoves(getStateMachine().getLegalMoves(getCurrentState(), getRole()));
		ExplicitMove selection = (moves.get(this.random.nextInt(moves.size())));

		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;

	}



}
