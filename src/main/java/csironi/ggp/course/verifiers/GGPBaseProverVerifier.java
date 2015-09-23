/**
 *
 */
package csironi.ggp.course.verifiers;

import org.ggp.base.util.game.GameRepository;

/**
 * This class verifies the consistency of the prover state machine wrt the prover state machine.
 * This test has been created only to have a comparison of the number of rounds that can be played
 * when changing the subject state machine wrt using the prover state machine both as subject and
 * as reference state machine.
 *
 * It is possible to specify the following combinations of main arguments:
 *
 * 1. [keyOfGameToTest]
 * 2. [maximumTestDuration(ms)]
 * 3. [maximumTestDuration(ms)] [keyOfGameToTest]
 *
 * If nothing or something inconsistent is specified, 5 mins is used as default value for the propnet
 * building time and 10 seconds is used as default value for each test duration time.
 *
 * @author C.Sironi
 *
 */
public class GGPBaseProverVerifier {

	public static void main(String[] args) throws InterruptedException{

		/*********************** Parse main arguments ****************************/

		long testTime = 10000L;
		String gameToTest = null;

		if (args.length != 0){

			if(args.length == 1){
				// Check if it's a number
				try{
					testTime = Long.parseLong(args[0]);
				}catch(NumberFormatException nfe){
					testTime = 10000L;
					gameToTest = args[0];
				}
			}else if(args.length == 2){
				try{
					testTime = Long.parseLong(args[0]);
				}catch(NumberFormatException nfe){
					System.out.println("Inconsistent test duration specification! Using default value.");
					testTime = 10000L;
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




		/*

        ProverStateMachine theReference;
        ProverStateMachine theOtherMachine;

        GamerLogger.setSpilloverLogfile("GGPBaseProverVerifierTable.csv");
        GamerLogger.log(FORMAT.CSV_FORMAT, "GGPBaseProverVerifierTable", "Game key;Rounds;Test duration (ms);Pass;");
		 */

		GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            // TODO: change code so that if there is only one game to test we won't run through the whole sequence of keys.
            if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

            System.out.println("Detected activation in game " + gameKey + ".");

            /*
            Match fakeMatch = new Match(gameKey + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

            GamerLogger.startFileLogging(fakeMatch, "GGPBaseProverVerifier");

            GamerLogger.log("Verifier", "Testing on game " + gameKey);

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine();

            // Create the YAP state machine
            theOtherMachine = new ProverStateMachine();

            boolean pass = false;
            int rounds = -1;
            long duration = 0L;

            // Initialize the state machines
            theReference.initialize(description);
          	theOtherMachine.initialize(description);

          	System.out.println("Detected activation in game " + gameKey + ". Checking consistency: ");
            long start = System.currentTimeMillis();
            pass = StateMachineVerifier.checkMachineConsistency(theReference, theOtherMachine, testTime);
            duration = System.currentTimeMillis() - start;
            rounds = StateMachineVerifier.lastRounds;

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "Verifier", "");

            GamerLogger.stopFileLogging();
            GamerLogger.log(FORMAT.CSV_FORMAT, "GGPBaseProverVerifierTable", gameKey + ";" + rounds + ";" + duration + ";" + pass + ";");

			*/

        }
	}


}
