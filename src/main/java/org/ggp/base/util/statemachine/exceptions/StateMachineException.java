/**
 *
 */
package org.ggp.base.util.statemachine.exceptions;

/**
 * This exception must be thrown whenever one of the basic state machine methods fails,
 * so that whoever is using the state machine can take corrective actions, e.g. query
 * another state machine or inform the game manager that the player is not able to play
 * anymore.
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
