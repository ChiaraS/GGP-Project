/**
 *
 */
package csironi.ggp.course.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;

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


	public List<ProverMove> bestmove(ProverMachineState state, ProverRole role)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		log("Starting bestscore");


		/*Only for log*/
		List<ProverRole> roles = stateMachine.getRoles();
		String toLog = "Roles: [ ";
		for(ProverRole r: roles){
			toLog += r + " ";
		}
		toLog += "]";
		log(toLog);

		Pair<List<ProverMove>, Integer> result = maxscore(state, role);

		toLog = "Reversed actions sequence: [ ";
		for(ProverMove m: result.getFirst()){
			toLog += m + " ";
		}
		toLog += "]";
		log(toLog);

		// Revert the order of the actions since they are listed from last to first wrt to the root.
		List<ProverMove> bestPathMoves = new ArrayList<ProverMove>();
		for(ProverMove move: result.getFirst()){
			bestPathMoves.add(0, move);
		}

		toLog = "Actions sequence: [ ";
		for(ProverMove m: bestPathMoves){
			toLog += m + " ";
		}
		toLog += "]";
		log(toLog);


		return bestPathMoves;
	}


	private Pair<List<ProverMove>, Integer> maxscore(ProverMachineState state, ProverRole role)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		log("Performing maxscore");

		// Check if the state is terminal
		if(stateMachine.isTerminal(state)){
			int goal = stateMachine.getGoal(state, role);
			log("Terminal state goal: " + goal);
			return new Pair<List<ProverMove>, Integer> (new ArrayList<ProverMove>(), goal);
		}

		// Check all my available moves to find the best one
		List<ProverMove> moves = stateMachine.getLegalMoves(state, role);

		String toLog = "My moves: [ ";
		for(ProverMove move: moves){
			toLog += move + " ";
		}
		toLog += "]";
		log(toLog);

		Pair<List<ProverMove>, Integer> maxResult = new Pair<List<ProverMove>, Integer>(new ArrayList<ProverMove>(),0);

		for (ProverMove move: moves){

			log("Move [ " + move + " ]");

			Pair<List<ProverMove>, Integer> currentResult;

			if(stateMachine.getRoles().size() == 1){
				ArrayList<ProverMove> jointMoves = new ArrayList<ProverMove>();
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
		for(ProverMove m: maxResult.getFirst()){
			toLog += m + " ";
		}
		toLog += "]";
		log(toLog);

		return maxResult;
	}

	private Pair<List<ProverMove>, Integer> minscore(ProverMachineState state, ProverRole role, ProverMove move)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		log("Performing minscore");

		// Find all legal joint moves given the current move of the player
		List<List<ProverMove>> jointMovesList = stateMachine.getLegalJointMoves(state, role, move);

		Pair<List<ProverMove>, Integer> minResult = new Pair<List<ProverMove>, Integer>(new ArrayList<ProverMove>(),100);

		for(List<ProverMove> jointMoves: jointMovesList){

			String toLog = "Joint moves: [ ";
			for(ProverMove m: jointMoves){
				toLog += m + " ";
			}
			toLog += "]";
			log(toLog);

			Pair<List<ProverMove>, Integer> currentResult;

			currentResult = maxscore(stateMachine.getNextState(state, jointMoves), role);
			if(currentResult.getSecond().intValue() <= minResult.getSecond().intValue()){
				minResult = currentResult;
			}
			log("Current minResult: " + minResult.getSecond());
		}

		String toLog = "Reversed actions sequence: [ ";
		for(ProverMove m: minResult.getFirst()){
			toLog += m + " ";
		}
		toLog += "]";
		log(toLog);

		return minResult;
	}



}
