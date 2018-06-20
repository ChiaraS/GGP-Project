/**
 *
 */
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
import org.ggp.base.util.statemachine.hybrid.AdaptiveInitializationStateMachine;
import org.ggp.base.util.statemachine.hybrid.BackedYapStateMachine;
import org.ggp.base.util.statemachine.implementation.propnet.FwdInterrPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.implementation.yapProlog.YapStateMachine;

//TODO: merge all speed tests together in a single class since their code is similar.

/**
* This class checks the speed (nodes/second, iterations/second) of the AdaptiveInitializationStateMachine
* (i.e. the one that selects which of the given state machines to use as real state machine depending
* on which one is faster for the given game).
* This is done by performing Monte Carlo simulations from the initial state of the given game for the
* specified amount of time.
*
* It is possible to specify the following combinations of main arguments:
*
* 1. [withCache]
* 2. [withCache] [keyOfGameToTest]
* 3. [withCache] [givenInitTime] [safetyMargin] [maximumTestDuration]
* 4. [withCache] [givenInitTime] [safetyMargin] [maximumTestDuration] [keyOfGameToTest]
*
* where:
* [withCache] = true if the state machine must be provided with a cache for its results, false
* 				otherwise (DEFAULT: false).
* [givenInitTime] = maximum time in milliseconds that should be spent to initialize the
* 					state machine (DEFAULT: 300000ms - 5mins).
* [safetyMargin] = the time (in milliseconds) that the state machine should subtract from the
* 				   [givenInitTime] to be more sure to be able to finish initialization in time.
* 				   (DEFAULT: 0L, i.e. wait all the [givenInitTime]).
* [maximumTestDuration] = duration of each test in millisecond (DEFAULT: 60000ms - 1min).
* [keyOfGameToTest] = key of the game to be tested (DEFAULT: null (i.e. all games)).
*
* If nothing or something inconsistent is specified for any of the parameters, the default value will
* be used.
*
* @author C.Sironi
*
*/
public class AdaptiveInitSpeedTest {

	public static void main(String[] args) {

		/*********************** Parse main arguments ****************************/

		boolean withCache = false;
		long givenInitTime = 300000L;
		long safetyMargin = 0L;
		long testTime = 60000L;
		String gameToTest = null;

		if(args.length != 0){ // At least one argument is specified and the first argument is either true or false

			if (args.length <= 5){

				withCache = Boolean.parseBoolean(args[0]);

				if(args.length == 5 || args.length == 2){
					gameToTest = args[args.length-1];
				}
				if(args.length == 4 || args.length == 5){
					try{
						givenInitTime = Long.parseLong(args[1]);
					}catch(NumberFormatException nfe){
						System.out.println("Inconsistent maximum initialization time specification! Using default value.");
						givenInitTime = 300000L;
					}
					try{
						safetyMargin = Long.parseLong(args[2]);
					}catch(NumberFormatException nfe){
						System.out.println("Inconsistent Adaptive Initialization state machine safety margin specification! Using default value.");
						safetyMargin = 0L;
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

		if(withCache){
			System.out.println("Testing speed of the Adaptive Initialization state machine with cache.");
		}else{
			System.out.println("Testing speed of the Adaptive Initialization state machine with no cache.");
		}

		if(gameToTest == null){
			System.out.println("Running tests on ALL games with the following time settings:");
		}else{
			System.out.println("Running tests on game " + gameToTest + " with the following time settings:");
		}
		System.out.println("State machine maximum initialization time: " + givenInitTime + "ms");
		System.out.println("Safety margin for the state machine initialization time: " + safetyMargin + "ms");
		System.out.println("Running time for each test: " + testTime + "ms");
		System.out.println();


		/*********************** Perform all the tests ****************************/


		StateMachine theSubject;

		String type = "AdaptiveInit";
		if(withCache){
			type = "Cached" + type;
		}

		GamerLogger.setSpilloverLogfile(type + "SpeedTestTable.csv");
	    GamerLogger.log(FORMAT.CSV_FORMAT, type + "SpeedTestTable", "Game key;Initialization Time (ms);Test Duration (ms);Succeeded Iterations;Failed Iterations;Visited Nodes;Iterations/second;Nodes/second;SelectedStateMachine");

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

            StateMachine[] theMachines = new StateMachine[3];
            theMachines[0] = new FwdInterrPropnetStateMachine(random);
            theMachines[1] = new BackedYapStateMachine(random, new YapStateMachine(random, 500L), new ProverStateMachine(random));
            theMachines[2] = new ProverStateMachine(random);
            // Create the state machine giving it the sub-state machines that it has to check
            theSubject = new AdaptiveInitializationStateMachine(random, theMachines, safetyMargin);
            // If the state machine must be provided with a cache, create the cached state machine
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

            // Try to initialize the state machine.
            // If initialization fails, skip the test.
        	long initStart = System.currentTimeMillis();
            try{
            	theSubject.initialize(description, initStart + givenInitTime);

            	initializationTime = System.currentTimeMillis() - initStart;

            	System.out.println("Propnet creation succeeded. Checking speed.");

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
            	System.out.println("Skipping test on game " + gameKey + ". No propnet available.");
            }

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "SMSpeedTest", "");

            GamerLogger.stopFileLogging();

            GamerLogger.log(FORMAT.CSV_FORMAT, type + "SpeedTestTable", gameKey + ";" + initializationTime + ";" + testDuration + ";" + succeededIterations + ";" + failedIterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";" + theSubject.getName());

            theSubject.shutdown();
        }
	}

}
