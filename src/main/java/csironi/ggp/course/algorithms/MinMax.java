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
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.utils.Pair;

/**
 * @author C.Sironi
 *
 */
public class MinMax extends SearchAlgorithm {

	/**
	 *
	 */
	public MinMax(boolean log, String logFileName, StateMachine stateMachine) {
		super(log, logFileName, stateMachine);
	}



	/* (non-Javadoc)
	 * @see csironi.ggp.course.gamers.algorithms.SearchAlgorithm#bestmove()
	 */
	@Override
	public List<Move> bestmove(MachineState state, Role role)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {

		Pair<List<Move>, Integer> result = maxscore(state, role);

		// Revert the order of the actions since they are listed from last to first wrt to the root.
		List<Move> bestPathMoves = new ArrayList<Move>();
		for(Move move: result.getFirst()){
			bestPathMoves.add(0, move);
		}

		return bestPathMoves;
	}


	private Pair<List<Move>, Integer> maxscore(MachineState state, Role role/*MachineState state, Role myRole, List<Role> roles, int myIndex*/)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException{

		// Check if the state is terminal
		if(stateMachine.isTerminal(state)){
			int goal = stateMachine.getGoal(state, role);
			//out.println("Terminal state goal: " + goal);
			return new Pair<List<Move>, Integer> (new ArrayList<Move>(), goal);
		}

		// Check all my available moves to find the best one
		List<Move> moves = stateMachine.getLegalMoves(state, role);

		//out.print("My moves: [ ");
		//for(Move move: myMoves){
		//	out.print(move + " ");
		//}
		//out.println("]");

		Pair<List<Move>, Integer> maxResult = new Pair<List<Move>, Integer>(null,0);

		for (Move move: moves){

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
			if(currentResult.getSecond().intValue() > maxResult.getSecond().intValue()){
				maxResult = currentResult;
				maxResult.getFirst().add(move);
			}
			//out.println("Current maxscore: " + maxScore);
		}

		return maxResult;
	}

	private Pair<List<Move>, Integer> minscore(MachineState state, Role role, Move move)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException{

		// Find all legal joint moves given the current move of the player
		List<List<Move>> jointMovesList = stateMachine.getLegalJointMoves(state, role, move);

		//out.print("Opponent moves: [ ");
		//for(Move move: moves){
		//	out.print(move + " ");
		//}
		//out.println("]");

		Pair<List<Move>, Integer> minResult = new Pair<List<Move>, Integer>(null,100);

		for(List<Move> jointMoves: jointMovesList){

			Pair<List<Move>, Integer> currentResult;

			currentResult = maxscore(stateMachine.getNextState(state, jointMoves), role);
			if(currentResult.getSecond().intValue() < minResult.getSecond().intValue()){
				minResult = currentResult;
			}
			//out.println("Current minscore: " + minScore);
		}

		return minResult;
	}



}
