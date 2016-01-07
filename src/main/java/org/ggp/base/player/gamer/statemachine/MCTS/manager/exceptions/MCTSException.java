/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.manager.exceptions;

/**
 * @author C.Sironi
 *
 */
@SuppressWarnings("serial")
public class MCTSException extends Exception {

	/**
	 *
	 */
	public MCTSException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public MCTSException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public MCTSException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MCTSException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public MCTSException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
