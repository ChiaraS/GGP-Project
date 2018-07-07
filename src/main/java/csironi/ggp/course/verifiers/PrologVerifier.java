package csironi.ggp.course.verifiers;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.hybrid.BackedPrologStateMachine;
import org.ggp.base.util.statemachine.implementation.prolog.PrologStateMachine;
import org.ggp.base.util.statemachine.implementation.prolog.prover.PrologProver;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

// TODO: merge all verifiers together in a single class since their code is similar.


/** NOTE: THE ONLY WORKING PROLOG STATE MACHINE IS THE ONE BASED ON YAP AND WORKS ONLY ON LINUX.
 *  IF YOU WANNA TEST IT YOU CAN RATHER USE THE YAPVerifier CLASS
 *
 * This class verifies the consistency of the Prolog state machine wrt the prover state machine.
 *
 * It is possible to specify the following combinations of main arguments:
 *
 * 1. [backed] [type]
 * 2. [backed] [type] [keyOfGameToTest]
 * 3. [backed] [type] [queryWaitingTime] [maximumTestDuration]
 * 4. [backed] [type] [queryWaitingTime] [maximumTestDuration] [keyOfGameToTest]
 *
 * where:
 * [backed] = true if the Prolog state machine must be backed by the GGP Base prover state machine
 * 			  when the Prolog prover doesn't answer to a query in time, false otherwise (DEFAULT:
 * 			  false).
 * [type] = the type of prolog to be used. Available options are:
 * 		YAP : for Yap Prolog
 * 		SWI : for Swi Prolog
 * 		XSB : for Xsb Prolog
 * 		NXSB : for Xsb Prolog used as native engine (no subprocess will be created).
 * 	(DEFAULT: YAP).
 * [queryWaitingTime] = maximum time in milliseconds that the Prolog state machine must wait for
 * 						getting the result of a query from prolog (DEFAULT: 500ms).
 * 						ATTENTION: it's better to never run the tests with this parameter set to
 * 						0ms, as this will cause the state machine to wait indefinitely and thus
 * 						the program will get stuck if prolog doesn't answer.
 * [maximumTestDuration] = duration of each test in millisecond (DEFAULT: 60000ms - 1min).
 * [keyOfGameToTest] = key of the game to be tested (DEFAULT: null (i.e. all games)).
 *
 * If nothing or something inconsistent is specified for any of the parameters, the default value
 * will be used.
 *
 * @author C.Sironi
 *
 */
public class PrologVerifier {

	public static void main(String[] args) throws InterruptedException{


		/*********************** Parse main arguments ****************************/

		boolean backed = false;
		String type = "YAP";
		PrologProver.PROLOG_TYPE prologType = null;
		long queryWaitingTime = 500L;
		long testTime = 60000L;
		String gameToTest = null;

		if (args.length != 0){

			if(args.length <= 5){

				backed = Boolean.parseBoolean(args[0]);
				type = args[1];

				if(args.length == 5 || args.length == 3){
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
		}

		type = type.toUpperCase(Locale.ENGLISH);

		switch(type){
			case "YAP":
				prologType = PrologProver.PROLOG_TYPE.YAP;
				break;
			case "SWI":
				prologType = PrologProver.PROLOG_TYPE.SWI;
				break;
			case "XSB":
				prologType = PrologProver.PROLOG_TYPE.XSB;
				break;
			case "NXSB":
				prologType = PrologProver.PROLOG_TYPE.NXSB;
				break;
			default:
				System.out.println("Inconsistent prolog type specification! Using default value.");
				type = "YAP";
				prologType = PrologProver.PROLOG_TYPE.YAP;
				break;
		}


		if(backed){
			System.out.println("Testing consistency of the " + type + " prolog state machine backed by the GGP Base prover.");
		}else{
			System.out.println("Testing consistency of the " + type + " prolog state machine.");
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


        ProverStateMachine theReference;
        StateMachine theSubject;

        if(backed){
        	type = "Backed" + type + "Prolog";
        }

        GamerLogger.setSpilloverLogfile(type + "VerifierTable.csv");
        GamerLogger.log(FORMAT.CSV_FORMAT, type + "VerifierTable", "Game key;Initialization time (ms);Rounds;Completed rounds;Test duration (ms);Subject exception;Other exceptions;Pass;");

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            // TODO: change code so that if there is only one game to test we won't run through the whole sequence of keys.
            if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

            System.out.println("Detected activation in game " + gameKey + ".");

            Match fakeMatch = new Match(gameKey + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

            GamerLogger.startFileLogging(fakeMatch, type + "Verifier");

            GamerLogger.log("Verifier", "Testing on game " + gameKey);

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine(new Random());

            Random random = new Random();
            // Create the Prolog state machine
            theSubject = new PrologStateMachine(random, prologType, queryWaitingTime);

            if(backed){
            	// Create the BackedPrologStateMachine
            	theSubject = new BackedPrologStateMachine(random, (PrologStateMachine)theSubject, new ProverStateMachine(random));
            }

            theReference.initialize(description, Long.MAX_VALUE);

            long initializationTime;
            int rounds = -1;
            int completedRounds = -1;
            long testDuration = -1L;
            boolean pass = false;
            String exception = "-";
            int otherExceptions = -1;

            long initStart = System.currentTimeMillis();

            // Try to initialize the prolog state machine.
            // If initialization fails, skip the test.
          	try {
				theSubject.initialize(description, Long.MAX_VALUE);
				initializationTime = System.currentTimeMillis() - initStart;
				System.out.println(type + " state machine initialization succeeded. Checking consistency.");
				long testStart = System.currentTimeMillis();
                pass = ExtendedStateMachineVerifier.checkMachineConsistency(theReference, theSubject, testTime);
                testDuration = System.currentTimeMillis() - testStart;
                rounds = ExtendedStateMachineVerifier.lastRounds;
                completedRounds = ExtendedStateMachineVerifier.completedRounds;
                exception = ExtendedStateMachineVerifier.exception;
                otherExceptions = ExtendedStateMachineVerifier.otherExceptions;
			} catch (StateMachineInitializationException e) {
				initializationTime = System.currentTimeMillis() - initStart;
            	GamerLogger.logError("Verifier", "State machine " + theSubject.getName() + " initialization failed, impossible to test this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
            	GamerLogger.logStackTrace("Verifier", e);
            	System.out.println("Skipping test on game " + gameKey + ". State machine initialization failed.");
			}

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "Verifier", "");

            GamerLogger.stopFileLogging();

            GamerLogger.log(FORMAT.CSV_FORMAT, type + "VerifierTable", gameKey + ";" + initializationTime + ";" + rounds +  ";"  + completedRounds + ";"  + testDuration + ";"  + exception + ";"  + otherExceptions + ";" + pass + ";");

            theSubject.shutdown();

        }
	}

}

