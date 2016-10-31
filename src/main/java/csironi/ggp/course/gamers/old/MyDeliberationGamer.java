/**
 *
 */
package csironi.ggp.course.gamers.old;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

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

		StateMachine stateMachine = getStateMachine();
		ExplicitMachineState state = getCurrentState();
		ExplicitRole role = getRole();
		List<ExplicitMove> moves = stateMachine.getExplicitLegalMoves(state, role);

		ExplicitMove selection = bestmove(role, state);


		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

	/**
	 * @throws StateMachineException
	 *
	 *
	 */
	private ExplicitMove bestmove(ExplicitRole role, ExplicitMachineState state)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		StateMachine stateMachine = getStateMachine();
		List<ExplicitMove> moves = stateMachine.getExplicitLegalMoves(state, role);
		ExplicitMove selection = moves.get(0);
		int maxScore = 0;

		for (ExplicitMove move: moves){
			ArrayList<ExplicitMove> jointMoves = new ArrayList<ExplicitMove>();
			jointMoves.add(move);
			int currentScore = maxscore(role, stateMachine.getExplicitNextState(state, jointMoves));
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
	private int maxscore(ExplicitRole role, ExplicitMachineState state)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		StateMachine stateMachine = getStateMachine();

		if(stateMachine.isTerminal(state)){
			return stateMachine.getGoal(state, role);
		}

		List<ExplicitMove> moves = stateMachine.getExplicitLegalMoves(state, role);
		int maxScore = 0;

		for (ExplicitMove move: moves){
			ArrayList<ExplicitMove> jointMoves = new ArrayList<ExplicitMove>();
			jointMoves.add(move);
			int currentScore = maxscore(role, stateMachine.getExplicitNextState(state, jointMoves));
			if(currentScore > maxScore){
				maxScore = currentScore;
			}
		}

		return maxScore;
	}

}
