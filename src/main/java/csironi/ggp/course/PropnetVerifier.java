package csironi.ggp.course;

import java.util.List;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.implementation.propnet.ForwardInterruptingPropNetStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.verifier.StateMachineVerifier;

public class PropnetVerifier {

	public static void main(String[] args) throws InterruptedException {

		GamerLogger.setSpilloverLogfile("PropnetVerifierResults");

        ProverStateMachine theReference;
        ForwardInterruptingPropNetStateMachine thePropNetMachine;

        GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(gameKey.contains("laikLee")) continue;

            if(gameKey.equals("amazonsTorus")) continue;
            if(gameKey.equals("god")) continue;

            //if(!gameKey.equals("3pConnectFour")) continue;

            GamerLogger.log("StateMachine", "Testing on game " + gameKey);

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            theReference = new ProverStateMachine();
            thePropNetMachine = new ForwardInterruptingPropNetStateMachine();

            theReference.initialize(description);
            thePropNetMachine.initialize(description);

            System.out.println("Detected activation in game " + gameKey + ". Checking consistency: ");
            StateMachineVerifier.checkMachineConsistency(theReference, thePropNetMachine, 10000);
        }

	}

}
