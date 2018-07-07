package csironi.ggp.course.verifiers;

import java.util.List;
import java.util.Random;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.propnet.FwdInterrPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.safe.InitializationSafeStateMachine;

//TODO: merge all verifiers together in a single class since their code is similar.

/**
 * This class verifies the consistency of the propnet state machine (with no internal check of the
 * propnet building time) wrt the prover state machine.
 *
 * It is possible to specify the following combinations of main arguments:
 *
 * 1. [keyOfGameToTest]
 * 2. [maximumInitializationTime] [maximumTestDuration]
 * 3. [maximumInitializationTime] [maximumTestDuration] [keyOfGameToTest]
 *
 * where:
 * [maximumInitializationTime] = maximum time in milliseconds that should be spent to initialize
 * 								 the propnet state machine (DEFAULT: 300000ms - 5mins).
 * [maximumTestDuration] = duration of each test in millisecond (DEFAULT: 60000ms - 1min).
 * [keyOfGameToTest] = key of the game to be tested (DEFAULT: null (i.e. all games)).
 *
 * If nothing or something inconsistent is specified for any of the parameters, the default value will
 * be used.
 *
 * @author C.Sironi
 *
 */
public class ModifiedPropnetVerifier {

	public static void main(String[] args) throws InterruptedException{


		/*********************** Parse main arguments ****************************/


		long givenInitTime = 300000L;
		long testTime = 60000L;
		String gameToTest = null;

		if (args.length != 0 && args.length <= 3){
			if(args.length == 3 || args.length == 1){
				gameToTest = args[args.length-1];
			}
			if(args.length == 2 || args.length == 3){
				try{
					givenInitTime = Long.parseLong(args[0]);
				}catch(NumberFormatException nfe){
					System.out.println("Inconsistent maximum initialization time specification! Using default value.");
					givenInitTime = 300000L;
				}
				try{
					testTime = Long.parseLong(args[1]);
				}catch(NumberFormatException nfe){
					System.out.println("Inconsistent test duration specification! Using default value.");
					testTime = 60000L;
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
		System.out.println("Propnet building time: " + givenInitTime + "ms");
		System.out.println("Running time for each test: " + testTime + "ms");
		System.out.println();


		/*********************** Perform all the tests ****************************/


		ProverStateMachine theReference;
        InitializationSafeStateMachine thePropnetMachine;
        FwdInterrPropnetStateMachine theHiddenPropnetMachine;

        GamerLogger.setSpilloverLogfile("ModifiedPropnetVerifierTable.csv");
        GamerLogger.log(FORMAT.CSV_FORMAT, "ModifiedPropnetVerifierTable", "Game key;Initialization time (ms);Construction time (ms);Rounds;Completed rounds;Test duration (ms);Subject exception;Other exceptions;Pass;");

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            // TODO: change code so that if there is only one game to test we won't run through the whole sequence of keys.
            if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

            System.out.println("Detected activation in game " + gameKey + ".");

            Match fakeMatch = new Match(gameKey + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

            GamerLogger.startFileLogging(fakeMatch, "ModifiedPropnetVerifier");

            GamerLogger.log("Verifier", "Testing on game " + gameKey);

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine(new Random());

            // Create the propnet state machine and wrap it with the state machine that controls initialization
            Random random = new Random();
            theHiddenPropnetMachine = new FwdInterrPropnetStateMachine(random);
            thePropnetMachine = new InitializationSafeStateMachine(random, theHiddenPropnetMachine);

            theReference.initialize(description, Long.MAX_VALUE);

            long initializationTime;
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
            	thePropnetMachine.initialize(description, initStart + givenInitTime);
            	initializationTime = System.currentTimeMillis() - initStart;
            	System.out.println("Propnet creation succeeded. Checking consistency.");
            	long testStart = System.currentTimeMillis();
                pass = ExtendedStateMachineVerifier.checkMachineConsistency(theReference, thePropnetMachine, testTime);
                testDuration = System.currentTimeMillis() - testStart;
                rounds = ExtendedStateMachineVerifier.lastRounds;
                completedRounds = ExtendedStateMachineVerifier.completedRounds;
                exception = ExtendedStateMachineVerifier.exception;
                otherExceptions = ExtendedStateMachineVerifier.otherExceptions;
			}catch(StateMachineInitializationException e){
				initializationTime = System.currentTimeMillis() - initStart;
				GamerLogger.logError("Verifier", "State machine " + thePropnetMachine.getName() + " initialization failed, impossible to test this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
            	GamerLogger.logStackTrace("Verifier", e);
				System.out.println("Skipping test on game " + gameKey + ". State machine initialization failed, no propnet available.");
			}

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "Verifier", "");

            GamerLogger.stopFileLogging();

            GamerLogger.log(FORMAT.CSV_FORMAT, "ModifiedPropnetVerifierTable", gameKey + ";" + initializationTime + ";" + theHiddenPropnetMachine.getPropnetConstructionTime() + ";" + rounds + ";" + completedRounds + ";" + testDuration + ";"  + exception + ";"  + otherExceptions + ";" + pass + ";");
        }

        System.out.println();
	}
}
