package csironi.ggp.course;

import ggpbasebenchmark.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.LocalFolderGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.propnet.creationManager.PropNetManagerRunner;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetManager;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

public class TrickyGDLTester {

	public static void main(String args[]){

		if(args.length < 3){
			System.out.println("Specify the gameKey, the propnetCreationTime and one or more trace files (note: each trace file must contain only one trace).");
			return;
		}

		String gameKey = args[0];
		long pnCreationTime = Long.parseLong(args[1]);

		String tracesFolder = "C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\BenchmarkFiles";
		String[] traceFiles = new String[args.length-2];

		for(int i = 0; i < traceFiles.length; i++){
			traceFiles[i] = tracesFolder + "\\" + args[i+2];
		}

        System.out.println("Looking for game " + gameKey + "...");

        System.out.println(GamerConfiguration.defaultLocalGameRepositoryFolderPath);

        //GameRepository theRepository = new ManualUpdateLocalGameRepository(GamerConfiguration.defaultLocalGameRepositoryFolderPath);
        GameRepository theRepository = new LocalFolderGameRepository("C:/Users/c.sironi/BITBUCKET REPOS/GGP-Base/GDLFolder");

        Game theGame = theRepository.getGame(gameKey);

        if(theGame == null){
        	System.out.println("Couldn't find it. Impossible to build and print propnet.");
        	return;
        }

        Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey));

        System.out.println("Building propnet for game " + gameKey);

        List<Gdl> description = theRepository.getGame(gameKey).getRules();

        // Create the propnet creation manager
        SeparateInternalPropnetManager manager = new SeparateInternalPropnetManager(description, System.currentTimeMillis() + pnCreationTime);

		manager =  new SeparateInternalPropnetManager(description, System.currentTimeMillis() + pnCreationTime);

		//manager =  new SeparateInternalPropnetManager(description, System.currentTimeMillis() + pnCreationTime, optimizations);

		PropNetManagerRunner.runPropNetManager(manager, pnCreationTime);

		// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.
		if(manager.getImmutablePropnet() == null || manager.getInitialPropnetState() == null){
			System.out.println("Impossible to play the match. Propnet and/or propnet state are null.");
			return;
		}

		SeparateInternalPropnetStateMachine theMachine = new SeparateInternalPropnetStateMachine(manager.getImmutablePropnet(), manager.getInitialPropnetState());

		try {
			theMachine.initialize(description);
		} catch (StateMachineInitializationException e1) {
			System.out.println("Error initializing state machine!");
			return;
		}

		List<Trace> traces = new ArrayList<Trace>();

		System.out.println("Creating traces...");

		// Create the traces
		for(int i = 0; i < traceFiles.length; i++){
			try {
				traces.add(Trace.loadFromFile(new File(traceFiles[i])));
			} catch (Exception e) {
				System.out.println("Error while creating traces!");
				throw new RuntimeException("Wrong trace format.", e);
			}
		}

		for(Trace t : traces){
			// Compute second to last state
			ExplicitMachineState state = simulateTrace(theMachine, t);

			System.out.println("Reached second to last state.");

			// Check if current state is erroneously detected as terminal
			if(theMachine.isTerminal(state)){
				System.out.println("NON TERMINAL STATE DETECTED AS TERMINAL!");
				throw new RuntimeException("Terminal non-terminal state!");
			}

			/**
			 * To avoid the problem with cycles, copy the whole propnet state when the propnet is in
			 * the previous to last state of the game and each time, before simulating any move in the
			 * previous to last game state, reset the whole propnet state to this copied version.
			 * NOTE: this is just a trick used here for testing. Add to the propnet an internal way to
			 * deal with incorrect propagation in cycles!!!!!!
			 */
			ImmutableSeparatePropnetState propnetState = theMachine.getPropNetState().clone();

			// Compute next state according to last move in trace
			List<ExplicitMove> lastMove = t.get(t.size()-1);
			ExplicitMachineState nextState = theMachine.getExplicitNextState(state, lastMove);
			// Check if next state is erroneously detected as non-terminal
			if(!theMachine.isTerminal(nextState)){
				System.out.println("TERMINAL STATE DETECTED AS NON-TERMINAL!");
				throw new RuntimeException("Non-terminal terminal state!");
			}

			/**
			 * Reset the propnet state to the correct version before computing legal moves
			 */
			theMachine.resetPropNetState(propnetState.clone());

			// Compute all possible next states and count how many are terminal
			// (note: use a trace that leads to a state that only has 1 terminal successor state,
			// that is the one reached by performing the last move of the trace. In this case the
			// count of how many among all the other states are terminal should be 0!)
			List<List<ExplicitMove>> allJointMoves = null;
			try {
				allJointMoves = theMachine.getLegalJointMoves(state);
			} catch (MoveDefinitionException | StateMachineException e) {
				System.out.println("ERROR WHEN COMPUTING LEGAL JOINT MOVES!");
				throw new RuntimeException("Error when computing legal joint moves!");
			}

			int count = 0;
			for(List<ExplicitMove> jm : allJointMoves){
				if(!jm.equals(lastMove)){
					/**
					 * Reset the propnet state to the correct version before computing next state
					 */
					theMachine.resetPropNetState(propnetState.clone());

					nextState = theMachine.getExplicitNextState(state, jm);
					if(theMachine.isTerminal(state)){
						count++;
					}
				}
			}

			System.out.println("DETECTED " + count + " STATES THAT ARE TERMINAL!");
		}

	}

	/**
	 * Simulate trace except last move and return second to last state of the episode.
	 *
	 * @param theMachine
	 * @param theTrace
	 * @return
	 */
	private static ExplicitMachineState simulateTrace(SeparateInternalPropnetStateMachine theMachine, Trace theTrace){

		List<ExplicitRole> roles = theMachine.getExplicitRoles();

		System.out.println();
		System.out.println("Simulating new trace:");

		ExplicitMachineState state = theMachine.getExplicitInitialState();

		System.out.println();
		System.out.println("INITIAL_STATE[ " + state + " ]");

		// Advance the state for each joint move except the last and check if the state is terminal before the end of trace
		for(int i = 0; i < theTrace.size()-1; i++){

			// Check if current state is erroneously detected as terminal
			if(theMachine.isTerminal(state)){
				System.out.println("NON TERMINAL STATE DETECTED AS TERMINAL!");
				int[] goals = theMachine.getSafeGoals(state);
				String goalsString = "[ ";
				for(int j = 0; j < goals.length; j++){
					goalsString += (goals[j] + " ");
				}
				goalsString += "]";
				System.out.println("GOALS = " + goalsString);
				throw new RuntimeException("Terminal non-terminal state!");
			}

			// Print all legal moves for each role in this state
			System.out.println("LEGAL_MOVES[");

			for(ExplicitRole r : roles){
				try {
					System.out.println("     " + r + " = " + theMachine.getExplicitLegalMoves(state, r));
				} catch (MoveDefinitionException e) {
					System.out.println("ERROR WHEN COMPUTING LEGAL MOVES!");
					throw new RuntimeException("Error when computing legal moves!");
				}
			}

			System.out.println(" ]");
			System.out.println("PEWRFORMING_MOVE = " + theTrace.get(i));

			// Compute next state
			state = theMachine.getExplicitNextState(state, theTrace.get(i));

			System.out.println();
			System.out.println("NEXT_STATE[ " + state + " ]");

		}

		return state;
	}
}
