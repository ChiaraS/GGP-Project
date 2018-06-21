/**
 *
 */
package csironi.ggp.course.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import csironi.ggp.course.evalfunctions.EvaluationFunction;

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

	public ExplicitMove bestmove(long finishBy, ExplicitMachineState state, ExplicitRole role, Boolean prune, double alpha, double beta, int limit, boolean shuffleTop, boolean shuffleInt, EvaluationFunction stateEval)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {

		log("\nStarting bestmove with depth limit " + limit);


		/*Only for log*/
		List<ExplicitRole> roles = stateMachine.getExplicitRoles();
		String toLog = "Roles: [ ";
		for(ExplicitRole r: roles){
			toLog += r + " ";
		}
		toLog += "]";
		log(toLog);

		List<ExplicitMove> moves = stateMachine.getExplicitLegalMoves(state, role);

		toLog = "My moves: [ ";
		for(ExplicitMove move: moves){
			toLog += move + " ";
		}
		toLog += "]";
		log(toLog);

		ExplicitMove selection = moves.get(0);

		if(shuffleTop){

			log("Shuffling top moves");

			// Build an array of indexes
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			for(int i=0; i < moves.size(); i++){
				indexes.add(i);
			}

			Collections.shuffle(indexes);

			toLog = "My moves: [ ";
			for(Integer i: indexes){
				toLog += moves.get(i) + " ";
			}
			toLog += "]";
			log(toLog);

			selection = moves.get(indexes.get(0));

			if(limit <= 0){

				log("Negative or 0 depth limit. Returning first legal action: " + selection);

				return selection;
			}

			if(prune){

				for (Integer i: indexes){

					ExplicitMove move = moves.get(i);

					double currentScore;

					if(stateMachine.getExplicitRoles().size() == 1){
						ArrayList<ExplicitMove> jointMoves = new ArrayList<ExplicitMove>();
						jointMoves.add(move);
						currentScore = maxscore(finishBy, stateMachine.getExplicitNextState(state, jointMoves), role, alpha, beta, 1, limit, shuffleInt, stateEval);
					}else{
						currentScore = minscore(finishBy, state, role, move, alpha, beta, 0, limit, shuffleInt, stateEval);
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
				double maxScore = 0;

				for (Integer i: indexes){

					ExplicitMove move = moves.get(i);

					double currentScore;

					if(stateMachine.getExplicitRoles().size() == 1){
						ArrayList<ExplicitMove> jointMoves = new ArrayList<ExplicitMove>();
						jointMoves.add(move);
						currentScore = maxscore(finishBy, stateMachine.getExplicitNextState(state, jointMoves), role, 1, limit, shuffleInt, stateEval);
					}else{
						currentScore = minscore(finishBy, state, role, move, 0, limit, shuffleInt, stateEval);
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

		}else{

			if(limit <= 0){

				log("Negative or 0 depth limit. Returning first legal action: " + selection);

				return selection;
			}

			if(prune){

				for (ExplicitMove move: moves){

					double currentScore;

					if(stateMachine.getExplicitRoles().size() == 1){
						ArrayList<ExplicitMove> jointMoves = new ArrayList<ExplicitMove>();
						jointMoves.add(move);
						currentScore = maxscore(finishBy, stateMachine.getExplicitNextState(state, jointMoves), role, alpha, beta, 1, limit, shuffleInt, stateEval);
					}else{
						currentScore = minscore(finishBy, state, role, move, alpha, beta, 0, limit, shuffleInt, stateEval);
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
				double maxScore = 0;

				for (ExplicitMove move: moves){

					double currentScore;

					if(stateMachine.getExplicitRoles().size() == 1){
						ArrayList<ExplicitMove> jointMoves = new ArrayList<ExplicitMove>();
						jointMoves.add(move);
						currentScore = maxscore(finishBy, stateMachine.getExplicitNextState(state, jointMoves), role, 1, limit, shuffleInt, stateEval);
					}else{
						currentScore = minscore(finishBy, state, role, move, 0, limit, shuffleInt, stateEval);
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

		}



		log("Returned move " + selection);

		return selection;
	}


	private double maxscore(long finishBy, ExplicitMachineState state, ExplicitRole role, int level, int limit, boolean shuffle, EvaluationFunction stateEval)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		if(System.currentTimeMillis() > finishBy){
			log("Timeout detected.");
			this.timedOut = true;
			return 0;
		}

		log("Performing maxscore");

		// Check if the state is terminal
		if(stateMachine.isTerminal(state)){
			double goal = stateMachine.getGoal(state, role);
			log("Terminal state goal: " + goal);
			return goal;
		}

		if(level >= limit){

			double stateValue = stateEval.eval(state, role);
			log("Reached depth limit. Returning state value estimation: " + stateValue);
			return stateValue;

		}

		// Check all my available moves to find the best one
		List<ExplicitMove> moves = stateMachine.getExplicitLegalMoves(state, role);

		double maxScore = 0;

		if(shuffle){

			log("Shuffling internal moves");

			// Build an array of indexes
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			for(int i=0; i < moves.size(); i++){
				indexes.add(i);
			}

			Collections.shuffle(indexes);

			String toLog = "My moves: [ ";
			for(Integer i: indexes){
				toLog += moves.get(i) + " ";
			}
			toLog += "]";
			log(toLog);

			for (Integer i: indexes){

				ExplicitMove move = moves.get(i);

				log("Move [ " + move + " ]");

				double currentScore;

				if(stateMachine.getExplicitRoles().size() == 1){
					ArrayList<ExplicitMove> jointMoves = new ArrayList<ExplicitMove>();
					jointMoves.add(move);
					currentScore = maxscore(finishBy, stateMachine.getExplicitNextState(state, jointMoves), role, level+1, limit, shuffle, stateEval);
				}else{
					currentScore = minscore(finishBy, state, role, move, level, limit, shuffle, stateEval);
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



		}else{

			String toLog = "My moves: [ ";
			for(ExplicitMove move: moves){
				toLog += move + " ";
			}
			toLog += "]";
			log(toLog);

			for (ExplicitMove move: moves){

				log("Move [ " + move + " ]");

				double currentScore;

				if(stateMachine.getExplicitRoles().size() == 1){
					ArrayList<ExplicitMove> jointMoves = new ArrayList<ExplicitMove>();
					jointMoves.add(move);
					currentScore = maxscore(finishBy, stateMachine.getExplicitNextState(state, jointMoves), role, level+1, limit, shuffle, stateEval);
				}else{
					currentScore = minscore(finishBy, state, role, move, level, limit, shuffle, stateEval);
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
		}

		return maxScore;
	}

	private double minscore(long finishBy, ExplicitMachineState state, ExplicitRole role, ExplicitMove move, int level, int limit, boolean shuffle, EvaluationFunction stateEval)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		log("Performing minscore");

		// Find all legal joint moves given the current move of the player
		List<List<ExplicitMove>> jointMovesList = stateMachine.getLegalJointMoves(state, role, move);

		double minScore = 100;

		for(List<ExplicitMove> jointMoves: jointMovesList){

			String toLog = "Joint moves: [ ";
			for(ExplicitMove m: jointMoves){
				toLog += m + " ";
			}
			toLog += "]";
			log(toLog);

			double currentScore;

			currentScore = maxscore(finishBy, stateMachine.getExplicitNextState(state, jointMoves), role, level+1, limit, shuffle, stateEval);

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


	private double maxscore(long finishBy, ExplicitMachineState state, ExplicitRole role, double alpha, double beta, int level, int limit, boolean shuffle, EvaluationFunction stateEval)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		if(System.currentTimeMillis() > finishBy){
			log("Timeout detected.");
			this.timedOut = true;
			return 0;
		}

		log("Performing maxscore");

		// Check if the state is terminal
		if(stateMachine.isTerminal(state)){
			double goal = stateMachine.getGoal(state, role);
			log("Terminal state goal: " + goal);
			return goal;
		}

		if(level >= limit){

			double stateValue = stateEval.eval(state, role);
			log("Reached depth limit. Returning state value estimation: " + stateValue);
			return stateValue;
		}

		// Check all my available moves to find the best one
		List<ExplicitMove> moves = stateMachine.getExplicitLegalMoves(state, role);

		if(shuffle){

			log("Shuffling internal moves");

			// Build an array of indexes
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			for(int i=0; i < moves.size(); i++){
				indexes.add(i);
			}

			Collections.shuffle(indexes);

			String toLog = "My moves: [ ";
			for(Integer i: indexes){
				toLog += moves.get(i) + " ";
			}
			toLog += "]";
			log(toLog);

			for (Integer i: indexes){

				ExplicitMove move = moves.get(i);

				log("Move [ " + move + " ]");

				double currentScore;

				if(stateMachine.getExplicitRoles().size() == 1){
					ArrayList<ExplicitMove> jointMoves = new ArrayList<ExplicitMove>();
					jointMoves.add(move);
					currentScore = maxscore(finishBy, stateMachine.getExplicitNextState(state, jointMoves), role, alpha, beta, level+1, limit, shuffle, stateEval);
				}else{
					currentScore = minscore(finishBy, state, role, move, alpha, beta, level, limit, shuffle, stateEval);
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

		}else{

			String toLog = "My moves: [ ";
			for(ExplicitMove move: moves){
				toLog += move + " ";
			}
			toLog += "]";
			log(toLog);

			for (ExplicitMove move: moves){

				log("Move [ " + move + " ]");

				double currentScore;

				if(stateMachine.getExplicitRoles().size() == 1){
					ArrayList<ExplicitMove> jointMoves = new ArrayList<ExplicitMove>();
					jointMoves.add(move);
					currentScore = maxscore(finishBy, stateMachine.getExplicitNextState(state, jointMoves), role, alpha, beta, level+1, limit, shuffle, stateEval);
				}else{
					currentScore = minscore(finishBy, state, role, move, alpha, beta, level, limit, shuffle, stateEval);
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
		}

		log("Returning alpha");
		return alpha;
	}

	private double minscore(long finishBy, ExplicitMachineState state, ExplicitRole role, ExplicitMove move, double alpha, double beta, int level, int limit, boolean shuffle, EvaluationFunction stateEval)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		log("Performing minscore");

		// Find all legal joint moves given the current move of the player
		List<List<ExplicitMove>> jointMovesList = stateMachine.getLegalJointMoves(state, role, move);

		for(List<ExplicitMove> jointMoves: jointMovesList){

			String toLog = "Joint moves: [ ";
			for(ExplicitMove m: jointMoves){
				toLog += m + " ";
			}
			toLog += "]";
			log(toLog);

			double currentScore;

			currentScore = maxscore(finishBy, stateMachine.getExplicitNextState(state, jointMoves), role, alpha, beta, level+1, limit, shuffle, stateEval);

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
