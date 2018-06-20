package csironi.ggp.course.MCTStest;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.decoupled.PnDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.sequential.PnSequentialTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.slowsequential.PnSlowSequentialTreeNodeFactory;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetManager;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;


/**
* This class checks the speed (nodes/second, iterations/second) of the UCT MCTS player
* (decoupled or sequential) that uses the separate propnet state machine.
*
* It is possible to specify the following combinations of main arguments:
*
* 1. [DUCT|SUCT|SLOW_SUCT]
* 2. [DUCT|SUCT|SLOW_SUCT] [keyOfGameToTest]
* 3. [DUCT|SUCT|SLOW_SUCT] [givenInitTime] [maximumTestDuration]
* 4. [DUCT|SUCT|SLOW_SUCT] [givenInitTime] [maximumTestDuration] [keyOfGameToTest]
*
* where:
* [DUCT|SUCT|SLOW_SUCT] = set it to the string "DUCT" if the player must perform Decoupled UCT, "SUCT" if
* 					  it must perform Sequential UCT, "SLOW_SUCT" if it must perform the slow version of
* 					  Sequential UCT (DEFAULT: "DUCT").
* [givenInitTime] = maximum time in milliseconds that should be spent to initialize the propnet state machine
* 					(DEFAULT: 420000ms - 7mins).
* [maximumTestDuration] = duration of each test in millisecond (DEFAULT: 60000ms - 1min).
* [keyOfGameToTest] = key of the game to be tested (DEFAULT: null (i.e. all games)).
*
* If nothing or something inconsistent is specified for any of the parameters, the default value will
* be used.
*
* @author C.Sironi
*
*/
public class MCTSSpeedTest {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	public static void main(String[] args) throws MoveDefinitionException, StateMachineException {

		/*********************** Parse main arguments ****************************/

		String mctsType = "DUCT";
		long givenInitTime = 420000L;
		long testTime = 60000L;
		String gameToTest = null;

		if(args.length != 0){ // At least one argument is specified and the first argument is the MCTS type.

			if(args[0].equals("SUCT") || args[0].equals("SLOW_SUCT")){
				mctsType = args[0];
			}

			if (args.length <= 4){

				if(args.length == 4 || args.length == 2){
					gameToTest = args[args.length-1];
				}
				if(args.length == 4 || args.length == 3){
					try{
						givenInitTime = Long.parseLong(args[1]);
					}catch(NumberFormatException nfe){
						System.out.println("Inconsistent maximum initialization time specification! Using default value.");
						givenInitTime = 420000L;
					}
					try{
						testTime = Long.parseLong(args[2]);
					}catch(NumberFormatException nfe){
						System.out.println("Inconsistent test duration specification! Using default value.");
						testTime = 60000L;
					}
				}
			}else{ // Too many arguments, using default
				System.out.println("Inconsistent number of main arguments! Ignoring them.");
			}

		} // else use default values

		System.out.println("Testing speed of " + mctsType + "/MCTS using the propnet state machine.");

		if(gameToTest == null){
			System.out.println("Running tests on ALL games with the following time settings:");
		}else{
			System.out.println("Running tests on game " + gameToTest + " with the following time settings:");
		}
		System.out.println("Propnet building time: " + givenInitTime + "ms");
		System.out.println("Running time for each test: " + testTime + "ms");
		System.out.println();


		/*********************** Perform all the tests ****************************/

		InternalPropnetStateMachine thePropnetMachine;

		String mainLogFolder = System.currentTimeMillis() + "-" + mctsType + "MCTSSpeedTest";
    	ThreadContext.put("LOG_FOLDER", mainLogFolder);

    	GamerLogger.startFileLogging();

		GamerLogger.log(FORMAT.CSV_FORMAT, mctsType + "MCTSSpeedTestTable", "Game key;#Roles;PN Construction Time (ms);PN Initialization Time (ms);SM initialization time;Test Duration (ms);Search time(ms);Iterations;Visited Nodes;Iterations/second;Nodes/second;Playing role;Chosen move;Scores sum;Visits;AverageScore");

	    GameRepository theRepository = new ManualUpdateLocalGameRepository("GGPBase-GameRepo-03022016");
	    for(String gameKey : theRepository.getGameKeys()) {
	        if(gameKey.contains("laikLee")) continue;

	        //if(gameKey.equals("ticTacHeaven")) continue;

	        // TODO: change code so that if there is only one game to test we won't run through the whole sequence of keys.
	        if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

	        System.out.println("Detected activation in game " + gameKey + ".");

	        Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey));

	        ThreadContext.put("LOG_FOLDER", mainLogFolder + "/" + fakeMatch.getMatchId() + "-" + mctsType + "MCTSSpeedTester");

	        GamerLogger.log(mctsType + "MCTSSpeedTest", "Testing on game " + gameKey);

	        List<Gdl> description = theRepository.getGame(gameKey).getRules();

	        // Create the executor service that will run the propnet manager that creates the propnet
	        ExecutorService executor = Executors.newSingleThreadExecutor();

	        // Create the propnet creation manager
	        SeparateInternalPropnetManager manager = new SeparateInternalPropnetManager(description, System.currentTimeMillis() + givenInitTime);

	  	  	// Start the manager
	  	  	executor.execute(manager);

	  	  	// Shutdown executor to tell it not to accept any more task to execute.
			// Note that this doesn't interrupt previously started tasks.
			executor.shutdown();

			// Tell the executor to wait until the currently running task has completed execution or the timeout has elapsed.
			try{
				executor.awaitTermination(givenInitTime, TimeUnit.MILLISECONDS);
			}catch(InterruptedException e){ // The thread running the verifier has been interrupted => stop the test
				executor.shutdownNow(); // Interrupt everything
				GamerLogger.logError(mctsType + "MCTSSpeedTest", mctsType + "/MCTS speed test interrupted. Test on game "+ gameKey +" won't be completed.");
				GamerLogger.logStackTrace(mctsType + "MCTSSpeedTest", e);
				GamerLogger.stopFileLogging();
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
					// If this exception is thrown it means the thread that is executing the speed test
					// of the DUCT/MCTS has been interrupted. If we do nothing this state machine could be stuck in the
					// while loop anyway until all tasks in the executor have terminated, thus we break out of the loop and return.
					// What happens to the still running tasks in the executor? Who will make sure they terminate?
					GamerLogger.logError(mctsType + "MCTSSpeedTest", "State mahcine verification interrupted. Test on game "+ gameKey +" won't be completed.");
					GamerLogger.logStackTrace(mctsType + "MCTSSpeedTest", e);
					GamerLogger.stopFileLogging();
					Thread.currentThread().interrupt();
					return;
				}
			}

			// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.

			ImmutablePropNet propnet = manager.getImmutablePropnet();
			ImmutableSeparatePropnetState propnetState = manager.getInitialPropnetState();
			Random random = new Random();

			// Create the state machine giving it the propnet and the propnet state.
			// NOTE that if any of the two is null, it means that the propnet creation/initialization went wrong
			// and this will be detected by the state machine during initialization.
		    thePropnetMachine = new SeparateInternalPropnetStateMachine(random, propnet, propnetState);

		    int numRoles = -1;
	        long initializationTime;
	        long testDuration = -1L;
	        long searchTime = -1L;
	        int iterations = -1;
	        int visitedNodes = -1;
	        double iterationsPerSecond = -1;
	        double nodesPerSecond = -1;
	        ExplicitRole playingRole = null;
	        ExplicitMove chosenMove = null;
	        double scoresSum = -1.0;
	        int visits = -1;
	        double averageScore = -1;

	        // Try to initialize the propnet state machine.
	        // If initialization fails, skip the test.
	    	long initStart = System.currentTimeMillis();
	        try{
	        	thePropnetMachine.initialize(description, initStart + givenInitTime);

	        	initializationTime = System.currentTimeMillis() - initStart;

	        	System.out.println("Propnet creation succeeded. Checking speed.");



		        /***************************************/
		        System.gc();
		        /***************************************/

		        double c = 0.7;
		        double unexploredMoveDefaultSelectionValue = Double.MAX_VALUE;
		        double uctOffset = 0.01;
		        int gameStep = 1;
		        int gameStepOffset = 2;
		        int maxSearchDepth = 500;

		        long testStart = System.currentTimeMillis();

		        GamerLogger.log(mctsType + "MCTSSpeedTest", "Starting speed test.");




		        //System.out.println(thePropnetMachine.getRoles());

		        //System.out.println(thePropnetMachine.getLegalMoves(thePropnetMachine.getInitialState(), thePropnetMachine.getRoles().get(0)));

		        //System.out.println(thePropnetMachine.getLegalMoves(thePropnetMachine.getInitialState(), thePropnetMachine.getRoles().get(1)));





		        CompactRole internalPlayingRole = thePropnetMachine.getCompactRoles().get(0);
		        playingRole = thePropnetMachine.convertToExplicitRole(internalPlayingRole);
		        numRoles = thePropnetMachine.getCompactRoles().size();

		        PnTreeNodeFactory theNodeFactory;

		        switch(mctsType){
		        	case "SUCT":
		        		theNodeFactory = new PnSequentialTreeNodeFactory(thePropnetMachine, internalPlayingRole);
		        		break;
		        	case "SLOW_SUCT":
		        		theNodeFactory = new PnSlowSequentialTreeNodeFactory(thePropnetMachine, internalPlayingRole);
		        		break;
		        	default:
			        	theNodeFactory = new PnDecoupledTreeNodeFactory(thePropnetMachine);
			        	break;
		        }

		        /*
		        InternalPropnetMCTSManager MCTSmanager = new InternalPropnetMCTSManager(
		        		new PnUCTSelection(numRoles, internalPlayingRole, r, uctOffset, new PnUCTEvaluator(c, unexploredMoveDefaultSelectionValue)),
		        		new PnRandomExpansion(numRoles, internalPlayingRole, r), new PnRandomPlayout(thePropnetMachine),
		        		new PnStandardBackpropagation(numRoles, internalPlayingRole),
		        		new PnMaximumScoreChoice(internalPlayingRole, r), null, null, null, theNodeFactory,
		        		thePropnetMachine, gameStepOffset, maxSearchDepth, false);
		        		*/

		        try{
		        	GamerLogger.log(mctsType + "MCTSSpeedTest", "Starting search.");

		        	//MctsNode initialNode = MCTSmanager.search(thePropnetMachine.getCompactInitialState(), System.currentTimeMillis() + testTime, gameStep);
		        	//PnCompleteMoveStats finalMove = MCTSmanager.getBestMove(initialNode);

		        	GamerLogger.log(mctsType + "MCTSSpeedTest", "Search ended correctly.");
		        	//chosenMove = thePropnetMachine.convertToExplicitMove(finalMove.getTheMove());
		 	        //scoresSum = finalMove.getScoreSum();
		 	        //visits = finalMove.getVisits();
		 	        if(visits != 0){
		 	        	averageScore = scoresSum / ((double) visits);
		 	        }
		 	        //iterations = MCTSmanager.getIterations();
		        	//visitedNodes = MCTSmanager.getVisitedNodes();
		        	//searchTime = MCTSmanager.getSearchTime();
		        	if(searchTime != 0){
			        	iterationsPerSecond = ((double) iterations * 1000)/((double) searchTime);
			        	nodesPerSecond = ((double) visitedNodes * 1000)/((double) searchTime);
		        	}
		        }catch(Exception e){
		        	GamerLogger.logError(mctsType + "MCTSSpeedTest", "MCTS failed during execution. Impossible to continue the speed test. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
		        	GamerLogger.logStackTrace(mctsType + "MCTSSpeedTest", e);
		        	System.out.println("Stopping test on game " + gameKey + ". Exception during search execution.");
		        }

		        testDuration = System.currentTimeMillis() - testStart;
	        }catch(StateMachineInitializationException e){
	        	initializationTime = System.currentTimeMillis() - initStart;
	        	GamerLogger.logError(mctsType + "MCTSSpeedTest", "State machine " + thePropnetMachine.getName() + " initialization failed, impossible to test this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	        	GamerLogger.logStackTrace(mctsType + "MCTSSpeedTest", e);
	        	System.out.println("Skipping test on game " + gameKey + ". No propnet available.");
	        }

	        ThreadContext.put("LOG_FOLDER", mainLogFolder);

	        GamerLogger.log(FORMAT.CSV_FORMAT, mctsType + "MCTSSpeedTestTable", gameKey + ";" + numRoles + ";" + manager.getPropnetConstructionTime() + ";" + manager.getTotalInitTime() + ";" + initializationTime + ";" + testDuration + ";" + searchTime + ";" + iterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";" + playingRole + ";" + chosenMove + ";" + scoresSum + ";" + visits + ";" + averageScore + ";");

	        /***************************************/
	        System.gc();
	        GdlPool.drainPool();
	        /***************************************/

	    }
	}

}
