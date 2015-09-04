/**
 *
 */
package org.ggp.base.util.statemachine.exceptions;

/**
 * This exception must be thrown whenever one of the basic state machine methods fails
 * to get an answer for the query (*), so that whoever is using the state machine can
 * take corrective actions, e.g. query another state machine or inform the game manager
 * that the player is not able to play anymore.
 *
 * (*) NOTE that this is different from saying that the state machine computed an answer
 * to the query but the answer is in the wrong format (for this case there are the
 * GoalDefinitionException, the MoveDefinitionException and the TransitionDefinitionException).
 *
 * Examples of situations when this exception should be thrown:
 * - when the state machine relies on another entity to get the answer of a query and the
 *   other entity fails (e.g. for the YapStateMachine when the update of the state on the
 *   YAP prolog side fails, no queries can be answered about that state).
 * - when the state machine relies on another entity to get the answer of a query and
 *   only waits for it for a limited amount of time (i.e. no answer before the timeout,
 *   so the exception is thrown when timeout is reached).
 *
 * @author C.Sironi
 *
 */
@SuppressWarnings("serial")
public class StateMachineException extends Exception {

	/**
	 *
	 */
	public StateMachineException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public StateMachineException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public StateMachineException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public StateMachineException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public StateMachineException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
