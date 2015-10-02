package csironi.ggp.course;

import java.util.List;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.hybrid.AdaptiveInitializationStateMachine;
import org.ggp.base.util.statemachine.hybrid.BackedYapStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.implementation.yapProlog.YapStateMachine;

public class ProvaAdaptiveInit {

	public static void main(String[] args) {

		StateMachine theSubject;

		GameRepository theRepository = GameRepository.getDefaultRepository();

		String gameKey;
		Game game;

		for(int i = 0; i < args.length; i++){

			gameKey = args[i];

			System.out.println("Looking for game " + gameKey + "...");

			game = theRepository.getGame(gameKey);

			if(game == null){
				System.out.println("Impossible to find game " + gameKey + ". Skipping its test.");
				continue;
			}else{
				System.out.println("Testing on game " + gameKey + "...");
			}

			Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1, game);
            GamerLogger.startFileLogging(fakeMatch, "ProvaAdaptiveInit");

            List<Gdl> description = game.getRules();

            StateMachine[] theMachines = new StateMachine[1];

            //theMachines[0] = new FwdInterrPropnetStateMachine();
            theMachines[0] = new BackedYapStateMachine(new YapStateMachine(500L), new ProverStateMachine());
            //theMachines[0] = new ProverStateMachine();

            theSubject = new AdaptiveInitializationStateMachine(theMachines, 0L);

            long startTime = System.currentTimeMillis();
            long initTime;

            try {
				theSubject.initialize(description, System.currentTimeMillis() + 300000L);
				initTime = System.currentTimeMillis() - startTime;
				System.out.println("Initialization succeeded.");
			} catch (StateMachineInitializationException e) {
				initTime = System.currentTimeMillis() - startTime;
				System.out.println("Initialization failed.");
			}

            System.out.println("Initialization time: " + initTime + "ms.");
            System.out.println("");

		}
	}

}
