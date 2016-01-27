package csironi.ggp.course.verifiers;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetCreationManager;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.internalPropnet.SeparateInternalPropnetCachedStateMachine;
import org.ggp.base.util.statemachine.implementation.internalPropnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

//TODO: merge all verifiers together in a single class since their code is similar.

/**
* This class verifies the consistency of the propnet state machine (the one that memorizes externally
* the state of the propnet components and separates the dynamic version of the propnet used during
* propnet optimization from the immutable version used at runtime to reason on the game) wrt the prover
* state machine.
*
* It is possible to specify the following combinations of main arguments:
*
* 1. [withCache]
* 2. [withCache] [keyOfGameToTest]
* 3. [withCache] [maximumPropnetInitializationTime] [maximumTestDuration]
* 4. [withCache] [maximumPropnetInitializationTime] [maximumTestDuration] [keyOfGameToTest]
*
* where:
* [withCache] = true if the propnet state machine must be provided with a cache for its results, false
* 				otherwise (DEFAULT: false).
* [maximumPropnetInitializationTime] = time in milliseconds that is available to build and initialize
* 									   the propnet (DEFAULT: 420000ms - 7mins).
* [maximumTestDuration] = duration of each test in millisecond (DEFAULT: 60000ms - 1min).
* [keyOfGameToTest] = key of the game to be tested (DEFAULT: null (i.e. all games)).
*
* If nothing or something inconsistent is specified for any of the parameters, the default value will
* be used.
*
* @author C.Sironi
*
*/
public class SeparatePropnetVerifier {

	public static void main(String[] args) throws InterruptedException{


		/*********************** Parse main arguments ****************************/


		boolean withCache = false;
		long initializationTime = 420000L;
		long testTime = 60000L;
		String gameToTest = null;

		if (args.length != 0 && args.length <= 4){

			withCache = Boolean.parseBoolean(args[0]);

			if(args.length == 4 || args.length == 2){
				gameToTest = args[args.length-1];
			}
			if(args.length == 4 || args.length == 3){
				try{
					initializationTime = Long.parseLong(args[1]);
				}catch(NumberFormatException nfe){
					System.out.println("Inconsistent propnet maximum building time specification! Using default value.");
					initializationTime = 420000L;
				}
				try{
					testTime = Long.parseLong(args[2]);
				}catch(NumberFormatException nfe){
					System.out.println("Inconsistent test duration specification! Using default value.");
					testTime = 60000L;
				}
			}
		}else if(args.length > 4){
			System.out.println("Inconsistent number of main arguments! Ignoring them.");
		}

		if(gameToTest == null){
			System.out.println("Running tests on ALL games with the following settings:");
		}else{
			System.out.println("Running tests on game " + gameToTest + " with the following settings:");
		}
		if(withCache){
			System.out.println("With cache: yes.");
		}else{
			System.out.println("With cache: no.");
		}
		System.out.println("Propnet building time: " + initializationTime + "ms");
		System.out.println("Running time for each test: " + testTime + "ms");
		System.out.println();


		/*********************** Perform all the tests ****************************/


		ProverStateMachine theReference;
		SeparateInternalPropnetStateMachine thePropnetMachine;
		StateMachine theSubject;

	    GamerLogger.setSpilloverLogfile("SeparatePropnetVerifierTable.csv");
	    GamerLogger.log(FORMAT.CSV_FORMAT, "SeparatePropnetVerifierTable", "Game key;PN initialization time (ms);PN construction time (ms);SM initialization time;Rounds;Completed rounds;Test duration (ms);Subject exception;Other exceptions;Pass;");

	    GameRepository theRepository = GameRepository.getDefaultRepository();
	    for(String gameKey : theRepository.getGameKeys()) {
	        if(gameKey.contains("laikLee")) continue;

	        // TODO: change code so that if there is only one game to test we won't run through the whole sequence of keys.
	        if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

	        System.out.println("Detected activation in game " + gameKey + ".");

	        Match fakeMatch = new Match(gameKey + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

	        GamerLogger.startFileLogging(fakeMatch, "SeparatePropnetVerifier");

	        GamerLogger.log("Verifier", "Testing on game " + gameKey);

	        List<Gdl> description = theRepository.getGame(gameKey).getRules();

	        theReference = new ProverStateMachine();

	        // Create the executor service that will run the propnet manager that creates the propnet
	        ExecutorService executor = Executors.newSingleThreadExecutor();

	        // Create the propnet creation manager
	        SeparateInternalPropnetCreationManager manager = new SeparateInternalPropnetCreationManager(description, System.currentTimeMillis() + initializationTime);

	        // Start the manager
	  	  	executor.execute(manager);

	  	  	// Shutdown executor to tell it not to accept any more task to execute.
			// Note that this doesn't interrupt previously started tasks.
			executor.shutdown();

			// Tell the executor to wait until the currently running task has completed execution or the timeout has elapsed.
			try{
				executor.awaitTermination(initializationTime, TimeUnit.MILLISECONDS);
			}catch(InterruptedException e){ // The thread running the verifier has been interrupted => stop the test
				executor.shutdownNow(); // Interrupt everything
				GamerLogger.logError("Verifier", "State machine verification interrupted. Test on game "+ gameKey +" won't be completed.");
				GamerLogger.logStackTrace("Verifier", e);
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
					GamerLogger.logError("Verifier", "State machine verification interrupted. Test on game "+ gameKey +" won't be completed.");
					GamerLogger.logStackTrace("Verifier", e);
					GamerLogger.stopFileLogging();
					Thread.currentThread().interrupt();
					return;
				}
			}

			// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.

			ImmutablePropNet propnet = manager.getImmutablePropnet();
			ImmutableSeparatePropnetState propnetState = manager.getInitialPropnetState();

			// Create the state machine giving it the propnet and the propnet state.
			// NOTE that if any of the two is null, it means that the propnet creation/initialization went wrong
			// and this will be detected by the state machine during initialization.
		    thePropnetMachine = new SeparateInternalPropnetStateMachine(propnet, propnetState);

		    if(withCache){
		    	theSubject = new SeparateInternalPropnetCachedStateMachine(thePropnetMachine);
		    }else{
		    	theSubject = thePropnetMachine;
		    }

		    theReference.initialize(description, Long.MAX_VALUE);

		    long smInitTime = -1L;
	        int rounds = -1;
	        int completedRounds = -1;
	        long testDuration = -1L;
	        boolean pass = false;
	        String exception = "-";
	        int otherExceptions = -1;

	        long initStart = System.currentTimeMillis();

	        // Try to initialize the propnet state machine.
	        // If initialization fails, skip the test.
	        try{
	        	theSubject.initialize(description, Long.MAX_VALUE);
		        smInitTime = System.currentTimeMillis() - initStart;
		        System.out.println("Propnet creation succeeded. Checking consistency.");
		        long testStart = System.currentTimeMillis();

		        /***************************************/
			    //System.gc();
			    /***************************************/

		        pass = ExtendedStateMachineVerifier.checkMachineConsistency(theReference, theSubject, testTime);
		        testDuration = System.currentTimeMillis() - testStart;
		        rounds = ExtendedStateMachineVerifier.lastRounds;
		        completedRounds = ExtendedStateMachineVerifier.completedRounds;
		        exception = ExtendedStateMachineVerifier.exception;
		        otherExceptions = ExtendedStateMachineVerifier.otherExceptions;
	        }catch(StateMachineInitializationException e){
	      	  	smInitTime = System.currentTimeMillis() - initStart;
	        	GamerLogger.logError("Verifier", "State machine " + theSubject.getName() + " initialization failed, impossible to test this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	        	GamerLogger.logStackTrace("Verifier", e);
	        	System.out.println("Skipping test on game " + gameKey + ". State machine initialization failed, no propnet available.");
	        }

	        GamerLogger.log(FORMAT.PLAIN_FORMAT, "Verifier", "");

	        GamerLogger.stopFileLogging();

	        GamerLogger.log(FORMAT.CSV_FORMAT, "SeparatePropnetVerifierTable", gameKey + ";" + manager.getTotalInitTime() + ";" + manager.getPropnetConstructionTime() + ";" + smInitTime +  ";"  + rounds +  ";"  + completedRounds + ";"  + testDuration + ";"  + exception + ";"  + otherExceptions + ";" + pass + ";");

	        /***************************************/
	        //System.gc();
	        //GdlPool.drainPool();
	        /***************************************/
	    }
	}
}
