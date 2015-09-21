/**
 *
 */
package csironi.ggp.course;

import java.util.List;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.propnet.CheckFwdInterrPropNetStateMachine;

/**
 * @author C.Sironi
 *
 */
public class PropnetSpeedTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long buildingTime = 300000L;
		long testTime = 60000L;

		String gameToTest = null;

		if (args.length != 0){

			if(args.length >= 2){
				try{
					buildingTime = Long.parseLong(args[0]);
					testTime = Long.parseLong(args[1]);
					System.out.println("Running tests with the following settings:");
				}catch(NumberFormatException nfe){
					System.out.println("Inconsistent time values specification! Running tests with default settings:");
					buildingTime = 300000L;
					testTime = 60000L;
				}
				if(args.length > 2){
					gameToTest = args[2];
				}
			}else{
				System.out.println("Inconsistent time values specification! Running tests with default settings:");
			}
		}else{
			System.out.println("Running tests with default settings:");
		}

		System.out.println("Propnet building time: " + buildingTime);
		System.out.println("Running time for each test: " + testTime);
		System.out.println();

		if(gameToTest != null){
			System.out.println("Running test only on game " +  gameToTest);
		}else{
			System.out.println("Running tests on ALL games.");
		}

		System.out.println();

		CheckFwdInterrPropNetStateMachine thePropNetMachine;

        GamerLogger.setSpilloverLogfile("PropnetSpeedTestTable.csv");
        GamerLogger.log(FORMAT.CSV_FORMAT, "PropnetSpeedTestTable", "Game key;Construction Time (ms);Test Duration (ms);Succeeded Iterations;Failed Iterations;Visited Nodes;Iterations/second;Nodes/second;");

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

            //if(!gameKey.equals("3pConnectFour") && !gameKey.equals("god")) continue;

            //if(!gameKey.equals("mummymaze1p")) continue;

            Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

            GamerLogger.startFileLogging(fakeMatch, "PropnetSpeedTester");

            GamerLogger.log("SMSpeedTest", "Testing on game " + gameKey);

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            // Create propnet state machine giving it buildingTime milliseconds to build the propnet
            thePropNetMachine = new CheckFwdInterrPropNetStateMachine(buildingTime);

            long testDuration = -1L;
            int succeededIterations = -1;
            int failedIterations = -1;
            int visitedNodes = -1;
            double iterationsPerSecond = -1;
            double nodesPerSecond = -1;

            // Try to initialize the propnet state machine.
            // If initialization fails, skip the test.
            try{
            	System.out.println("Detected activation in game " + gameKey + ".");
            	thePropNetMachine.initialize(description);
            	System.out.println("Propnet creation succeeded. Checking speed.");
            	StateMachineSpeedTest.testSpeed(thePropNetMachine, testTime);

            	testDuration = StateMachineSpeedTest.exactTimeSpent;
            	succeededIterations = StateMachineSpeedTest.succeededIterations;
         		failedIterations = StateMachineSpeedTest.failedIterations;
                visitedNodes = StateMachineSpeedTest.visitedNodes;
                iterationsPerSecond = ((double) succeededIterations * 1000)/((double) testDuration);
                nodesPerSecond = ((double) visitedNodes * 1000)/((double) testDuration);
            }catch(StateMachineInitializationException e){
            	GamerLogger.log("SMSpeedTest", "No propnet available. Impossible to test its speed in this game. Cause: " + e.getMessage());
            	System.out.println("Skipping test on game " + gameKey + ". No propnet available.");
            }

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "SMSpeedTest", "");

            GamerLogger.stopFileLogging();
            GamerLogger.log(FORMAT.CSV_FORMAT, "PropnetSpeedTestTable", gameKey + ";" + thePropNetMachine.getConstructionTime() + ";" + testDuration + ";" + succeededIterations + ";" + failedIterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";");

        }

	}



}
