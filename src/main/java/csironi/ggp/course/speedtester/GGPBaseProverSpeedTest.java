package csironi.ggp.course.speedtester;

import java.util.List;
import java.util.Random;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

//TODO: merge all speed tests together in a single class since their code is similar.

/**
 * This class checks the speed (nodes/second, iterations/second) of the GGP Base prover state machine.
 * This is done by performing Monte Carlo simulations from the initial state of the given game for the
 * specified amount of time.
 *
 * It is possible to specify the following combinations of main arguments:
 *
 * 1. [withCache]
 * 2. [withCache] [keyOfGameToTest]
 * 3. [withCache] [maximumTestDuration]
 * 4. [withCache] [maximumTestDuration] [keyOfGameToTest]
 *
 * where:
 * [withCache] = true if the GGP Base prover state machine must be provided with a cache for its results,
 * 				 false otherwise (DEFAULT: false).
 * [maximumTestDuration] = duration of each test in millisecond (DEFAULT: 60000ms - 1min).
 * [keyOfGameToTest] = key of the game to be tested (DEFAULT: null (i.e. all games)). *
 *
 * @author C.Sironi
 *
 */
public class GGPBaseProverSpeedTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		/*********************** Parse main arguments ****************************/


		boolean withCache = false;
		long testTime = 60000L;
		String gameToTest = null;

		if (args.length != 0){ // At least one argument is specified and the first argument is either true or false

			if(args.length <= 3){

				withCache = Boolean.parseBoolean(args[0]);

				if(args.length == 2){
					// Check if the second argument is a number, thus the maximum test duration..
					try{
						testTime = Long.parseLong(args[1]);
					}catch(NumberFormatException nfe){
						// ...and if not, it is probably the key of the game to test.
						testTime = 60000L;
						gameToTest = args[1];
					}
				}else if(args.length == 3){
					try{
						testTime = Long.parseLong(args[1]);
					}catch(NumberFormatException nfe){
						System.out.println("Inconsistent test duration specification! Using default value.");
						testTime = 60000L;
					}
					gameToTest = args[2];
				}
			}else{
				System.out.println("Inconsistent number of main arguments! Ignoring them.");
			}
		}

		if(withCache){
			System.out.println("Testing speed of the GGP Base prover state machine with cache.");
		}else{
			System.out.println("Testing speed of the GGP Base prover state machine with no cache.");
		}

		if(gameToTest == null){
			System.out.println("Running tests on ALL games with the following time setting:");
		}else{
			System.out.println("Running tests on game " + gameToTest + " with the following time setting:");
		}

		System.out.println("Running time for each test: " + testTime + "ms");
		System.out.println();


		/*********************** Perform all the tests ****************************/


		StateMachine theSubject;

		String type = "Prover";
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

            Random random = new Random();

            // Create prover state machine
            theSubject = new ProverStateMachine(random);
            // If the prover state machine must be provided with a cache, create the cached state machine
            if(withCache){
            	theSubject = new CachedStateMachine(random, theSubject);
            }

            long initializationTime;
            long testDuration = -1L;
            int succeededIterations = -1;
            int failedIterations = -1;
            int visitedNodes = -1;
            double iterationsPerSecond = -1;
            double nodesPerSecond = -1;

            // Try to initialize the (cached) prover state machine
            long initStart = System.currentTimeMillis();
            try {

            	theSubject.initialize(description, Long.MAX_VALUE);

				initializationTime = System.currentTimeMillis() - initStart;

	           	System.out.println(type + " state machine initialization succeeded. Checking speed.");

	            StateMachineSpeedTest.testSpeed(theSubject, testTime);

	           	testDuration = StateMachineSpeedTest.exactTimeSpent;
	           	succeededIterations = StateMachineSpeedTest.succeededIterations;
	       		failedIterations = StateMachineSpeedTest.failedIterations;
	            visitedNodes = StateMachineSpeedTest.visitedNodes;
	            iterationsPerSecond = ((double) succeededIterations * 1000)/((double) testDuration);
	            nodesPerSecond = ((double) visitedNodes * 1000)/((double) testDuration);
			} catch (StateMachineInitializationException e) {
				initializationTime = System.currentTimeMillis() - initStart;
				GamerLogger.logError("SMSpeedTest", "State machine " + theSubject.getName() + " initialization failed, impossible to test this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
				GamerLogger.logStackTrace("SMSpeedTest", e);
            	System.out.println("Skipping test on game " + gameKey + ". State machine initialization failed.");
			}

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "SMSpeedTest", "");

            GamerLogger.stopFileLogging();

            GamerLogger.log(FORMAT.CSV_FORMAT, type + "SpeedTestTable", gameKey + ";" + initializationTime + ";" + testDuration + ";" + succeededIterations + ";" + failedIterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";");
        }
	}
}
