/**
 *
 */
package csironi.ggp.course.MCTS;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.finalMoveChioce.FinalMoveChoiceStrategy;
import csironi.ggp.course.MCTS.selection.SelectionStrategy;

/**
 * @author C.Sironi
 *
 */
public class MCTSController {

	/**
	 * Strategy that the player uses to perform selection (e.g. random, UCT, ...).
	 */
	SelectionStrategy selectionStrategy;

	/**
	 * Strategy that the player uses at the end of its turn to choose the move to be sent to the game manager
	 * (e.g. pick the action with highest number of visits, or the action with highest average score,...).
	 */
	FinalMoveChoiceStrategy finalMoveChoiceStrategy;

	/**
	 * Constructor that initializes the selection and the final move choice strategies.
	 *
	 * @param selectionStrategy the strategy that the player uses to perform selection.
	 * @param finalMoveChoiceStrategy the strategy that the player uses at the end of its
	 * turn to choose the move to be sent to the game manager.
	 */
	public MCTSController(SelectionStrategy selectionStrategy, FinalMoveChoiceStrategy finalMoveChoiceStrategy) {

		this.selectionStrategy = selectionStrategy;

		this.finalMoveChoiceStrategy = finalMoveChoiceStrategy;
	}


	public Move selectBestMove(long finishBy, StateMachine stateMachine, int myRoleIndex, MachineState currentState) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, StateMachineException{

		List<Role> roles = stateMachine.getRoles();
		List<Move> jointMove = new ArrayList<Move>();
		for(int i = 0; i < roles.size(); i++){
			jointMove.add(null);
		}

		MCTNode root = new MCTNode(stateMachine, currentState, myRoleIndex, myRoleIndex, null, jointMove);
		root.initializeChildren();

		List<Integer> goals;

		long startTime = System.currentTimeMillis();
		long endTime;
		long remainingTime;

		GamerLogger.log("Stats", "Starting MCTS at time: " + startTime);

		int numberOfIterations = 0;
		double avgTime;

		do{

			goals = selectionStrategy.select(root);
			root.update(goals);

			numberOfIterations++;

			endTime = System.currentTimeMillis();
			avgTime = (double) (endTime - startTime)/numberOfIterations;
			remainingTime = finishBy - endTime;

		}while(remainingTime > 2 * avgTime);

		GamerLogger.log("Stats", "Ending MCTS at time: " + System.currentTimeMillis());
		GamerLogger.log("Stats", "Number of simulations: " + numberOfIterations);
		GamerLogger.log("Stats", "Average time per simulation: " + avgTime +  "ms");

		return this.finalMoveChoiceStrategy.chooseFinalMove(root);

	}

}
