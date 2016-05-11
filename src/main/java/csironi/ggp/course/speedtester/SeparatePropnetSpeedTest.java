package csironi.ggp.course.speedtester;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetManager;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.SeparateInternalPropnetCachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;

//TODO: merge all speed tests together in a single class since their code is similar.

/**
* This class checks the speed (nodes/second, iterations/second) of the propnet state machine that
* memorizes externally the state of the propnet components and separates the dynamic version of the
* propnet used during propnet optimization from the immutable version used at runtime to reason on
* the game).
* This is done by performing Monte Carlo simulations from the initial state of the given game for the
* specified amount of time.
*
* It is possible to specify the following combinations of main arguments:
*
* 1. [withTranslation] [withCache]
* 2. [withTranslation] [withCache] [keyOfGameToTest]
* 3. [withTranslation] [withCache] [givenInitTime] [maximumTestDuration]
* 4. [withTranslation] [withCache] [givenInitTime] [maximumTestDuration] [keyOfGameToTest]
*
* where:
* [withTranslation] = true if the speed test must be performed translating every time from the canonical
* 					  representation of State, Move and Role to their internal representation in the
* 					  propnet state machine and vice versa; false otherwise (DEFAULT: false).
* [withCache] = true if the propnet state machine must be provided with a cache for its results, false
* 				otherwise (DEFAULT: false).
* [givenInitTime] = maximum time in milliseconds that should be spent to initialize the
* 								 propnet state machine (DEFAULT: 420000ms - 7mins).
* [maximumTestDuration] = duration of each test in millisecond (DEFAULT: 60000ms - 1min).
* [keyOfGameToTest] = key of the game to be tested (DEFAULT: null (i.e. all games)).
*
* If nothing or something inconsistent is specified for any of the parameters, the default value will
* be used.
*
* @author C.Sironi
*
*/
public class SeparatePropnetSpeedTest {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	public static void main(String[] args) {

		/*********************** Parse main arguments ****************************/

		boolean withTranslation = false;
		boolean withCache = false;
		long givenInitTime = 420000L;
		long testTime = 60000L;
		String gameToTest = null;

		if(args.length != 0){ // At least one argument is specified and the first argument is either true or false

			if (args.length <= 5 && args.length > 1){

				withTranslation = Boolean.parseBoolean(args[0]);
				withCache = Boolean.parseBoolean(args[1]);

				if(args.length == 3 || args.length == 5){
					gameToTest = args[args.length-1];
				}
				if(args.length == 5 || args.length == 4){
					try{
						givenInitTime = Long.parseLong(args[2]);
					}catch(NumberFormatException nfe){
						System.out.println("Inconsistent maximum initialization time specification! Using default value.");
						givenInitTime = 420000L;
					}
					try{
						testTime = Long.parseLong(args[3]);
					}catch(NumberFormatException nfe){
						System.out.println("Inconsistent test duration specification! Using default value.");
						testTime = 60000L;
					}
				}
			}else{ // Too many arguments, using default
				System.out.println("Inconsistent number of main arguments! Ignoring them.");
			}

		} // else use default values

		if(withTranslation){
			System.out.print("Testing speed of the propnet state machine with translation");
		}else{
			System.out.print("Testing speed of the propnet state machine with no translation");
		}

		if(withCache){
			System.out.println(" and cache.");
		}else{
			System.out.println(" and no cache.");
		}

		if(gameToTest == null){
			System.out.println("Running tests on ALL games with the following time settings:");
		}else{
			System.out.println("Running tests on game " + gameToTest + " with the following time settings:");
		}
		System.out.println("Propnet building time: " + givenInitTime + "ms");
		System.out.println("Running time for each test: " + testTime + "ms");
		System.out.println();


		/*********************** Perform all the tests ****************************/

		StateMachine theSubject;
		InternalPropnetStateMachine thePropnetMachine;

		String type = "SeparatePN";
		if(withTranslation){
			type = "Translating" + type;
		}
		if(withCache){
			type = "Cached" + type;
		}


		String mainLogFolder = System.currentTimeMillis() + ".SpeedTester";
    	ThreadContext.put("LOG_FOLDER", mainLogFolder);
		GamerLogger.startFileLogging();
		//GamerLogger.setSpilloverLogfile(type + "SpeedTestTable.csv");
	    GamerLogger.log(FORMAT.CSV_FORMAT, type + "SpeedTestTable", "Game key;PN Initialization Time (ms);PN Construction Time (ms);SM initialization time;Test Duration (ms);Succeeded Iterations;Failed Iterations;Visited Nodes;Iterations/second;Nodes/second;");

	    GameRepository theRepository = GameRepository.getDefaultRepository();
	    for(String gameKey : theRepository.getGameKeys()) {
	        if(gameKey.contains("laikLee")) continue;

	        // TODO: change code so that if there is only one game to test we won't run through the whole sequence of keys.
	        if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

	        System.out.println("Detected activation in game " + gameKey + ".");

	        Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

	        ThreadContext.put("LOG_FOLDER", mainLogFolder + "/logs/" + fakeMatch.getMatchId());

	       // GamerLogger.startFileLogging(fakeMatch, type + "SpeedTester");

	        GamerLogger.log("SMSpeedTest", "Testing on game " + gameKey);

	        List<Gdl> description = theRepository.getGame(gameKey).getRules();

	        // Create the executor service that will run the propnet manager that creates the propnet
	        ExecutorService executor = Executors.newSingleThreadExecutor();

	        // Create the propnet creation manager
	        SeparateInternalPropnetManager manager = new SeparateInternalPropnetManager(description, System.currentTimeMillis() + givenInitTime);

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
				GamerLogger.logError("SMSpeedTest", "State machine speed test interrupted. Test on game "+ gameKey +" won't be completed.");
				GamerLogger.logStackTrace("SMSpeedTest", e);
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
					GamerLogger.logError("SMSpeedTest", "State mahcine verification interrupted. Test on game "+ gameKey +" won't be completed.");
					GamerLogger.logStackTrace("SMSpeedTest", e);
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

		    // For now the cache can be used only for the state machine that performs translation
		    if(withCache){
		    	theSubject = new SeparateInternalPropnetCachedStateMachine((SeparateInternalPropnetStateMachine) thePropnetMachine);
	        }else{
	        	theSubject = thePropnetMachine;
	        }

	        long initializationTime;
	        long testDuration = -1L;
	        int succeededIterations = -1;
	        int failedIterations = -1;
	        int visitedNodes = -1;
	        double iterationsPerSecond = -1;
	        double nodesPerSecond = -1;

	        // Try to initialize the propnet state machine.
	        // If initialization fails, skip the test.
	    	long initStart = System.currentTimeMillis();
	        try{
	        	theSubject.initialize(description, initStart + givenInitTime);

	        	initializationTime = System.currentTimeMillis() - initStart;

	        	System.out.println("Propnet creation succeeded. Checking speed.");


		        /***************************************/
		        //System.gc();
		        /***************************************/

	        	if(withTranslation){
	        		StateMachineSpeedTest.testSpeed(theSubject, testTime);
	        	}else{
	        		StateMachineSpeedTest.testSeparatePNSpeed((InternalPropnetStateMachine) theSubject, testTime);
	        	}

	        	testDuration = StateMachineSpeedTest.exactTimeSpent;
	        	succeededIterations = StateMachineSpeedTest.succeededIterations;
	     		failedIterations = StateMachineSpeedTest.failedIterations;
	            visitedNodes = StateMachineSpeedTest.visitedNodes;
	            iterationsPerSecond = ((double) succeededIterations * 1000)/((double) testDuration);
	            nodesPerSecond = ((double) visitedNodes * 1000)/((double) testDuration);
	        }catch(StateMachineInitializationException e){
	        	initializationTime = System.currentTimeMillis() - initStart;
	        	GamerLogger.logError("SMSpeedTest", "State machine " + theSubject.getName() + " initialization failed, impossible to test this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	        	GamerLogger.logStackTrace("SMSpeedTest", e);
	        	System.out.println("Skipping test on game " + gameKey + ". No propnet available.");
	        }

	        GamerLogger.log(FORMAT.PLAIN_FORMAT, "SMSpeedTest", "");

	        ThreadContext.put("LOG_FOLDER", mainLogFolder);

	        GamerLogger.log(FORMAT.CSV_FORMAT, type + "SpeedTestTable", gameKey + ";" + manager.getTotalInitTime() + ";" + manager.getPropnetConstructionTime() + ";" + initializationTime + ";" + testDuration + ";" + succeededIterations + ";" + failedIterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";");

	        /***************************************/
	        //System.gc();
	        //GdlPool.drainPool();
	        /***************************************/

	    }
	}
}
