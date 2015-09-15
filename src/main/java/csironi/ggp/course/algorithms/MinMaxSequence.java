/**
 *
 */
package csironi.ggp.course.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.utils.Pair;

/**
 * Implementation of minmax search algorithm that works for both single- and multi-player games.
 *
 * For single-player games it always computes the score of each state maximizing the scores of
 * its children, thus behaving like a compulsive deliberation agent. For two-player games it
 * behaves like the classical minmax algorithm, while for multiplayer games it behaves like the
 * paranoid search algorithm. Moreover, while maximizing the score for each state, this algorithm
 * keeps track of the sequence of moves that led to this maximum score. This implies that, if run
 * only during the startclock time, this algorithm can be used to implement a sequential planning
 * gamer.
 *
 * @author C.Sironi
 *
 */
public class MinMaxSequence extends SearchAlgorithm {

	/**
	 * Constructor that calls the constructor of the parent.
	 */
	public MinMaxSequence(boolean log, String logFileName, StateMachine stateMachine) {
		super(log, logFileName, stateMachine);
	}


	public List<Move> bestmove(MachineState state, Role role)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		log("Starting bestscore");


		/*Only for log*/
		List<Role> roles = stateMachine.getRoles();
		String toLog = "Roles: [ ";
		for(Role r: roles){
			toLog += r + " ";
		}
		toLog += "]";
		log(toLog);

		Pair<List<Move>, Integer> result = maxscore(state, role);

		toLog = "Reversed actions sequence: [ ";
		for(Move m: result.getFirst()){
			toLog += m + " ";
		}
		toLog += "]";
		log(toLog);

		// Revert the order of the actions since they are listed from last to first wrt to the root.
		List<Move> bestPathMoves = new ArrayList<Move>();
		for(Move move: result.getFirst()){
			bestPathMoves.add(0, move);
		}

		toLog = "Actions sequence: [ ";
		for(Move m: bestPathMoves){
			toLog += m + " ";
		}
		toLog += "]";
		log(toLog);


		return bestPathMoves;
	}


	private Pair<List<Move>, Integer> maxscore(MachineState state, Role role)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		log("Performing maxscore");

		// Check if the state is terminal
		if(stateMachine.isTerminal(state)){
			int goal = stateMachine.getGoal(state, role);
			log("Terminal state goal: " + goal);
			return new Pair<List<Move>, Integer> (new ArrayList<Move>(), goal);
		}

		// Check all my available moves to find the best one
		List<Move> moves = stateMachine.getLegalMoves(state, role);

		String toLog = "My moves: [ ";
		for(Move move: moves){
			toLog += move + " ";
		}
		toLog += "]";
		log(toLog);

		Pair<List<Move>, Integer> maxResult = new Pair<List<Move>, Integer>(new ArrayList<Move>(),0);

		for (Move move: moves){

			log("Move [ " + move + " ]");

			Pair<List<Move>, Integer> currentResult;

			if(stateMachine.getRoles().size() == 1){
				ArrayList<Move> jointMoves = new ArrayList<Move>();
				jointMoves.add(move);
				currentResult = maxscore(stateMachine.getNextState(state, jointMoves), role);
			}else{
				currentResult = minscore(state, role, move);
			}

			// Check if the maximum score must be updated
			/*if(currentScore == 100){
				return move;
			}*/
			if(currentResult.getSecond().intValue() >= maxResult.getSecond().intValue()){
				maxResult = currentResult;
				maxResult.getFirst().add(move);
			}

			log("Current maxResult: " + maxResult.getSecond());
		}

		toLog = "Reversed actions sequence: [ ";
		for(Move m: maxResult.getFirst()){
			toLog += m + " ";
		}
		toLog += "]";
		log(toLog);

		return maxResult;
	}

	private Pair<List<Move>, Integer> minscore(MachineState state, Role role, Move move)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		log("Performing minscore");

		// Find all legal joint moves given the current move of the player
		List<List<Move>> jointMovesList = stateMachine.getLegalJointMoves(state, role, move);

		Pair<List<Move>, Integer> minResult = new Pair<List<Move>, Integer>(new ArrayList<Move>(),100);

		for(List<Move> jointMoves: jointMovesList){

			String toLog = "Joint moves: [ ";
			for(Move m: jointMoves){
				toLog += m + " ";
			}
			toLog += "]";
			log(toLog);

			Pair<List<Move>, Integer> currentResult;

			currentResult = maxscore(stateMachine.getNextState(state, jointMoves), role);
			if(currentResult.getSecond().intValue() <= minResult.getSecond().intValue()){
				minResult = currentResult;
			}
			log("Current minResult: " + minResult.getSecond());
		}

		String toLog = "Reversed actions sequence: [ ";
		for(Move m: minResult.getFirst()){
			toLog += m + " ";
		}
		toLog += "]";
		log(toLog);

		return minResult;
	}



}
