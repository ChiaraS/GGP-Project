package csironi.ggp.course.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetCreationManager;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.internalPropnet.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.internalPropnet.SeparateInternalPropnetStateMachine;

public class LimitedBFSearch {

	/**
	 * Static reference to the logger
	 */
	private static final Logger LOGGER;

	static{

		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
		System.setProperty("isThreadContextMapInheritable", "true");

		LOGGER = LogManager.getRootLogger();

	}

	private StateMachine theMachine;

	public LimitedBFSearch(StateMachine theMachine) {
		this.theMachine = theMachine;
	}

	public void BFSearch(MachineState state, int maxDepth){

		System.out.println("Starting.");

		int currentDepth = 0;

		ArrayList<MachineState> frontier = new ArrayList<MachineState>();

		ArrayList<MachineState> nextFrontier = new ArrayList<MachineState>();

		frontier.add(state);

		int numRoles = this.theMachine.getRoles().size();

		while(!(frontier.isEmpty())){

			System.out.println();
			System.out.println("Depth = " + currentDepth);

			for(MachineState s: frontier){

				//System.out.print(".");

				boolean terminal = false;
				try {
					terminal = this.theMachine.isTerminal(s);
				} catch (StateMachineException e) {
					LOGGER.error("[BFS] [STEP "+ currentDepth + "] Exception when computing terminality.", e);
					return;
				}

				if(terminal){
					try {
						this.theMachine.getGoals(s);
					} catch (GoalDefinitionException | StateMachineException e) {
						LOGGER.error("[BFS] [STEP "+ currentDepth + "] Exception when computing goals.", e);
					}
				}else if(currentDepth <= maxDepth){
					// Get legal moves of all players
					List<List<Move>> legalMoves = new ArrayList<List<Move>>();

					for(Role r : this.theMachine.getRoles()){
						try {
							legalMoves.add(this.theMachine.getLegalMoves(s, r));
						} catch (MoveDefinitionException | StateMachineException e) {
							LOGGER.error("[BFS] [STEP "+ currentDepth + "] Exception when computing legal moves.", e);
							return;
						}
					}

					// Create all joint moves
					List<Move> partialJointMove = new ArrayList<Move>();
					for(int i = 0; i < numRoles; i++){
						partialJointMove.add(null);
					}

					List<List<Move>> jointMoves = new ArrayList<List<Move>>();

					this.getAllJointMoves(legalMoves, 0, numRoles, partialJointMove, jointMoves);

					// For every joint move add next state to the next frontier
					for(List<Move> jointMove :  jointMoves){
						try {
							nextFrontier.add(this.theMachine.getNextState(s, jointMove));
						} catch (TransitionDefinitionException | StateMachineException e) {
							LOGGER.error("[BFS] [STEP "+ currentDepth + "] Exception when computing next state.", e);
							return;
						}
					}
				}
			}

			currentDepth++;

			frontier = nextFrontier;
			nextFrontier = new ArrayList<MachineState>();

		}
	}

	private void getAllJointMoves(List<List<Move>> legalMoves, int currentRoleIndex, int numRoles, List<Move> partialJointMove, List<List<Move>> jointMoves){

		// We have a complete joint move and we can add it to the joint moves
		if(currentRoleIndex == numRoles){
			jointMoves.add(new ArrayList<Move>(partialJointMove));
		}else{
			for(Move m : legalMoves.get(currentRoleIndex)){
				partialJointMove.set(currentRoleIndex, m);
				this.getAllJointMoves(legalMoves, currentRoleIndex+1, numRoles, partialJointMove, jointMoves);
			}
		}
	}

	public static void main(String args[]){

		if(args.length != 1){
			System.out.println("Specify the game key.");
			return;
		}

		ThreadContext.put("LOG_FOLDER", System.currentTimeMillis() + "BFS");
		ThreadContext.put("LOG_FILE", "logs");

		String gameKey = args[0];

		List<Gdl> description = GameRepository.getDefaultRepository().getGame(gameKey).getRules();

		//ProverStateMachine theProver = new ProverStateMachine();

		//theProver.initialize(description, Long.MAX_VALUE);

		//LimitedBFSearch searcher = new LimitedBFSearch(theProver);

		//searcher.BFSearch(theProver.getInitialState(), 20);



		long initializationTime = 300000L;

		// Create the executor service that will run the propnet manager that creates the propnet
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Create the propnet creation manager
        SeparateInternalPropnetCreationManager manager = new SeparateInternalPropnetCreationManager(description, System.currentTimeMillis() + initializationTime);

        // Start the manager
  	  	executor.execute(manager);

  	  	// Shutdown executor to tell it not to accept any more task to execute.
		// Note that this doesn't interrupt previously started tasks.
		executor.shutdown();

		// Tell the executor to wait until the currently running task has completed execution or the timeout has elapsed.
		try{
			executor.awaitTermination(initializationTime, TimeUnit.MILLISECONDS);
		}catch(InterruptedException e){ // The thread running the verifier has been interrupted => stop the test
			executor.shutdownNow(); // Interrupt everything
			LOGGER.error("[BFS] State machine verification interrupted. Test on game "+ gameKey +" won't be completed.", e);
			Thread.currentThread().interrupt();
			return;
		}

		// Here the available time has elapsed, so we must interrupt the thread if it is still running.
		executor.shutdownNow();

		// Wait for the thread to actually terminate
		while(!executor.isTerminated()){

			// If the thread didn't terminate, wait for a minute and then check again
			try{
				executor.awaitTermination(1, TimeUnit.MINUTES);
			}catch(InterruptedException e) {
				// If this exception is thrown it means the thread that is executing the verification
				// of the state machine has been interrupted. If we do nothing this state machine could be stuck in the
				// while loop anyway until all tasks in the executor have terminated, thus we break out of the loop and return.
				// What happens to the still running tasks in the executor? Who will make sure they terminate?
				LOGGER.error("[BFS] State machine verification interrupted. Test on game "+ gameKey +" won't be completed.", e);
				Thread.currentThread().interrupt();
				return;
			}
		}

		// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.

		ImmutablePropNet propnet = manager.getImmutablePropnet();
		ImmutableSeparatePropnetState propnetState = manager.getInitialPropnetState();

		if(propnet == null || propnetState == null){
			LOGGER.error("[BFS] Impossible to create the state machine. Propnet or/and propnetState equal to null");
		}
		InternalPropnetStateMachine thePropnet = new SeparateInternalPropnetStateMachine(propnet, propnetState);

		try {
			thePropnet.initialize(description, Long.MAX_VALUE);
		} catch (StateMachineInitializationException e) {
			LOGGER.error("[BFS] Impossible to create the state machine. Propnet or/and propnetState equal to null");
		}

		LimitedBFSearch searcher = new LimitedBFSearch(thePropnet);

		searcher.BFSearch(thePropnet.getInitialState(), 20);

	}

	/*
	private static void prova(List<List<String>> legalMoves, int currentRoleIndex, int numRoles, List<String> partialJointMove, List<List<String>> jointMoves){

		// We have a complete joint move and we can add it to the joint moves
		if(currentRoleIndex == numRoles){
			jointMoves.add(new ArrayList<String>(partialJointMove));
		}else{
			for(String m : legalMoves.get(currentRoleIndex)){
				partialJointMove.set(currentRoleIndex, m);
				prova(legalMoves, currentRoleIndex+1, numRoles, partialJointMove, jointMoves);
			}
		}
	}
	*/

	/*
	public static void main(String args[]){
		List<List<String>> legalMoves = new ArrayList<List<String>>();

		int numRoles = 3;

		for(int i = 0; i < 3; i++){

			List<String> s = new ArrayList<String>();
			s.add("a");
			s.add("b");

			legalMoves.add(s);
		}

		// Create all joint moves
		List<String> partialJointMove = new ArrayList<String>();
		for(int i = 0; i < numRoles; i++){
			partialJointMove.add(null);
		}

		List<List<String>> jointMoves = new ArrayList<List<String>>();

		prova(legalMoves, 0, numRoles, partialJointMove, jointMoves);

		// For every joint move add next state to the next frontier
		for(List<String> jointMove :  jointMoves){
			System.out.print("Moves: [");
			for(int j = 0; j < jointMove.size(); j++){
				System.out.print(" " + jointMove.get(j));
			}
			System.out.println(" ]");
		}
	}
	*/



}
