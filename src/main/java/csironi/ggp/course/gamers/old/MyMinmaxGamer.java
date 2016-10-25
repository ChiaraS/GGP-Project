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
 * NOTE: this player only works on two-player games! Do not use it for single-player games or multi-player games!
 *
 * @see MyMinmaxPlusGamer for multi-player games.
 *
 * @author C.Sironi
 *
 */
public class MyMinmaxGamer extends SampleGamer {

	private PrintWriter out = null;


	/**
	 *
	 */
	public MyMinmaxGamer() {
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
			out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\LOG\\mylogMinmax.txt", true)));

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

		// Find out the index of my role in the list of roles
		Map<ProverRole,Integer> map = stateMachine.getRoleIndices();
		int myIndex = map.get(myRole);
		// Find out the index of the opponent's role in the list of roles
		// NOTE: we are assuming that we are dealing only with two-players games
		// thus if my index is 0 the opponent's index is 1 and vice versa.
		int opponentIndex = (myIndex+1)%2;

		out.println("My index: " + myIndex);
		out.println("Opponent's index: " + opponentIndex);

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

			// Compute the score for the current move
			int currentScore = minscore(state, myRole, move, myIndex, opponentIndex);

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
	private int minscore(ProverMachineState state, ProverRole myRole, ProverMove myMove, int myIndex, int opponentIndex)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		StateMachine stateMachine = getStateMachine();

		// Retrieve the list of all roles
		List<ProverRole> roles = stateMachine.getRoles();

		// Find legal moves for the opponent
		List<ProverMove> moves = stateMachine.getLegalMoves(state, roles.get(opponentIndex));

		out.print("Opponent moves: [ ");
		for(ProverMove move: moves){
			out.print(move + " ");
		}
		out.println("]");

		int minScore = 100;

		for (ProverMove move: moves){

			// Create the list of joint moves for this state with the same capacity of 2 (= total number if roles)
			ArrayList<ProverMove> jointMoves = new ArrayList<ProverMove>(2);
			// Initialization as null of all joint moves
			for(int i=0; i<2; i++){
				jointMoves.add(null);
			}
			jointMoves.set(myIndex, myMove);
			jointMoves.set(opponentIndex, move);

			int currentScore = maxscore(stateMachine.getNextState(state, jointMoves), myRole, myIndex, opponentIndex);
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
	private int maxscore(ProverMachineState state, ProverRole myRole, int myIndex, int opponentIndex)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException{

		StateMachine stateMachine = getStateMachine();

		// Check if the state is terminal
		if(stateMachine.isTerminal(state)){
			int goal = stateMachine.getGoal(state, myRole);
			out.println("Terminal state goal: " + goal);
			return goal;
		}

		// Check all my available moves to find the best one
		List<ProverMove> myMoves = stateMachine.getLegalMoves(state, myRole);

		out.print("My moves: [ ");
		for(ProverMove move: myMoves){
			out.print(move + " ");
		}
		out.println("]");

		int maxScore = 0;

		for (ProverMove move: myMoves){

			// Compute the score for the current move
			int currentScore = minscore(state, myRole, move, myIndex, opponentIndex);

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
