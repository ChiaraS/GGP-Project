package csironi.ggp.course;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.implementation.propnet.ForwardInterruptingPropNetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.verifier.StateMachineVerifier;

/**
 * This class verifies the consistency of the propnet state machine wrt the prover state machine.
 *
 * It is possible to specify as main arguments the time in milliseconds that the state machine has to initialize
 * and the time in milliseconds that each test must take to run. If nothing or something inconsistent is specified,
 * 5 mins is used as default value for the state machine initialization time and 10 seconds is used as default
 * value for each test's duration time.
 *
 * @author C.Sironi
 *
 */
public class ModifiedPropnetVerifier {

	public static void main(String[] args) throws InterruptedException{

		class PropnetInitializer implements Callable<Long>{

			private List<Gdl> description;

			private ForwardInterruptingPropNetStateMachine theMachine;

			public void setInitializer(List<Gdl> description, ForwardInterruptingPropNetStateMachine theMachine){
				this.description = description;
				this.theMachine = theMachine;
			}

			@Override
			public Long call() throws Exception {
				long start = System.currentTimeMillis();
				this.theMachine.initialize(this.description);
				return System.currentTimeMillis() - start;

			}
		}

		//long buildingTime = 300000L;
		long maxInitializationTime = 300000L;
		long testTime = 10000L;

		if (args.length != 0){

			if(args.length == 2){
				try{
					//buildingTime = Long.parseLong(args[0]);
					maxInitializationTime = Long.parseLong(args[0]);
					testTime = Long.parseLong(args[1]);
					System.out.println("Running tests with the following settings:");
				}catch(NumberFormatException nfe){
					System.out.println("Inconsistent time values specification! Running tests with default settings:");
					//buildingTime = 300000L;
					maxInitializationTime = 300000L;
					testTime = 10000L;
				}
			}else{
				System.out.println("Inconsistent time values specification! Running tests with default settings:");
			}
		}else{
			System.out.println("Running tests with default settings:");
		}

		//System.out.println("Propnet building time: " + buildingTime);
		System.out.println("Propnet initialization time: " + maxInitializationTime);

		System.out.println("Running time for each test: " + testTime);
		System.out.println();

        ProverStateMachine theReference;
        ForwardInterruptingPropNetStateMachine thePropNetMachine;

        GamerLogger.setSpilloverLogfile("PropnetVerifierTable.csv");
        GamerLogger.log(FORMAT.CSV_FORMAT, "PropnetVerifierTable", "Game key;Initialization time (ms);Propnet Construction time (ms);Rounds;Test duration (ms);Pass;");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        PropnetInitializer initializer = new PropnetInitializer();

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            //if(!gameKey.equals("3pConnectFour") && !gameKey.equals("god")) continue;

            //if(!gameKey.equals("coins_atomic")) continue;

            Match fakeMatch = new Match(gameKey + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey) );

            GamerLogger.startFileLogging(fakeMatch, "PropnetVerifier");

            GamerLogger.log("Verifier", "Testing on game " + gameKey);

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine();

            // Create propnet state machine giving it 5 minutes to build the propnet
            thePropNetMachine = new ForwardInterruptingPropNetStateMachine();

            theReference.initialize(description);

            long constructionTime = -1L;
            long initializationTime = -1L;
            int rounds = -1;
            long duration = -1L;
            boolean pass = false;

            // Try to initialize the propnet state machine.
            // If initialization fails, skip the test.
            initializer.setInitializer(description, thePropNetMachine);

            long initStart = System.currentTimeMillis();

            try {
            	initializationTime = executor.invokeAny(Arrays.asList(initializer), maxInitializationTime, TimeUnit.MILLISECONDS);
            	System.out.println("Detected activation in game " + gameKey + ". Checking consistency: ");
            	long start = System.currentTimeMillis();
                pass = StateMachineVerifier.checkMachineConsistency(theReference, thePropNetMachine, testTime);
                duration = System.currentTimeMillis() - start;
                rounds = StateMachineVerifier.lastRounds;
                constructionTime = thePropNetMachine.getPropnetConstructionTime();
			} catch (ExecutionException | TimeoutException e) {
				// Reset executor and initializer
				executor.shutdownNow();
				// Wait for all tasks to be completed before continuing (needed to avoid any interrupted
				// but still running task in the executor to write logs on the wrong file -> after executor
				// shutdown the logging file might already have been changed when the still running tasks
				// log a message).
				if(!executor.isTerminated()){
					// If not all tasks terminated, wait for a minute and then check again
					executor.awaitTermination(1, TimeUnit.MINUTES);
				}
				executor = Executors.newSingleThreadExecutor();
				initializer = new PropnetInitializer();
				// Check anyway how much time was spent for initialization (this tells us exactly how much
				// extra time the propnet creation took to realize it had been interrupted and stop execution.
				initializationTime = System.currentTimeMillis() - initStart;
				GamerLogger.logError("Verifier", "State machine " + thePropNetMachine.getName() + " initialization failed, impossible to test this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
            	GamerLogger.logStackTrace("Verifier", e);
				System.out.println("Skipping test on game " + gameKey + ". State machine initialization failed.");
			}

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "Verifier", "");

            GamerLogger.stopFileLogging();

            GamerLogger.log(FORMAT.CSV_FORMAT, "PropnetVerifierTable", gameKey + ";" + initializationTime + ";" + constructionTime + ";" + rounds + ";" + duration + ";" + pass + ";");
        }

        // Otherwise this program will never stop
        executor.shutdownNow();
	}

}
