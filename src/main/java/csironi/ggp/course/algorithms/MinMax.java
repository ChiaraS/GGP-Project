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

/**
 *
 *
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

	public Move bestmove(MachineState state, Role role, Boolean prune, int alpha, int beta)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {

		log("Starting bestmove");


		/*Only for log*/
		List<Role> roles = stateMachine.getRoles();
		String toLog = "Roles: [ ";
		for(Role r: roles){
			toLog += r + " ";
		}
		toLog += "]";
		log(toLog);

		List<Move> moves = stateMachine.getLegalMoves(state, role);

		toLog = "My moves: [ ";
		for(Move move: moves){
			toLog += move + " ";
		}
		toLog += "]";
		log(toLog);

		Move selection = moves.get(0);

		if(prune){

			for (Move move: moves){

				int currentScore;

				if(stateMachine.getRoles().size() == 1){
					ArrayList<Move> jointMoves = new ArrayList<Move>();
					jointMoves.add(move);
					currentScore = maxscore(stateMachine.getNextState(state, jointMoves), role, alpha, beta);
				}else{
					currentScore = minscore(state, role, move, alpha, beta);
				}

				if(currentScore > alpha){
					alpha = currentScore;
					selection = move;
				}

				log("Current maxScore (alpha): " + alpha);

				if(alpha >= beta){
					log("Alpa >= beta: stopping!");
					log("Returned move " + move);
					return move;
				}

			}

		}else{
			int maxScore = 0;

			for (Move move: moves){

				int currentScore;

				if(stateMachine.getRoles().size() == 1){
					ArrayList<Move> jointMoves = new ArrayList<Move>();
					jointMoves.add(move);
					currentScore = maxscore(stateMachine.getNextState(state, jointMoves), role);
				}else{
					currentScore = minscore(state, role, move);
				}

				// Check if the maximum score must be updated
				/*if(currentScore == 100){
					return move;
				}*/
				if(currentScore > maxScore){
					maxScore = currentScore;
					selection = move;
				}

				log("Current maxScore: " + maxScore);
			}

		}

		log("Returned move " + selection);

		return selection;
	}


	private int maxscore(MachineState state, Role role)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException{

		log("Performing maxscore");

		// Check if the state is terminal
		if(stateMachine.isTerminal(state)){
			int goal = stateMachine.getGoal(state, role);
			log("Terminal state goal: " + goal);
			return goal;
		}

		// Check all my available moves to find the best one
		List<Move> moves = stateMachine.getLegalMoves(state, role);

		String toLog = "My moves: [ ";
		for(Move move: moves){
			toLog += move + " ";
		}
		toLog += "]";
		log(toLog);

		int maxScore = 0;

		for (Move move: moves){

			log("Move [ " + move + " ]");

			int currentScore;

			if(stateMachine.getRoles().size() == 1){
				ArrayList<Move> jointMoves = new ArrayList<Move>();
				jointMoves.add(move);
				currentScore = maxscore(stateMachine.getNextState(state, jointMoves), role);
			}else{
				currentScore = minscore(state, role, move);
			}

			// Check if the maximum score must be updated
			/*if(currentScore == 100){
				return move;
			}*/
			if(currentScore > maxScore){
				maxScore = currentScore;
			}

			log("Current maxScore: " + maxScore);
		}

		return maxScore;
	}

	private int minscore(MachineState state, Role role, Move move)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException{

		log("Performing minscore");

		// Find all legal joint moves given the current move of the player
		List<List<Move>> jointMovesList = stateMachine.getLegalJointMoves(state, role, move);

		int minScore = 100;

		for(List<Move> jointMoves: jointMovesList){

			String toLog = "Joint moves: [ ";
			for(Move m: jointMoves){
				toLog += m + " ";
			}
			toLog += "]";
			log(toLog);

			int currentScore;

			currentScore = maxscore(stateMachine.getNextState(state, jointMoves), role);
			if(currentScore < minScore){
				minScore = currentScore;
			}
			log("Current minScore: " + minScore);
		}

		return minScore;
	}


	private int maxscore(MachineState state, Role role, int alpha, int beta)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException{

		log("Performing maxscore");

		// Check if the state is terminal
		if(stateMachine.isTerminal(state)){
			int goal = stateMachine.getGoal(state, role);
			log("Terminal state goal: " + goal);
			return goal;
		}

		// Check all my available moves to find the best one
		List<Move> moves = stateMachine.getLegalMoves(state, role);

		String toLog = "My moves: [ ";
		for(Move move: moves){
			toLog += move + " ";
		}
		toLog += "]";
		log(toLog);

		for (Move move: moves){

			log("Move [ " + move + " ]");

			int currentScore;

			if(stateMachine.getRoles().size() == 1){
				ArrayList<Move> jointMoves = new ArrayList<Move>();
				jointMoves.add(move);
				currentScore = maxscore(stateMachine.getNextState(state, jointMoves), role, alpha, beta);
			}else{
				currentScore = minscore(state, role, move, alpha, beta);
			}

			// Check if the maximum score must be updated
			/*if(currentScore == 100){
				return move;
			}*/
			if(currentScore > alpha){
				alpha = currentScore;
			}

			log("Current alpha: " + alpha);
			log("Current beta: " + beta);

			if(alpha >= beta){
				log("Returning beta");
				return beta;
			}
		}

		log("Returning alpha");
		return alpha;
	}

	private int minscore(MachineState state, Role role, Move move, int alpha, int beta)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException{

		log("Performing minscore");

		// Find all legal joint moves given the current move of the player
		List<List<Move>> jointMovesList = stateMachine.getLegalJointMoves(state, role, move);

		for(List<Move> jointMoves: jointMovesList){

			String toLog = "Joint moves: [ ";
			for(Move m: jointMoves){
				toLog += m + " ";
			}
			toLog += "]";
			log(toLog);

			int currentScore;

			currentScore = maxscore(stateMachine.getNextState(state, jointMoves), role, alpha, beta);
			if(currentScore < beta){
				beta = currentScore;
			}

			log("Current alpha: " + alpha);
			log("Current beta: " + beta);

			if(beta <= alpha){
				log("Returning alpha");
				return alpha;
			}
		}

		log("Returning beta");

		return beta;
	}

}
