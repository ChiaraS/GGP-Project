/**
 *
 */
package csironi.ggp.course.algorithms;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.ggp.base.util.statemachine.StateMachine;

/**
 * @author C.Sironi
 *
 */
public abstract class SearchAlgorithm {

	/**
	 * True if logs must be written on file, false otherwise.
	 */
	private boolean log;

	/**
	 * Name (path) for the log file.
	 */
	private String logFileName;

	protected StateMachine stateMachine;

	protected boolean timedOut;

	/**
	 *
	 */
	public SearchAlgorithm(boolean log, String logFileName, StateMachine stateMachine) {
		this.log = log;
		this.logFileName = logFileName;
		this.stateMachine = stateMachine;
		this.timedOut = false;
	}

	/**
	 * This methods writes to the log file the given string.
	 *
	 * @param toLog the string to be written on the file.
	 */
	// TODO Switch to the use of the GGP-base logger
	protected void log(String toLog){

		if(log){
			try{
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(this.logFileName, true)));

				out.println(toLog);

				out.close();

			}catch(IOException e){
				System.out.println("Exception when writing log to file. Log string not written. Continuing game playing.");
			}
		}
	}

}
