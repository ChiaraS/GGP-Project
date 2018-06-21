/**
 *
 */
package csironi.ggp.course.gamers.yapGamer;

import java.util.List;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.hybrid.BackedYapStateMachine;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;


/**
 * SampleMonteCarloGamer is a simple state-machine-based Gamer. It will use a
 * pure Monte Carlo approach towards picking moves, doing simulations and then
 * choosing the move that has the highest expected score. It should be slightly
 * more challenging than the RandomGamer, while still playing reasonably fast.
 *
 * However, right now it isn't challenging at all. It's extremely mediocre, and
 * doesn't even block obvious one-move wins. This is partially due to the speed
 * of the default state machine (which is slow) and mostly due to the algorithm
 * assuming that the opponent plays completely randomly, which is inaccurate.
 *
 * @author Sam Schreiber
 */
public final class SampleMontecarloYapGamer extends SampleYapGamer{

	/**
	 * Employs a simple sample "Monte Carlo" algorithm.
	 * @throws StateMachineException
	 */
	@Override
	public ExplicitMove stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException, StateMachineException
	{
		//StateMachine theMachine = getStateMachine();
		BackedYapStateMachine theMachine = getYapStateMachine();
		long start = System.currentTimeMillis();
		long finishBy = timeout - 5000;

		List<ExplicitMove> moves = theMachine.getExplicitLegalMoves(getCurrentState(), getRole());
		ExplicitMove selection = moves.get(0);
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

		notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
		return selection;
	}

	private int[] depth = new int[1];

	//////////
	private ExplicitMachineState randomNextState;
	//////////

	double performDepthChargeFromMove(ExplicitMachineState theState, ExplicitMove myMove) {
		//StateMachine theMachine = getStateMachine();
		BackedYapStateMachine theMachine = getYapStateMachine();
		/*
		try {
			MachineState finalState = theMachine.performDepthCharge(theMachine.getRandomNextState(theState, getRole(), myMove), depth);
			return theMachine.getGoal(finalState, getRole());
		} catch (Exception e) {
			e.printStackTrace();
			//////////
			System.out.println();
			System.out.println("theState :"+theState);
			System.out.println("myMove :"+myMove);
			System.out.println();
			//////////
			return 0;
		}
		 */
		try{
			randomNextState = theMachine.getRandomNextState(theState, getRole(), myMove);
			if(randomNextState.equals(null)) return 0;
			else{
				ExplicitMachineState finalState = theMachine.performDepthCharge(randomNextState, depth);
				if(finalState.equals(null)) return 0;
				else {
					return theMachine.getGoal(finalState, getRole());
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			return 0;
		}
	}
}
