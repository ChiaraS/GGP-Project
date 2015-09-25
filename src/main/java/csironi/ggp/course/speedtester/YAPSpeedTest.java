/**
 *
 */
package csironi.ggp.course.speedtester;

import java.util.List;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.hybrid.BackedYapStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.implementation.yapProlog.YapStateMachine;

//TODO: merge all speed tests together in a single class since their code is similar.

/**
 * This class checks the speed (nodes/second, iterations/second) of the Yap state machine.
 * This is done by performing Monte Carlo simulations from the initial state of the given game for the
 * specified amount of time.
 *
 * It is possible to specify the following combinations of main arguments:
 *
 * where:
 * 1. [withCache] [backed]
 * 2. [withCache] [backed] [keyOfGameToTest]
 * 3. [withCache] [backed] [queryWaitingTime] [maximumTestDuration]
 * 4. [withCache] [backed] [queryWaitingTime] [maximumTestDuration] [keyOfGameToTest]
 *
 * where:
 * [withCache] = true if the Yap state machine must be provided with a cache for its results, false
 * 				 otherwise (DEFAULT: false).
 * [backed] = true if the Yap state machine must be backed by the GGP Base prover state machine
 * 			  when the Yap prover doesn't answer to a query in time, false otherwise (DEFAULT:
 * 			  false).
 * [queryWaitingTime] = maximum time in milliseconds that the Yap state machine must wait for
 * 						getting the result of a query from Yap prolog (DEFAULT: 500ms).
 * 						ATTENTION: it's better to never run the tests with this parameter set to
 * 						0ms, as this will cause the state machine to wait indefinitely and thus
 * 						the program will get stuck if Yap prolog doesn't answer.
 * [maximumTestDuration] = duration of each test in millisecond (DEFAULT: 60000ms - 1min).
 * [keyOfGameToTest] = key of the game to be tested (DEFAULT: null (i.e. all games)).
 *
 * If nothing or something inconsistent is specified for any of the parameters, the default value
 * will be used.
 *
 * @author C.Sironi
 *
 */
public class YAPSpeedTest {

	public static void main(String[] args) {


		/*********************** Parse main arguments ****************************/


		boolean withCache = false;
		boolean backed = false;
		long queryWaitingTime = 500L;
		long testTime = 60000L;
		String gameToTest = null;

		if(args.length != 0){ // At least one argument is specified and the first two argument are true or false

			if (args.length > 1 && args.length <= 4){

				withCache = Boolean.parseBoolean(args[0]);
				backed = Boolean.parseBoolean(args[1]);

				if(args.length == 3 || args.length == 5){
					gameToTest = args[args.length-1];
				}
				if(args.length == 4 || args.length == 5){
					try{
						queryWaitingTime = Long.parseLong(args[2]);
					}catch(NumberFormatException nfe){
						System.out.println("Inconsistent query maximum waiting time specification! Using default value.");
						queryWaitingTime = 500L;
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

		String machineType = "YAP state machine";
		if(backed){
			machineType += " backed by the GGP Base prover";
		}

		if(withCache){
			System.out.println("Testing speed of the " + machineType + " with cache.");
		}else{
			System.out.println("Testing speed of the " + machineType + " with no cache.");
		}

		if(gameToTest == null){
			System.out.println("Running tests on ALL games with the following time settings:");
		}else{
			System.out.println("Running tests on game " + gameToTest + " with the following time settings:");
		}
		System.out.println("Waiting time for a query result: " + queryWaitingTime + "ms");
		System.out.println("Running time for each test: " + testTime + "ms");
		System.out.println();


		/*********************** Perform all the tests ****************************/


		StateMachine theSubject;

		String type = "YAP";

		if(backed){
			type = "Backed" + type;
		}
		if(withCache){
			type = "Cached" + type;
		}

		GamerLogger.setSpilloverLogfile(type + "SpeedTestTable.csv");
		GamerLogger.log(FORMAT.CSV_FORMAT, type + "SpeedTestTable", "Game key;Initialization Time (ms);Test Duration (ms);Succeeded Iterations;Failed Iterations;Visited Nodes;Iterations/second;Nodes/second;");

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            // TODO: change code so that if there is only one game to test we won't run through the whole sequence of keys.
            if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

            System.out.println("Detected activation in game " + gameKey + ".");

            Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

           	GamerLogger.startFileLogging(fakeMatch, type + "SpeedTester");

            GamerLogger.log("SMSpeedTest", "Testing on game " + gameKey);

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            // Create Yap state machine giving it queryWaitingTime milliseconds to wait for the result of a query
            theSubject = new YapStateMachine(queryWaitingTime);

            if(backed){
            	// Create the BackedYapStateMachine
            	theSubject = new BackedYapStateMachine((YapStateMachine)theSubject, new ProverStateMachine());
            }
            // If the Yap state machine must be provided with a cache, create the cached state machine
            if(withCache){
            	theSubject = new CachedStateMachine(theSubject);
            }

            long initializationTime;
            long testDuration = -1L;
            int succeededIterations = -1;
            int failedIterations = -1;
            int visitedNodes = -1;
            double iterationsPerSecond = -1;
            double nodesPerSecond = -1;

            // Try to initialize the Yap state machine.
            // If initialization fails, skip the test.
        	long initStart = System.currentTimeMillis();
            try{
            	theSubject.initialize(description);
            	initializationTime = System.currentTimeMillis() - initStart;

            	System.out.println(type + " state machine initialization succeeded. Checking speed.");
            	StateMachineSpeedTest.testSpeed(theSubject, testTime);

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
            	System.out.println("Skipping test on game " + gameKey + ". State machine initialization failed.");
            }

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "SMSpeedTest", "");

            GamerLogger.stopFileLogging();

            GamerLogger.log(FORMAT.CSV_FORMAT, type + "SpeedTestTable", gameKey + ";" + initializationTime + ";" + testDuration + ";" + succeededIterations + ";" + failedIterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";");

            theSubject.shutdown();
        }
	}
}
