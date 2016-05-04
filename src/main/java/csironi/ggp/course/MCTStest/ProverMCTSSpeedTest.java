package csironi.ggp.course.MCTStest;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.gamer.statemachine.MCS.manager.prover.ProverCompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.backpropagation.ProverStandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.expansion.ProverRandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.movechoice.ProverMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.ProverRandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.ProverUCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.selection.evaluators.ProverUCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.ProverTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.treestructure.decoupled.ProverDecoupledTreeNodeFactory;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;


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
* 					  Sequential UCT (DEFAULT: "DUCT"). !ONLY DUCT IS ACCEPTED FOR NOW
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
public class ProverMCTSSpeedTest {

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

		System.out.println("Testing speed of " + mctsType + "/MCTS using the prover state machine.");

		if(gameToTest == null){
			System.out.println("Running tests on ALL games with the following time settings:");
		}else{
			System.out.println("Running tests on game " + gameToTest + " with the following time settings:");
		}
		System.out.println("State machine initialization time: " + givenInitTime + "ms");
		System.out.println("Running time for each test: " + testTime + "ms");
		System.out.println();


		/*********************** Perform all the tests ****************************/

		ProverStateMachine theProverMachine;

		String mainLogFolder = System.currentTimeMillis() + "-Prover" + mctsType + "MCTSSpeedTest";
    	ThreadContext.put("LOG_FOLDER", mainLogFolder);

    	GamerLogger.startFileLogging();

		GamerLogger.log(FORMAT.CSV_FORMAT, mctsType + "MCTSSpeedTestTable", "Game key;#Roles;SM initialization time;Test Duration (ms);Search time(ms);Iterations;Visited Nodes;Iterations/second;Nodes/second;Playing role;Chosen move;Scores sum;Visits;AverageScore");

		GameRepository theRepository = GameRepository.getDefaultRepository();

	    //GameRepository theRepository = new ManualUpdateLocalGameRepository("GGPBase-GameRepo-03022016");

	    for(String gameKey : theRepository.getGameKeys()) {
	        if(gameKey.contains("laikLee")) continue;

	        //if(gameKey.equals("ticTacHeaven")) continue;

	        // TODO: change code so that if there is only one game to test we won't run through the whole sequence of keys.
	        if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

	        System.out.println("Detected activation in game " + gameKey + ".");

	        Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey));

	        ThreadContext.put("LOG_FOLDER", mainLogFolder + "/logs/" + fakeMatch.getMatchId());

	        GamerLogger.log(mctsType + "MCTSSpeedTest", "Testing on game " + gameKey);

	        List<Gdl> description = theRepository.getGame(gameKey).getRules();

		    theProverMachine = new ProverStateMachine();

		    int numRoles = -1;
	        long initializationTime;
	        long testDuration = -1L;
	        long searchTime = -1L;
	        int iterations = -1;
	        int visitedNodes = -1;
	        double iterationsPerSecond = -1;
	        double nodesPerSecond = -1;
	        Role playingRole = null;
	        Move chosenMove = null;
	        long scoresSum = -1L;
	        long visits = -1;
	        double averageScore = -1;

	        // Try to initialize the propnet state machine.
	        // If initialization fails, skip the test.
	    	long initStart = System.currentTimeMillis();
	        theProverMachine.initialize(description, initStart + givenInitTime);

			initializationTime = System.currentTimeMillis() - initStart;

			/***************************************/
			System.gc();
			/***************************************/

			Random r = new Random();
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





			playingRole = theProverMachine.getRoles().get(0);
			numRoles = theProverMachine.getRoles().size();

			ProverTreeNodeFactory theNodeFactory;

			switch(mctsType){
				/*case "SUCT":
					theNodeFactory = new PnSequentialTreeNodeFactory(thePropnetMachine, internalPlayingRole);
					break;
				case "SLOW_SUCT":
					theNodeFactory = new PnSlowSequentialTreeNodeFactory(thePropnetMachine, internalPlayingRole);
					break;*/
				default:
			    	theNodeFactory = new ProverDecoupledTreeNodeFactory(theProverMachine);
			    	break;
			}

			ProverMCTSManager MCTSmanager = new ProverMCTSManager(
					new ProverUCTSelection(numRoles, playingRole, r, uctOffset, new ProverUCTEvaluator(c, unexploredMoveDefaultSelectionValue)),
					new ProverRandomExpansion(numRoles, playingRole, r), new ProverRandomPlayout(theProverMachine),
					new ProverStandardBackpropagation(numRoles, playingRole),
					new ProverMaximumScoreChoice(0, r), null, null, theNodeFactory,
					theProverMachine, gameStepOffset, maxSearchDepth);

			try{
				GamerLogger.log(mctsType + "MCTSSpeedTest", "Starting search.");

				MCTSNode initialNode = MCTSmanager.search(theProverMachine.getInitialState(), System.currentTimeMillis() + testTime, gameStep);
				ProverCompleteMoveStats finalMove = MCTSmanager.getBestMove(initialNode);

				GamerLogger.log(mctsType + "MCTSSpeedTest", "Search ended correctly.");
				chosenMove = finalMove.getTheMove();
			    scoresSum = finalMove.getScoreSum();
			    visits = finalMove.getVisits();
			    if(visits != 0){
			    	averageScore = ((double) scoresSum) / ((double) visits);
			    }
			    iterations = MCTSmanager.getIterations();
				visitedNodes = MCTSmanager.getVisitedNodes();
				searchTime = MCTSmanager.getSearchTime();
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

	        ThreadContext.put("LOG_FOLDER", mainLogFolder);

	        GamerLogger.log(FORMAT.CSV_FORMAT, mctsType + "MCTSSpeedTestTable", gameKey + ";" + numRoles + ";" + initializationTime + ";" + testDuration + ";" + searchTime + ";" + iterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";" + playingRole + ";" + chosenMove + ";" + scoresSum + ";" + visits + ";" + averageScore + ";");

	        /***************************************/
	        System.gc();
	        GdlPool.drainPool();
	        /***************************************/

	    }
	}

}
