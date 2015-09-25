/**
 *
 */
package csironi.ggp.course.verifiers;

import java.util.List;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

//TODO: merge all verifiers together in a single class since their code is similar.

/**
 * This class verifies the consistency of the GGP Base prover state machine wrt the GGP Base prover
 * state machine.
 * This test has been created only to have a comparison of the number of rounds that can be played
 * when changing the subject state machine wrt using the prover state machine both as subject and
 * as reference state machine.
 *
 * It is possible to specify the following combinations of main arguments:
 *
 * 1. [keyOfGameToTest]
 * 2. [maximumTestDuration]
 * 3. [maximumTestDuration] [keyOfGameToTest]
 *
 * where:
 * [maximumTestDuration] = duration of each test in millisecond (DEFAULT: 60000ms - 1min).
 * [keyOfGameToTest] = key of the game to be tested (DEFAULT: null (i.e. all games)).
 *
 * If nothing or something inconsistent is specified for any of the parameters, the default value
 * will be used.
 *
 * @author C.Sironi
 *
 */
public class GGPBaseProverVerifier {

	public static void main(String[] args) throws InterruptedException{


		/*********************** Parse main arguments ****************************/


		long testTime = 60000L;
		String gameToTest = null;

		if (args.length != 0){

			if(args.length == 1){
				// Check if it's a number
				try{
					testTime = Long.parseLong(args[0]);
				}catch(NumberFormatException nfe){
					testTime = 60000L;
					gameToTest = args[0];
				}
			}else if(args.length == 2){
				try{
					testTime = Long.parseLong(args[0]);
				}catch(NumberFormatException nfe){
					System.out.println("Inconsistent test duration specification! Using default value.");
					testTime = 60000L;
				}
				gameToTest = args[1];
			}else{
				System.out.println("Inconsistent number of main arguments! Ignoring them.");
			}
		}

		if(gameToTest == null){
			System.out.println("Running tests on ALL games with the following time setting:");
		}else{
			System.out.println("Running tests on game " + gameToTest + " with the following time setting:");
		}

		System.out.println("Running time for each test: " + testTime + "ms");
		System.out.println();


		/*********************** Perform all the tests ****************************/

		ProverStateMachine theReference;
        ProverStateMachine theProverMachine;

        GamerLogger.setSpilloverLogfile("GGPBaseProverVerifierTable.csv");
        GamerLogger.log(FORMAT.CSV_FORMAT, "GGPBaseProverVerifierTable", "Game key;Initialization time (ms);Rounds;Completed rounds;Test duration (ms);Subject exception;Other exceptions;Pass;");

		GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            // TODO: change code so that if there is only one game to test we won't run through the whole sequence of keys.
            if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

            System.out.println("Detected activation in game " + gameKey + ".");

            Match fakeMatch = new Match(gameKey + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

            GamerLogger.startFileLogging(fakeMatch, "GGPBaseProverVerifier");

            GamerLogger.log("Verifier", "Testing on game " + gameKey);

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine();

            // Create the second prover state machine
            theProverMachine = new ProverStateMachine();

            long initializationTime;
            int rounds;
            int completedRounds;
            long testDuration;
            boolean pass;
            String exception;
            int otherExceptions;

            // Initialize the state machines
            theReference.initialize(description);

            long initStart = System.currentTimeMillis();
            theProverMachine.initialize(description);
            initializationTime = System.currentTimeMillis() - initStart;

          	System.out.println("Prover state machine initialization succeeded. Checking consistency.");
            long testStart = System.currentTimeMillis();
            pass = ExtendedStateMachineVerifier.checkMachineConsistency(theReference, theProverMachine, testTime);
            testDuration = System.currentTimeMillis() - testStart;
            rounds = ExtendedStateMachineVerifier.lastRounds;
            completedRounds = ExtendedStateMachineVerifier.completedRounds;
            exception = ExtendedStateMachineVerifier.exception;
            otherExceptions = ExtendedStateMachineVerifier.otherExceptions;

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "Verifier", "");

            GamerLogger.stopFileLogging();

            GamerLogger.log(FORMAT.CSV_FORMAT, "GGPBaseProverVerifierTable", gameKey + ";" + initializationTime + ";" + rounds +  ";"  + completedRounds + ";" + testDuration + ";"  + exception + ";"  + otherExceptions + ";" + pass + ";");
        }
	}
}
