package csironi.ggp.course;

import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class StateMachineSpeedTest {

	public static int visitedNodes;
	public static int succeededIterations;
	public static int failedIterations;
	public static long exactTimeSpent;


	public static void testSpeed(StateMachine theMachine, long timeToSpend){

		visitedNodes = 0;
		succeededIterations = 0;
		failedIterations = 0;
		exactTimeSpent = 0L;

		int[] lastIterationVisitedNodes = new int[1];

		MachineState initialState = theMachine.getInitialState();

		long startTime = System.currentTimeMillis();

		while(System.currentTimeMillis() < startTime + timeToSpend){

			try {
				theMachine.performDepthCharge(initialState, lastIterationVisitedNodes);
				succeededIterations++;
				visitedNodes += lastIterationVisitedNodes[0];
			} catch (TransitionDefinitionException | MoveDefinitionException e) {
				GamerLogger.logError("SMSpeedTest", "Exception during iteration!");
				GamerLogger.logStackTrace("SMSpeedTest", e);
				failedIterations++;
			}
		}

		exactTimeSpent = System.currentTimeMillis() - startTime;

	}

}
