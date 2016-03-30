package org.ggp.base.util.propnet.creationManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ggp.base.util.logging.GamerLogger;

public class PropNetManagerRunner {

	/**
	 * This method takes care of running the PropNet manager within the given period of time.
	 * (Running the manager means letting it create and optimize the PropNet and create its initial state).
	 *
	 * @param description
	 * @param givenInitTime
	 * @return
	 */
	public static void runPropNetManager(SeparateInternalPropnetManager manager, long givenInitTime){

        // Create the executor service that will run the PropNet manager that creates the PropNet
        ExecutorService executor = Executors.newSingleThreadExecutor();

  	  	// Start the manager
  	  	executor.execute(manager);

  	  	// Shutdown executor to tell it not to accept any more task to execute.
		// Note that this doesn't interrupt previously started tasks.
		executor.shutdown();

		// Tell the executor to wait until the currently running task has completed execution or the timeout has elapsed.
		try{
			executor.awaitTermination(givenInitTime, TimeUnit.MILLISECONDS);
		}catch(InterruptedException e){ // The thread running the speed test has been interrupted => stop the test
			executor.shutdownNow(); // Interrupt everything
			GamerLogger.logError("PropnetManager", "Propnet manager execution interrupted. Propnet creation and initialization won't be completed.");
			GamerLogger.logStackTrace("PropnetManager", e);
			Thread.currentThread().interrupt();
			return;
		}

		// Here the available time has elapsed, so we must interrupt the thread if it is still running.
		executor.shutdownNow();

		// Wait for the thread to actually terminate
		while(!executor.isTerminated()){

			// If the thread didn't terminate, wait for a minute and then check again
			try{
				executor.awaitTermination(1, TimeUnit.MINUTES);
			}catch(InterruptedException e) {
				// If this exception is thrown it means the thread that is executing the verification
				// of the state machine has been interrupted. If we do nothing this state machine could be stuck in the
				// while loop anyway until all tasks in the executor have terminated, thus we break out of the loop and return.
				// What happens to the still running tasks in the executor? Who will make sure they terminate?
				GamerLogger.logError("PropnetManager", "Propnet manager execution interrupted. Propnet creation and initialization won't be completed.");
				GamerLogger.logStackTrace("PropnetManager", e);
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

}
