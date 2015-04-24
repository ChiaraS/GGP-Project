/**
 *
 */
package csironi.ggp.course.algorithms;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

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

	/**
	 *
	 */
	public SearchAlgorithm(boolean log, String logFileName, StateMachine stateMachine) {
		this.log = log;
		this.logFileName = logFileName;
		this.stateMachine = stateMachine;
	}


	public abstract List<Move> bestmove(MachineState state, Role role)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException;

	/**
	 * This methods writes to the log file the given string.
	 *
	 * @param toLog the string to be written on the file.
	 */
	// TODO Switch to the use of the GGP-base logger
	private void log(String toLog){

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
