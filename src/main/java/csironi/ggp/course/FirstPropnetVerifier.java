package csironi.ggp.course;

import java.util.List;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.propnet.CheckFwdInterrPropNetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.verifier.StateMachineVerifier;

/**
 * This class verifies the consistency of the propnet state machine (the one that checks internally during
 * initialization that the building time of the propnet doesn't exceed a given threshold) wrt the prover
 * state machine.
 *
 * It is possible to specify as main arguments the time in milliseconds that the propnet state machine must
 * use to build the propnet and the time in milliseconds that each test must take to run. If nothing or
 * something inconsistent is specified, 5 mins is used as default value for the propnet building time and 10
 * seconds is used as default value for each test duration time.
 *
 * @author C.Sironi
 *
 */
public class FirstPropnetVerifier {

	public static void main(String[] args) throws InterruptedException{

		long buildingTime = 300000L;
		long testTime = 10000L;

		if (args.length != 0){

			if(args.length == 2){
				try{
					buildingTime = Long.parseLong(args[0]);
					testTime = Long.parseLong(args[1]);
					System.out.println("Running tests with the following settings:");
				}catch(NumberFormatException nfe){
					System.out.println("Inconsistent time values specification! Running tests with default settings:");
					buildingTime = 300000L;
					testTime = 10000L;
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

        ProverStateMachine theReference;
        CheckFwdInterrPropNetStateMachine thePropNetMachine;

        GamerLogger.setSpilloverLogfile("PropnetVerifierTable.csv");
        GamerLogger.log(FORMAT.CSV_FORMAT, "PropnetVerifierTable", "Game key;Initialization Time (ms);Construction Time (ms);Rounds;Test duration (ms);Pass;");

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            //if(!gameKey.equals("3pConnectFour") && !gameKey.equals("god")) continue;

            //if(!gameKey.equals("mummymaze1p")) continue;

            Match fakeMatch = new Match(gameKey + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

            GamerLogger.startFileLogging(fakeMatch, "PropnetVerifier");

            GamerLogger.log("Verifier", "Testing on game " + gameKey);

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine();

            // Create propnet state machine giving it 5 minutes to build the propnet
            thePropNetMachine = new CheckFwdInterrPropNetStateMachine(buildingTime);

            theReference.initialize(description);

            boolean pass = false;
            int rounds = -1;
            long duration = 0L;
            long initializationDuration;

            long initStart = System.currentTimeMillis();

            // Try to initialize the propnet state machine.
            // If initialization fails, skip the test.
            try{
            	thePropNetMachine.initialize(description);
            	initializationDuration = System.currentTimeMillis() - initStart;
            	System.out.println("Detected activation in game " + gameKey + ". Checking consistency: ");
            	long start = System.currentTimeMillis();
                pass = StateMachineVerifier.checkMachineConsistency(theReference, thePropNetMachine, testTime);
                duration = System.currentTimeMillis() - start;
                rounds = StateMachineVerifier.lastRounds;
            }catch(StateMachineInitializationException e){
            	initializationDuration = System.currentTimeMillis() - initStart;
            	GamerLogger.log("StateMachine", "No propnet available. Impossible to test this game. Cause: " + e.getMessage());
            	System.out.println("Skipping test on game " + gameKey + ". No propnet available.");
            }

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "Verifier", "");

            GamerLogger.stopFileLogging();

            GamerLogger.log(FORMAT.CSV_FORMAT, "PropnetVerifierTable", gameKey + ";" + initializationDuration + ";" + thePropNetMachine.getConstructionTime() + ";" + rounds + ";" + duration + ";" + pass + ";");
        }
	}

}
