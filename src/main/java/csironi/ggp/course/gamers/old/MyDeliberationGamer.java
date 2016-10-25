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
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;

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
	public ProverMove stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		long start = System.currentTimeMillis();

		StateMachine stateMachine = getStateMachine();
		ProverMachineState state = getCurrentState();
		ProverRole role = getRole();
		List<ProverMove> moves = stateMachine.getLegalMoves(state, role);

		ProverMove selection = bestmove(role, state);


		long stop = System.currentTimeMillis();

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

	/**
	 * @throws StateMachineException
	 *
	 *
	 */
	private ProverMove bestmove(ProverRole role, ProverMachineState state)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		StateMachine stateMachine = getStateMachine();
		List<ProverMove> moves = stateMachine.getLegalMoves(state, role);
		ProverMove selection = moves.get(0);
		int maxScore = 0;

		for (ProverMove move: moves){
			ArrayList<ProverMove> jointMoves = new ArrayList<ProverMove>();
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
	 * @throws StateMachineException
	 *
	 *
	 */
	private int maxscore(ProverRole role, ProverMachineState state)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		StateMachine stateMachine = getStateMachine();

		if(stateMachine.isTerminal(state)){
			return stateMachine.getGoal(state, role);
		}

		List<ProverMove> moves = stateMachine.getLegalMoves(state, role);
		int maxScore = 0;

		for (ProverMove move: moves){
			ArrayList<ProverMove> jointMoves = new ArrayList<ProverMove>();
			jointMoves.add(move);
			int currentScore = maxscore(role, stateMachine.getNextState(state, jointMoves));
			if(currentScore > maxScore){
				maxScore = currentScore;
			}
		}

		return maxScore;
	}

}
