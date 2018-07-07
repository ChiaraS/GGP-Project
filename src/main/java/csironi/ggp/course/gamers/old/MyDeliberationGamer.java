/**
 *
 */
package csironi.ggp.course.gamers.old;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

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
	public ExplicitMove stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		long start = System.currentTimeMillis();

		AbstractStateMachine stateMachine = getStateMachine();
		MachineState state = getCurrentState();
		Role role = getRole();
		List<Move> moves = stateMachine.getLegalMoves(state, role);

		Move selection = bestmove(role, state);


		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(stateMachine.convertToExplicitMoves(moves), stateMachine.convertToExplicitMove(selection), stop - start));
		return stateMachine.convertToExplicitMove(selection);
	}

	/**
	 * @throws StateMachineException
	 *
	 *
	 */
	private Move bestmove(Role role, MachineState state)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		AbstractStateMachine stateMachine = getStateMachine();
		List<Move> moves = stateMachine.getLegalMoves(state, role);
		Move selection = moves.get(0);
		double maxScore = 0;

		for (Move move: moves){
			ArrayList<Move> jointMoves = new ArrayList<Move>();
			jointMoves.add(move);
			double currentScore = maxscore(role, stateMachine.getNextState(state, jointMoves));
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
	 * @throws StateMachineException
	 *
	 *
	 */
	private double maxscore(Role role, MachineState state)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		AbstractStateMachine stateMachine = getStateMachine();

		if(stateMachine.isTerminal(state)){
			return stateMachine.getSafeGoalsAvgForAllRoles(state)[stateMachine.getRoleIndices().get(role)];
		}

		List<Move> moves = stateMachine.getLegalMoves(state, role);
		double maxScore = 0;

		for (Move move: moves){
			ArrayList<Move> jointMoves = new ArrayList<Move>();
			jointMoves.add(move);
			double currentScore = maxscore(role, stateMachine.getNextState(state, jointMoves));
			if(currentScore > maxScore){
				maxScore = currentScore;
			}
		}

		return maxScore;
	}

}
