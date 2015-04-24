/**
 *
 */
package csironi.ggp.course.gamers;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import csironi.ggp.course.algorithms.MinMax;
import csironi.ggp.course.algorithms.SearchAlgorithm;

/**
 * Sequential planning gamer realized for the GGP course.
 * This gamer during the start clock searches the whole search space and builds a plan with all the legal actions
 * that lead to the best treminal state (i.e. the one with highest utility). Then for each game step returns the
 * corresponding action in the computed plan.
 *
 * NOTE: this player works for single-player games. If used to play multi-player games the uncertainty about other
 * players' actions might cause the plan to be inconsistent after the first step. This is because the plan is based on
 * certain opponents' actions that might be different from what the opponents actually chose.
 *
 *
 * @author C.Sironi
 *
 */
public class CSequentialGamer extends SampleGamer {

	private List<Move> bestPlan;

	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		StateMachine stateMachine = getStateMachine();

		SearchAlgorithm search = new MinMax(true, "C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\LOG\\SequentialLog.txt", stateMachine);
		this.bestPlan = search.bestmove(getCurrentState(), getRole());

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException {

		return bestPlan.remove(0);
	}

}
