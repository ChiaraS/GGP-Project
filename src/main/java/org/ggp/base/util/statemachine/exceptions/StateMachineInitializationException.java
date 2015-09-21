/**
 *
 */
package org.ggp.base.util.statemachine.exceptions;

/**
 * @author C.Sironi
 *
 */
@SuppressWarnings("serial")
public class StateMachineInitializationException extends Exception {

	/**
	 *
	 */
	public StateMachineInitializationException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public StateMachineInitializationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public StateMachineInitializationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public StateMachineInitializationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public StateMachineInitializationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
