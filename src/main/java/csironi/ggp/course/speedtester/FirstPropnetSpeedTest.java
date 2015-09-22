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
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.propnet.CheckFwdInterrPropnetStateMachine;

/**
 * @author C.Sironi
 *
 */
public class FirstPropnetSpeedTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		/*********************** Parse main arguments ****************************/

		long buildingTime = 300000L;
		long testTime = 60000L;
		String gameToTest = null;

		if (args.length != 0 && args.length <= 3){
			if(args.length == 3 || args.length == 1){
				gameToTest = args[args.length-1];
			}
			if(args.length == 2 || args.length == 3){
				try{
					buildingTime = Long.parseLong(args[0]);
				}catch(NumberFormatException nfe){
					System.out.println("Inconsistent propnet maximum building time specification! Using default value.");
					buildingTime = 300000L;
				}
				try{
					testTime = Long.parseLong(args[1]);
				}catch(NumberFormatException nfe){
					System.out.println("Inconsistent test duration specification! Using default value.");
					testTime = 10000L;
				}
			}
		}else if(args.length > 3){
			System.out.println("Inconsistent number of main arguments! Ignoring them.");
		}

		if(gameToTest == null){
			System.out.println("Running tests on ALL games with the following time settings:");
		}else{
			System.out.println("Running tests on game " + gameToTest + " with the following time settings:");
		}
		System.out.println("Propnet building time: " + buildingTime + "ms");
		System.out.println("Running time for each test: " + testTime + "ms");
		System.out.println();


		/*********************** Perform all the tests ****************************/


		CheckFwdInterrPropnetStateMachine thePropNetMachine;

        GamerLogger.setSpilloverLogfile("FirstPropnetSpeedTestTable.csv");
        GamerLogger.log(FORMAT.CSV_FORMAT, "FirstPropnetSpeedTestTable", "Game key;Initialization Time (ms);Construction Time (ms);Test Duration (ms);Succeeded Iterations;Failed Iterations;Visited Nodes;Iterations/second;Nodes/second;");

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

            System.out.println("Detected activation in game " + gameKey + ".");

            Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

            GamerLogger.startFileLogging(fakeMatch, "PropnetSpeedTester");

            GamerLogger.log("SMSpeedTest", "Testing on game " + gameKey);

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            // Create propnet state machine giving it buildingTime milliseconds to build the propnet
            thePropNetMachine = new CheckFwdInterrPropnetStateMachine(buildingTime);

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
            	thePropNetMachine.initialize(description);
            	initializationTime = System.currentTimeMillis() - initStart;
            	System.out.println("Propnet creation succeeded. Checking speed.");
            	StateMachineSpeedTest.testSpeed(thePropNetMachine, testTime);

            	testDuration = StateMachineSpeedTest.exactTimeSpent;
            	succeededIterations = StateMachineSpeedTest.succeededIterations;
         		failedIterations = StateMachineSpeedTest.failedIterations;
                visitedNodes = StateMachineSpeedTest.visitedNodes;
                iterationsPerSecond = ((double) succeededIterations * 1000)/((double) testDuration);
                nodesPerSecond = ((double) visitedNodes * 1000)/((double) testDuration);
            }catch(StateMachineInitializationException e){
            	initializationTime = System.currentTimeMillis() - initStart;
            	GamerLogger.log("SMSpeedTest", "No propnet available. Impossible to test its speed in this game. Cause: " + e.getMessage());
            	System.out.println("Skipping test on game " + gameKey + ". No propnet available.");
            }

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "SMSpeedTest", "");

            GamerLogger.stopFileLogging();

            GamerLogger.log(FORMAT.CSV_FORMAT, "PropnetSpeedTestTable", gameKey + ";" + initializationTime + ";" + thePropNetMachine.getConstructionTime() + ";" + testDuration + ";" + succeededIterations + ";" + failedIterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";");

        }

	}



}
