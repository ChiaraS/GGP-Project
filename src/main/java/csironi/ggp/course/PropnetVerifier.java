package csironi.ggp.course;

import java.util.List;
import java.util.concurrent.Callable;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.transforms.DistinctAndNotMover;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.statemachine.implementation.propnet.ForwardInterruptingPropNetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.verifier.StateMachineVerifier;

/**
 * TODOOOOOO:
 * 2. switcha alla prima versione funzionante della propnet, mergiala col master, implementa il
 * test con MCS e runnalo sul server.
 * 3. prova a sistemare yap prover usando DistinctAndNotMover.run(description) sulla game
 * description per vedere se il verifier riesce a passare i test dove fallisce senza
 * eccezione.
 * 1. merge di yap prover refactored con il master
 * 4. aggiungi un check del timeout anche all'aima prover
 * 5. Va che dopo aver sistemato tutto e aver testato le varie velocità devi anche provare tutto
 * con la cached state machine
 */



/**
 * This class verifies the consistency of the propnet state machine wrt the prover state machine.
 *
 * It is possible to specify as main arguments the time in milliseconds that the propnet state machine must use to
 * build the propnet and the time in milliseconds that each test must take to run. If nothing or something
 * inconsistent is specified, 5 mins is used as default value for the propnet building time and 10 seconds is used
 * as default value for each test duration time.
 *
 * @author C.Sironi
 *
 */
public class PropnetVerifier {

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
        ForwardInterruptingPropNetStateMachine thePropNetMachine;

        GamerLogger.setSpilloverLogfile("PropnetVerifierTable.csv");
        GamerLogger.log(FORMAT.CSV_FORMAT, "PropnetVerifierTable", "Game key;PropNet Construction Time (ms);Initialization time;Rounds;Test duration (ms);Pass;");

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            //if(!gameKey.equals("3pConnectFour") && !gameKey.equals("god")) continue;

            //if(!gameKey.equals("mummymaze1p")) continue;

            GamerLogger.setSpilloverLogfile("PropnetVerifierLogs.log");

            GamerLogger.log("StateMachine", "Testing on game " + gameKey);

            List<Gdl> description = theRepository.getGame(gameKey).getRules();


            theReference = new ProverStateMachine();

            // Create propnet state machine giving it 5 minutes to build the propnet
            thePropNetMachine = new ForwardInterruptingPropNetStateMachine();

            theReference.initialize(description);

            boolean pass = false;
            int rounds = -1;
            long duration = 0L;

            // Try to initialize the propnet state machine.
            // If initialization fails, skip the test.
            try{fiiiixx
            	thePropNetMachine.initialize(description);
            	System.out.println("Detected activation in game " + gameKey + ". Checking consistency: ");
            	long start = System.currentTimeMillis();
                pass = StateMachineVerifier.checkMachineConsistency(theReference, thePropNetMachine, testTime);
                duration = System.currentTimeMillis() - start;
                rounds = StateMachineVerifier.lastRounds;
            }catch(Exception e){
            	GamerLogger.log("StateMachine", "State machine " + thePropNetMachine.getName() + " initialization failed, impossible to test this game. Cause: " + e.getMessage());
            	System.out.println("Skipping test on game " + gameKey + ". State machine initialization failed.");
            }

            GamerLogger.log(FORMAT.PLAIN_FORMAT, "StateMachine", "");

            GamerLogger.setSpilloverLogfile("PropnetVerifierTable.csv");
            GamerLogger.log(FORMAT.CSV_FORMAT, "PropnetVerifierTable", gameKey + ";" + thePropNetMachine.getPropnetConstructionTime() + ";" + rounds + ";" + duration + ";" + pass + ";");
        }
	}


	public class PropnetInitializer implements Callable<Long>{

		private List<Gdl> description;

		private ForwardInterruptingPropNetStateMachine theMachine;

		private long initializationTime;

		public PropnetInitializer(List<Gdl> description, ForwardInterruptingPropNetStateMachine theMachine){
			this.description = description;
			this.theMachine = theMachine;
			this.initializationTime = -1L;

		}

		@Override
		public Long call() throws Exception {
			long start = System.currentTimeMillis();
			this.theMachine.initialize(this.description);
			return System.currentTimeMillis() - start;

		}
	}

}
