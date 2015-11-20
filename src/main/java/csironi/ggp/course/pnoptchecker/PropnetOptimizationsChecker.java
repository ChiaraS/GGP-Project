package csironi.ggp.course.pnoptchecker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;

/**
* This class check for each game how much each propnet optimization shrinks the propnet and
* how much time it requires.
* This class logs for each game the number of each component type in the propnet after each
* of the optimizations of the propnet (plus after creation and after final initialization).
*
* It is possible to specify the following combinations of main arguments:
*
* 1. [keyOfGameToTest]
* 2. [maximumPropnetInitializationTime] [keyOfGameToTest]
*
* where:
* [keyOfGameToTest] = key of the game to be tested (DEFAULT: null (i.e. all games)).
* [maximumPropnetInitializationTime] = time in milliseconds that is available to build,
* 									   optimize and initialize the propnet (DEFAULT:
* 									  900000ms - 15mins).*
*
* If nothing or something inconsistent is specified for any of the parameters, the default value will
* be used.
*
* @author C.Sironi
*
*/
public class PropnetOptimizationsChecker {

	public static void main(String[] args) throws InterruptedException{


		/*********************** Parse main arguments ****************************/


		long initializationTime = 900000L;
		String gameToTest = null;

		if (args.length != 0 && args.length <= 2){
			gameToTest = args[args.length-1];

			if(args.length == 2){
				try{
					initializationTime = Long.parseLong(args[0]);
				}catch(NumberFormatException nfe){
					System.out.println("Inconsistent propnet maximum initialization time specification! Using default value.");
					initializationTime = 900000L;
				}
			}
		}else if(args.length > 2){
			System.out.println("Inconsistent number of main arguments! Ignoring them.");
		}

		if(gameToTest == null){
			System.out.println("Running propnet optimization check on ALL games with the following time setting:");
		}else{
			System.out.println("Running propnet optimization check on game " + gameToTest + " with the following time setting:");
		}
		System.out.println("Propnet building time: " + initializationTime + "ms");
		System.out.println();


		/*********************** Perform all the tests ****************************/


	    GamerLogger.setSpilloverLogfile("PNOptCheckTable.csv");
	    GamerLogger.log(FORMAT.CSV_FORMAT, "PNOptCheckTable", "Game key;Task;Time(ms);#Components;#Links;#Constants;#Ands;#Ors;#Nots;#Transitions;#Bases;#Legals;#Inputs;#Inits;#Goals;#Terminals;#Others;");

	    GameRepository theRepository = GameRepository.getDefaultRepository();
	    for(String gameKey : theRepository.getGameKeys()) {
	        if(gameKey.contains("laikLee")) continue;

	        // TODO: change code so that if there is only one game to test we won't run through the whole sequence of keys.
	        if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

	        System.out.println("Detected activation in game " + gameKey + ".");

	        Match fakeMatch = new Match(gameKey + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

	        GamerLogger.startFileLogging(fakeMatch, "PNOptCheck");

	        GamerLogger.log("PNOptCheck", "Testing on game " + gameKey);

	        List<Gdl> description = theRepository.getGame(gameKey).getRules();

	        // Create the executor service that will run the propnet manager that creates the propnet
	        ExecutorService executor = Executors.newSingleThreadExecutor();

	        // Create the propnet creation manager
	        LoggingSeparatePropnetCreationManager manager = new LoggingSeparatePropnetCreationManager(description, gameKey);

	        // Start the manager
	  	  	executor.execute(manager);

	  	  	// Shutdown executor to tell it not to accept any more task to execute.
			// Note that this doesn't interrupt previously started tasks.
			executor.shutdown();

			// Tell the executor to wait until the currently running task has completed execution or the timeout has elapsed.
			try{
				executor.awaitTermination(initializationTime, TimeUnit.MILLISECONDS);
			}catch(InterruptedException e){ // The thread running the checker has been interrupted => stop the test
				executor.shutdownNow(); // Interrupt everything
				GamerLogger.logError("PNOptCheck", "Propnet optimization check interrupted. Check on game "+ gameKey +" won't be completed.");
				GamerLogger.logStackTrace("PNOptCheck", e);
				GamerLogger.stopFileLogging();
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
					GamerLogger.logError("PNOptCheck", "Propnet optimization check interrupted. Check on game "+ gameKey +" won't be completed.");
					GamerLogger.logStackTrace("PNOptCheck", e);
					GamerLogger.stopFileLogging();
					Thread.currentThread().interrupt();
					return;
				}
			}

			System.out.println("Propnet creation and optimization checked!");

			// If we are here it means that the manager stopped running. We must get all the log messages it created
			// and actually log them on the file.

	        GamerLogger.log(FORMAT.PLAIN_FORMAT, "PNOptCheck", "");

	        GamerLogger.stopFileLogging();

	        List<String> logs = manager.getLogs();

	        for(String s : logs){
	        	GamerLogger.log(FORMAT.CSV_FORMAT, "PNOptCheckTable", s);
	        }
	    }
	}

}
