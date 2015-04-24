/**
 *
 */
package csironi.ggp.course.gamers.old;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * Implementation of a Compulsive Deliberation player for the GGP course.
 *
 * NOTE: this player only works on single-player games! Do not use it for multi-player games!
 *
 * @author C.Sironi
 *
 */
public class MyDeliberationGamer extends SampleGamer {

	/**
	 *
	 */
	public MyDeliberationGamer() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {

		long start = System.currentTimeMillis();

		StateMachine stateMachine = getStateMachine();
		MachineState state = getCurrentState();
		Role role = getRole();
		List<Move> moves = stateMachine.getLegalMoves(state, role);

		Move selection = bestmove(role, state);


		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

	/**
	 *
	 *
	 */
	private Move bestmove(Role role, MachineState state)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException{

		StateMachine stateMachine = getStateMachine();
		List<Move> moves = stateMachine.getLegalMoves(state, role);
		Move selection = moves.get(0);
		int maxScore = 0;

		for (Move move: moves){
			ArrayList<Move> jointMoves = new ArrayList<Move>();
			jointMoves.add(move);
			int currentScore = maxscore(role, stateMachine.getNextState(state, jointMoves));
			if(currentScore == 100){
				return move;
			}
			if(currentScore > maxScore){
				maxScore = currentScore;
				selection = move;
			}
		}

		return selection;

	}

	/**
	 *
	 *
	 */
	private int maxscore(Role role, MachineState state)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException{

		StateMachine stateMachine = getStateMachine();

		if(stateMachine.isTerminal(state)){
			return stateMachine.getGoal(state, role);
		}

		List<Move> moves = stateMachine.getLegalMoves(state, role);
		int maxScore = 0;

		for (Move move: moves){
			ArrayList<Move> jointMoves = new ArrayList<Move>();
			jointMoves.add(move);
			int currentScore = maxscore(role, stateMachine.getNextState(state, jointMoves));
			if(currentScore > maxScore){
				maxScore = currentScore;
			}
		}

		return maxScore;
	}

}
