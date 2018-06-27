/**
 *
 */
package csironi.ggp.course.gamers;

import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.statemachine.sample.SampleGamer;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

/**
 * Random gamer realized for test.
 * This gamer chooses at each step a random legal action.
 * This gamer checks the goals of a state during every step of the game.
 *
 * @author C.Sironi
 *
 */
public class MCRandomActions extends SampleGamer {


	protected int callsNumber;

	protected int failedCalls;

	protected int numberOfIterations;



	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		GamerLogger.startFileLogging(getMatch(), getStateMachine().convertToExplicitRole(getRole()).getName().getValue());
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
		long finishBy = timeout - 30000;

		this.callsNumber = 0;
		this.failedCalls = 0;
		this.numberOfIterations = 0;

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

    			this.numberOfIterations++;

    			double theScore = performPlayout(getCurrentState(), moves.get(i));
    		    moveTotalPoints[i] += theScore;
    		    moveTotalAttempts[i] += 1;
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

		if(moves.size() > 1){
			GamerLogger.log("Times", " ");
			GamerLogger.log("Times", "Total duration: "  + (stop-start) + " ms");
			GamerLogger.log("Times", "Number of iterations: "  + this.numberOfIterations);
			GamerLogger.log("Times", "Average iteration duration: "  + ((double)(stop-start))/(double)this.numberOfIterations + " ms");
			GamerLogger.log("Times", "# total calls to getGoals(): " + this.callsNumber);
			GamerLogger.log("Times", "# failed calls: " + this.failedCalls);
			GamerLogger.log("Times", "Average calls to getGoals() per iteration: "  + (double)this.callsNumber/(double)this.numberOfIterations);
		}



		notifyObservers(new GamerSelectedMoveEvent(theMachine.convertToExplicitMoves(moves), theMachine.convertToExplicitMove(selection), stop - start));
		return theMachine.convertToExplicitMove(selection);

	}

	private double performPlayout(MachineState currentState, Move move) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException, StateMachineException{

		AbstractStateMachine theMachine = getStateMachine();
		MachineState nextState = theMachine.getRandomNextState(currentState, getRole(), move);

		double[] goals = null;

		while(!theMachine.isTerminal(nextState)){
			nextState = theMachine.getRandomNextState(nextState);

			this.callsNumber++;
			goals = theMachine.getSafeGoalsAvgForAllRoles(nextState);
			//System.out.println(goals);
		}

		goals = theMachine.getSafeGoalsAvgForAllRoles(nextState);
		this.callsNumber++;

		return goals[theMachine.getRoleIndices().get(getRole())];
	}

	@Override
	public void stateMachineStop() {

		GamerLogger.stopFileLogging();

	}



}
