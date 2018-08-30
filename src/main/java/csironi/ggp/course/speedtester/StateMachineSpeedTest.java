package csironi.ggp.course.speedtester;

import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.propnet.ExternalPropnetStateMachine;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;

/**
 * This class computes the nodes visits and the Monte Carlo iterations that a state machine
 * can perform in the given amount of time.
 *
 * @author C.Sironi
 *
 */
public class StateMachineSpeedTest {

	public static int visitedNodes;
	public static int succeededIterations;
	public static int failedIterations;
	public static long exactTimeSpent;


	/**
	 * This method computes the nodes visits and the Monte Carlo iterations that a state machine
	 * can perform in the given amount of time.
	 *
	 * @param theMachine the machine to be tested.
	 * @param timeToSpend the time for which to perform the Monte Carlo simulations.
	 */
	public static void testSpeed(StateMachine theMachine, long timeToSpend){

		visitedNodes = 0;
		succeededIterations = 0;
		failedIterations = 0;
		exactTimeSpent = 0L;

		int[] lastIterationVisitedNodes = new int[1];

		ExplicitMachineState initialState = theMachine.getExplicitInitialState();

		long startTime = System.currentTimeMillis();

		while(System.currentTimeMillis() < startTime + timeToSpend){

			try {
				theMachine.performDepthCharge(initialState, lastIterationVisitedNodes);
				succeededIterations++;
				visitedNodes += lastIterationVisitedNodes[0];
			}catch (TransitionDefinitionException | MoveDefinitionException | StateMachineException e) {
				GamerLogger.logError("SMSpeedTest", "Exception during iteration!");
				GamerLogger.logStackTrace("SMSpeedTest", e);
				failedIterations++;
			}catch (Exception e) { // Keep all other exception separate from the typical exceptions of the state machine (even if now they are all dealt with in the same way)
				GamerLogger.logError("SMSpeedTest", "Exception during iteration!");
				GamerLogger.logStackTrace("SMSpeedTest", e);
				failedIterations++;
			}catch (Error e) {
				GamerLogger.logError("SMSpeedTest", "Error during iteration!");
				GamerLogger.logStackTrace("SMSpeedTest", e);
				failedIterations++;
			}
		}

		exactTimeSpent = System.currentTimeMillis() - startTime;

	}

	/**
	 * This method computes the nodes visits and the Monte Carlo iterations that a propet state
	 * machine with external state can perform in the given amount of time.
	 * This method doesn't use the standard preformDepthCharge method of all the state machines,
	 * but uses the specific method of the ExternalPropnetStateMachine, that doesn't translate
	 * to and from standard State, Move and Role objects.
	 *
	 * @param theMachine the ExternalPropnetStateMachine to be tested.
	 * @param timeToSpend the time during which to perform the Monte Carlo simulations.
	 */
	public static void testSpeedAdHoc(ExternalPropnetStateMachine theMachine, long timeToSpend){

		visitedNodes = 0;
		succeededIterations = 0;
		failedIterations = 0;
		exactTimeSpent = 0L;

		int[] lastIterationVisitedNodes = new int[1];

		CompactMachineState initialState = theMachine.getPropnetInitialState();

		long startTime = System.currentTimeMillis();

		while(System.currentTimeMillis() < startTime + timeToSpend){

			try {
				theMachine.performDepthCharge(initialState, lastIterationVisitedNodes);
				succeededIterations++;
				visitedNodes += lastIterationVisitedNodes[0];
			}catch (TransitionDefinitionException | MoveDefinitionException | StateMachineException e) {
				GamerLogger.logError("SMSpeedTest", "Exception during iteration!");
				GamerLogger.logStackTrace("SMSpeedTest", e);
				failedIterations++;
			}catch (Exception e) { // Keep all other exception separate from the typical exceptions of the state machine (even if now they are all dealt with in the same way)
				GamerLogger.logError("SMSpeedTest", "Exception during iteration!");
				GamerLogger.logStackTrace("SMSpeedTest", e);
				failedIterations++;
			}catch (Error e) {
				GamerLogger.logError("SMSpeedTest", "Error during iteration!");
				GamerLogger.logStackTrace("SMSpeedTest", e);
				failedIterations++;
			}
		}

		exactTimeSpent = System.currentTimeMillis() - startTime;

	}

	/**
	 * This method computes the nodes visits and the Monte Carlo iterations that a propnet state
	 * machine with external state and separate propnet can perform in the given amount of time.
	 * This method doesn't use the standard preformDepthCharge method of all the state machines,
	 * but uses the specific method of the ExternalPropnetStateMachine, that doesn't translate
	 * to and from standard State, Move and Role objects.
	 *
	 * @param theMachine the ExternalPropnetStateMachine to be tested.
	 * @param timeToSpend the time during which to perform the Monte Carlo simulations.
	 */
	public static void testSeparatePNSpeed(InternalPropnetStateMachine theMachine, long timeToSpend){

		visitedNodes = 0;
		succeededIterations = 0;
		failedIterations = 0;
		exactTimeSpent = 0L;

		int[] lastIterationVisitedNodes = new int[1];

		CompactMachineState initialState = theMachine.getCompactInitialState();

		long startTime = System.currentTimeMillis();

		while(System.currentTimeMillis() < startTime + timeToSpend){

			try {
				theMachine.performDepthCharge(initialState, lastIterationVisitedNodes);
				succeededIterations++;
				visitedNodes += lastIterationVisitedNodes[0];
			}catch (TransitionDefinitionException | MoveDefinitionException | StateMachineException e) {
				GamerLogger.logError("SMSpeedTest", "Exception during iteration!");
				GamerLogger.logStackTrace("SMSpeedTest", e);
				failedIterations++;
			}catch (Exception e) { // Keep all other exceptions separate from the typical exceptions of the state machine (even if now they are all dealt with in the same way)
				GamerLogger.logError("SMSpeedTest", "Exception during iteration!");
				GamerLogger.logStackTrace("SMSpeedTest", e);
				failedIterations++;
			}catch (Error e) {
				GamerLogger.logError("SMSpeedTest", "Error during iteration!");
				GamerLogger.logStackTrace("SMSpeedTest", e);
				failedIterations++;
			}
		}

		exactTimeSpent = System.currentTimeMillis() - startTime;

	}

}
