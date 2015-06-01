/**
 *
 */
package csironi.ggp.course.MCTS;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.MCTS.finalMoveChioce.FinalMoveChoiceStrategy;
import csironi.ggp.course.MCTS.selection.SelectionStrategy;

/**
 * @author C.Sironi
 *
 */
public class MCTSManager {

	SelectionStrategy selectionStrategy;

	FinalMoveChoiceStrategy finalMoveChoiceStrategy;

	/**
	 *
	 */
	public MCTSManager(SelectionStrategy selectionStrategy, FinalMoveChoiceStrategy finalMoveChoiceStrategy) {

		this.selectionStrategy = selectionStrategy;

		this.finalMoveChoiceStrategy = finalMoveChoiceStrategy;
	}


	public Move selectBestMove(long finishBy, StateMachine stateMachine, int myRoleIndex, MachineState currentState) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{

		List<Role> roles = stateMachine.getRoles();
		List<Move> jointMoves = new ArrayList<Move>();
		for(int i = 0; i < roles.size(); i++){
			jointMoves.add(null);
		}

		MCTNode root = new MCTNode(stateMachine, currentState, myRoleIndex, myRoleIndex, null, jointMoves);
		root.initializeChildren();

		List<Integer> goals;

		long startTime = System.currentTimeMillis();
		int numberOfIterations = 0;
		long avgTime;

		do{

			goals = selectionStrategy.select(root);
			root.update(goals);

			numberOfIterations++;
			avgTime = (System.currentTimeMillis() - startTime)/numberOfIterations;

		}while(System.currentTimeMillis() + 2*avgTime < finishBy);

		return this.finalMoveChoiceStrategy.chooseFinalMove(root);

	}

}
