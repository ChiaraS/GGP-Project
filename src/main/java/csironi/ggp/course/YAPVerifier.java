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
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.implementation.yapProlog.YapStateMachine;
import org.ggp.base.util.statemachine.verifier.StateMachineVerifier;

/**
 * This class verifies the consistency of the YAP state machine wrt the prover state machine.
 *
 * It is possible to specify as main argument the time in milliseconds that each test must take to run.
 * If nothing or something inconsistent is specified, 10 seconds is used as default value for each test
 * duration time.
 *
 * @author C.Sironi
 *
 */
public class YAPVerifier {

	public static void main(String[] args) throws InterruptedException{

		long testTime = 10000L;

		if (args.length != 0){

			if(args.length == 1){
				try{
					testTime = Long.parseLong(args[0]);
					System.out.println("Running tests with the following time duration: " + testTime);
				}catch(NumberFormatException nfe){
					testTime = 10000L;
					System.out.println("Inconsistent time value specification! Running tests with default time duration: " + testTime);
				}
			}else{
				System.out.println("Inconsistent time value specification! Running tests with default time duration: " + testTime);
			}
		}else{
			System.out.println("Running tests with default time duration: " + testTime);
		}

		System.out.println();

        ProverStateMachine theReference;
        YapStateMachine theYapMachine;

        GamerLogger.setSpilloverLogfile("YAPVerifierTable.csv");
        GamerLogger.log(FORMAT.CSV_FORMAT, "YAPVerifierTable", "Game key;Rounds;Test duration (ms);Pass;");

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            //if(!gameKey.equals("3pConnectFour") && !gameKey.equals("god")) continue;

            //if(!gameKey.equals("mummymaze1p")) continue;

            Match fakeMatch = new Match(gameKey + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

            GamerLogger.startFileLogging(fakeMatch, "YAPVerifier");

            GamerLogger.log("YAPVerifier", "Testing on game " + gameKey);

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine();

            // Create the YAP state machine
            theYapMachine = new YapStateMachine(new ProverStateMachine());

            boolean pass = false;
            int rounds = -1;
            long duration = 0L;

            // Initialize the state machines
            theReference.initialize(description);
          	theYapMachine.initialize(description);

          	System.out.println("Detected activation in game " + gameKey + ". Checking consistency: ");
            long start = System.currentTimeMillis();
            pass = StateMachineVerifier.checkMachineConsistency(theReference, theYapMachine, testTime);
            duration = System.currentTimeMillis() - start;
            rounds = StateMachineVerifier.lastRounds;

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "YAPVerifier", "");

            GamerLogger.stopFileLogging();
            GamerLogger.log(FORMAT.CSV_FORMAT, "YapVerifierTable", gameKey + ";" + rounds + ";" + duration + ";" + pass + ";");
        }
	}

}
