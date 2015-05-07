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

	public Move bestmove(long finishBy, MachineState state, Role role, Boolean prune, int alpha, int beta, int limit)
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

		if(limit <= 0){

			log("Negative or 0 depth limit. Returning first legal action: " + selection);

			return selection;
		}

		if(prune){

			for (Move move: moves){

				int currentScore;

				if(stateMachine.getRoles().size() == 1){
					ArrayList<Move> jointMoves = new ArrayList<Move>();
					jointMoves.add(move);
					currentScore = maxscore(finishBy, stateMachine.getNextState(state, jointMoves), role, alpha, beta, 1, limit);
				}else{
					currentScore = minscore(finishBy, state, role, move, alpha, beta, 0, limit);
				}

				// If reached the timeout return the best move found so far excluding the one that was being investigated during timeout.
				// The information about this last move will be unreliable.
				// Otherwise check if the time is out and return the best move found so far including the last investigated move.
				if(timedOut){

					log("Ignoring last investigated move.");

					break;
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

				if(System.currentTimeMillis() > finishBy){
					log("Timeout detected.");
					this.timedOut = true;
					break;
				}

			}

		}else{
			int maxScore = 0;

			for (Move move: moves){

				int currentScore;

				if(stateMachine.getRoles().size() == 1){
					ArrayList<Move> jointMoves = new ArrayList<Move>();
					jointMoves.add(move);
					currentScore = maxscore(finishBy, stateMachine.getNextState(state, jointMoves), role, 1, limit);
				}else{
					currentScore = minscore(finishBy, state, role, move, 0, limit);
				}

				// If reached the timeout return the best move found so far excluding the one that was being investigated during timeout.
				// The information about this last move will be unreliable.
				// Otherwise check if the time is out and return the best move found so far including the last investigated move.
				if(timedOut){

					log("Ignoring last investigated move.");

					break;
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

				if(System.currentTimeMillis() > finishBy){
					log("Timeout detected.");
					this.timedOut = true;
					break;
				}
			}

		}

		log("Returned move " + selection);

		return selection;
	}


	private int maxscore(long finishBy, MachineState state, Role role, int level, int limit)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException{

		if(System.currentTimeMillis() > finishBy){
			log("Timeout detected.");
			this.timedOut = true;
			return 0;
		}

		log("Performing maxscore");

		// Check if the state is terminal
		if(stateMachine.isTerminal(state)){
			int goal = stateMachine.getGoal(state, role);
			log("Terminal state goal: " + goal);
			return goal;
		}

		if(level >= limit){

			log("Reached depth limit. Returning state value estimation: 0");
			return 0;
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
				currentScore = maxscore(finishBy, stateMachine.getNextState(state, jointMoves), role, level+1, limit);
			}else{
				currentScore = minscore(finishBy, state, role, move, level, limit);
			}

			if(timedOut){
				log("Stopping maxscore.");
				break;
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

	private int minscore(long finishBy, MachineState state, Role role, Move move, int level, int limit)
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

			currentScore = maxscore(finishBy, stateMachine.getNextState(state, jointMoves), role, level+1, limit);

			if(timedOut){
				log("Stopping minscore.");
				break;
			}

			if(currentScore < minScore){
				minScore = currentScore;
			}
			log("Current minScore: " + minScore);
		}

		return minScore;
	}


	private int maxscore(long finishBy, MachineState state, Role role, int alpha, int beta, int level, int limit)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException{

		if(System.currentTimeMillis() > finishBy){
			log("Timeout detected.");
			this.timedOut = true;
			return 0;
		}

		log("Performing maxscore");

		// Check if the state is terminal
		if(stateMachine.isTerminal(state)){
			int goal = stateMachine.getGoal(state, role);
			log("Terminal state goal: " + goal);
			return goal;
		}

		if(level >= limit){
			log("Reached depth limit. Returning state value estimation: 0");
			return 0;
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
				currentScore = maxscore(finishBy, stateMachine.getNextState(state, jointMoves), role, alpha, beta, level+1, limit);
			}else{
				currentScore = minscore(finishBy, state, role, move, alpha, beta, level, limit);
			}

			if(timedOut){
				log("Stopping maxscore.");
				break;
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

	private int minscore(long finishBy, MachineState state, Role role, Move move, int alpha, int beta, int level, int limit)
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

			currentScore = maxscore(finishBy, stateMachine.getNextState(state, jointMoves), role, alpha, beta, level+1, limit);

			if(timedOut){
				log("Stopping maxscore.");
				break;
			}

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
