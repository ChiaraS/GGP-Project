/**
 *
 */
package csironi.ggp.course.gamers.old;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * Implementation of a MinMax player for the GGP course.
 *
 * Does this implementation of minmax make sense for games with 3 or more players?
 *
 * NOTE: this player only works on multi-player games! Do not use it for single-player games!
 * @author C.Sironi
 *
 */
public class MyMinmaxPlusGamer extends SampleGamer {

	private PrintWriter out = null;

	/**
	 *
	 */
	public MyMinmaxPlusGamer() {
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
		ProverRole myRole = getRole();

		List<ProverMove> myMoves = stateMachine.getLegalMoves(state, myRole);
		ProverMove selection = myMoves.get(0);

		try{
			out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\LOG\\mylogMinmaxPlus.txt", true)));

			// If I only have one available move I will return that one otherwise I'll perform minmax search
			if(myMoves.size() != 1){
				selection = bestmove(myRole, state);
			}

			long stop = System.currentTimeMillis();

			notifyObservers(new GamerSelectedMoveEvent(myMoves, selection, stop - start));

		}catch(Exception e){
			System.out.println("Oh guarda...un'eccezione");
		}finally{
		    if(out != null){
		        out.close();
		    }
		}

		return selection;
	}

	/**
	 * @throws StateMachineException
	 *
	 *
	 */
	private ProverMove bestmove(ProverRole myRole, ProverMachineState state)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		StateMachine stateMachine = getStateMachine();

		// Find out what index my role has in the list of roles
		Map<ProverRole,Integer> map = stateMachine.getRoleIndices();
		int myIndex = map.get(myRole);


		out.println("Begin");
		out.println("My role: " + myRole);
		out.println("My index: " + myIndex);

		// Find the index of the opponent's role that needs to be investigated next
		// (i.e. All the roles must be investigated, starting from index 0 to the last index.
		// This check makes sure that my role won't be investigated as an opponent, thus as a min node).
		int nextOpponentIndex = 0;
		if(nextOpponentIndex == myIndex){
			nextOpponentIndex = 1;
		}

		out.println("Next opponent index: " + nextOpponentIndex);

		// Retrieve the list of all roles
		List<ProverRole> roles = stateMachine.getRoles();

		out.print("Roles: [ ");
		for(ProverRole role: roles){
			out.print(role + " ");
		}
		out.println("]");

		int numberOfRoles = roles.size();
		out.println("Number of roles: " + numberOfRoles);

		// Check all my available moves to find the best one
		List<ProverMove> myMoves = stateMachine.getLegalMoves(state, myRole);

		out.print("My moves: [ ");
		for(ProverMove move: myMoves){
			out.print(move + " ");
		}
		out.println("]");

		ProverMove selection = myMoves.get(0);
		int maxScore = 0;

		for (ProverMove move: myMoves){
			// Create the list of joint moves for this state with the same capacity as the size of the list of roles
			ArrayList<ProverMove> jointMoves = new ArrayList<ProverMove>(numberOfRoles);
			// Initialization as null of all joint moves
			for(int i=0; i<numberOfRoles; i++){
				jointMoves.add(null);
			}
			jointMoves.set(myIndex, move);

			// Compute the score for the current move
			int currentScore = minscore(state, myRole, roles, jointMoves, myIndex, nextOpponentIndex);

			// Check if the maximum score must be updated
			/*if(currentScore == 100){
				return move;
			}*/
			if(currentScore > maxScore){
				maxScore = currentScore;
				selection = move;
			}

			out.println("Current maxscore: " + maxScore);

		}

		return selection;

	}


	/**
	 * @throws StateMachineException
	 *
	 *
	 */
	private int minscore(ProverMachineState state, ProverRole myRole, List<ProverRole> roles, List<ProverMove> jointMoves, int myIndex, int thisOpponentIndex)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		// Find the index of the opponent's role that needs to be investigated next
		// (i.e. All the roles must be investigated, starting from index 0 to the last index.
		// This check makes sure that my role won't be investigated as an opponent, thus as a min node).
		int nextOpponentIndex = thisOpponentIndex + 1;
		if(nextOpponentIndex == myIndex){
			nextOpponentIndex++;
		}

		StateMachine stateMachine = getStateMachine();

		// Find legal moves for the current opponent
		List<ProverMove> moves = stateMachine.getLegalMoves(state, roles.get(thisOpponentIndex));

		out.print("Opponent moves: [ ");
		for(ProverMove move: moves){
			out.print(move + " ");
		}
		out.println("]");

		int minScore = 100;

		for (ProverMove move: moves){
			// Add this move to a copy of the joint moves list in the position specified
			// by the thisOpponentIndex variable.
			// NOTE: the array is copied for safety reasons (i.e. assignments for a position of the array
			// won't overlap), but using the same array for each recursive call shouldn't be a problem,
			// on the contrary it should save space.
			ArrayList<ProverMove> newJointMoves = new ArrayList<ProverMove>(jointMoves);
			newJointMoves.set(thisOpponentIndex, move);

			// Check if this is the last min node and thus the maxscore has to be called next
			// and the state must be advanced. Otherwise the minscore will be called.
			int currentScore = 0;
			if(nextOpponentIndex >= roles.size()){
				// Advance the state
				ProverMachineState nextState = stateMachine.getNextState(state, newJointMoves);
				currentScore = maxscore(nextState, myRole, roles, myIndex);
			}else{
				currentScore = minscore(state, myRole, roles, newJointMoves, myIndex, nextOpponentIndex);
			}
			if(currentScore < minScore){
				minScore = currentScore;
			}
			out.println("Current minscore: " + minScore);
		}

		return minScore;
	}

	/**
	 * @throws StateMachineException
	 *
	 *
	 */
	private int maxscore(ProverMachineState state, ProverRole myRole, List<ProverRole> roles, int myIndex)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		StateMachine stateMachine = getStateMachine();

		// Check if the state is terminal
		if(stateMachine.isTerminal(state)){
			int goal = stateMachine.getGoal(state, myRole);
			out.println("Terminal state goal: " + goal);
			return goal;
		}

		// Find the index of the opponent's role that needs to be investigated next
		// (i.e. All the roles must be investigated, starting from index 0 to the last index.
		// This check makes sure that my role won't be investigated as an opponent, thus as a min node).
		int nextOpponentIndex = 0;
		if(nextOpponentIndex == myIndex){
			nextOpponentIndex = 1;
		}

		// Check all my available moves to find the best one
		List<ProverMove> myMoves = stateMachine.getLegalMoves(state, myRole);

		out.print("My moves: [ ");
		for(ProverMove move: myMoves){
			out.print(move + " ");
		}
		out.println("]");

		int maxScore = 0;

		int numberOfRoles = roles.size();

		for (ProverMove move: myMoves){
			// Create the list of joint moves for this state with the same capacity as the size of the list of roles
			ArrayList<ProverMove> jointMoves = new ArrayList<ProverMove>(numberOfRoles);
			// Initialization as null of all joint moves
			for(int i=0; i<numberOfRoles; i++){
				jointMoves.add(null);
			}
			jointMoves.set(myIndex, move);

			// Compute the score for the current move
			int currentScore = minscore(state, myRole, roles, jointMoves, myIndex, nextOpponentIndex);

			// Check if the maximum score must be updated
			/*if(currentScore == 100){
				return move;
			}*/
			if(currentScore > maxScore){
				maxScore = currentScore;
			}
			out.println("Current maxscore: " + maxScore);
		}

		return maxScore;
	}

}
