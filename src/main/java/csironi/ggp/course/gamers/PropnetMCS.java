/**
 *
 */
package csironi.ggp.course.gamers;

import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.abstractsm.ExplicitStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.propnet.FwdInterrPropnetStateMachine;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

/**
 * @author C.Sironi
 *
 */
public class PropnetMCS extends SampleGamer {

	/**
	 *
	 */
	public PropnetMCS() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineSelectMove(long)
	 */
	@Override
	public ExplicitMove stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException, StateMachineException {
		AbstractStateMachine theMachine = getStateMachine();
		long start = System.currentTimeMillis();
		long finishBy = timeout - 90000;

		int visitedNodes = 0;
		int iterations = 0;

		List<Move> moves = theMachine.getLegalMoves(getCurrentState(), getRole());
		Move selection = moves.get(0);
		if (moves.size() > 1) {
    		int[] moveTotalPoints = new int[moves.size()];
    		int[] moveTotalAttempts = new int[moves.size()];

    		// Perform depth charges for each candidate move, and keep track
    		// of the total score and total attempts accumulated for each move.
    		for (int i = 0; true; i = (i+1) % moves.size()) {
    		    if (System.currentTimeMillis() > finishBy)
    		        break;

    		    double theScore = performDepthChargeFromMove(getCurrentState(), moves.get(i));
    		    moveTotalPoints[i] += theScore;
    		    moveTotalAttempts[i] += 1;
    		    visitedNodes += this.depth[0] + 1;
    		    iterations++;

    		}

    		// Compute the expected score for each move.
    		double[] moveExpectedPoints = new double[moves.size()];
    		for (int i = 0; i < moves.size(); i++) {
    		    moveExpectedPoints[i] = (double)moveTotalPoints[i] / moveTotalAttempts[i];
    		}

    		// Find the move with the best expected score.
    		int bestMove = 0;
    		double bestMoveScore = moveExpectedPoints[0];
    		for (int i = 1; i < moves.size(); i++) {
    		    if (moveExpectedPoints[i] > bestMoveScore) {
    		        bestMoveScore = moveExpectedPoints[i];
    		        bestMove = i;
    		    }
    		}
    		selection = moves.get(bestMove);
		}

		long stop = System.currentTimeMillis();

		GamerLogger.log("Stats", "VISITED_NODES = " + visitedNodes);
		GamerLogger.log("Stats", "ITERATIONS = " + iterations);
		GamerLogger.log("Stats", "MOVE_SELECTION_TIME = " + (stop - start));

		notifyObservers(new GamerSelectedMoveEvent(theMachine.convertToExplicitMoves(moves), theMachine.convertToExplicitMove(selection), stop - start));
		return theMachine.convertToExplicitMove(selection);
	}

	private int[] depth = new int[1];
	double performDepthChargeFromMove(MachineState theState, Move myMove) {
		AbstractStateMachine theMachine = getStateMachine();
	    try {
            MachineState finalState = theMachine.performDepthCharge(theMachine.getRandomNextState(theState, getRole(), myMove), depth);
            return theMachine.getSafeGoalsAvgForAllRoles(finalState)[getStateMachine().getRoleIndices().get(getRole())];
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
	}

	/**
	 * Returns a state machine based on the Forward Interrupting PropNet.
	 */
	@Override
	public AbstractStateMachine getInitialStateMachine(){
		return new ExplicitStateMachine(new FwdInterrPropnetStateMachine(this.random));
	}

}
