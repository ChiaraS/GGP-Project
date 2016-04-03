package csironi.ggp.course.MCTStest;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.gamer.statemachine.MCS.manager.prover.ProverCompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCS.manager.prover.ProverMCSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.ProverRandomPlayout;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.logging.GamerLogger.FORMAT;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

/**
* This class checks the speed (nodes/second, iterations/second) of the MCS player
* that uses the prover state machine.
*
* It is possible to specify the following combinations of main arguments:
*
* 1. [keyOfGameToTest]
* 2. [givenInitTime] [maximumTestDuration]
* 3. [givenInitTime] [maximumTestDuration] [keyOfGameToTest]
*
* where:
* [givenInitTime] = maximum time in milliseconds that should be spent to initialize the
* 					prover state machine (DEFAULT: 420000ms - 7mins).
* [maximumTestDuration] = duration of each test in millisecond (DEFAULT: 60000ms - 1min).
* [keyOfGameToTest] = key of the game to be tested (DEFAULT: null (i.e. all games)).
*
* If nothing or something inconsistent is specified for any of the parameters, the default
* value will be used.
*
* @author C.Sironi
*
*/
public class ProverMCSSpeedTest {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	public static void main(String[] args) {

		/*********************** Parse main arguments ****************************/

		long givenInitTime = 420000L;
		long testTime = 60000L;
		String gameToTest = null;

		if(args.length != 0){ // At least one argument is specified

			if (args.length <= 3){

				if(args.length == 3 || args.length == 1){
					gameToTest = args[args.length-1];
				}
				if(args.length == 3 || args.length == 2){
					try{
						givenInitTime = Long.parseLong(args[0]);
					}catch(NumberFormatException nfe){
						System.out.println("Inconsistent maximum initialization time specification! Using default value.");
						givenInitTime = 420000L;
					}
					try{
						testTime = Long.parseLong(args[1]);
					}catch(NumberFormatException nfe){
						System.out.println("Inconsistent test duration specification! Using default value.");
						testTime = 60000L;
					}
				}
			}else{ // Too many arguments, using default
				System.out.println("Inconsistent number of main arguments! Ignoring them.");
			}

		} // else use default values

		System.out.println("Testing speed of MCS using the prover state machine.");

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

		String generalLogFolder = System.currentTimeMillis() + "-ProverMCSSpeedTest";

    	ThreadContext.put("LOG_FOLDER", generalLogFolder);

    	GamerLogger.startFileLogging();

	    GamerLogger.log(FORMAT.CSV_FORMAT, "MCSSpeedTestTable", "Game key;#Roles;SM initialization time;Test Duration (ms);Search time(ms);Iterations;Visited Nodes;Iterations/second;Nodes/second;Playing role;Chosen move;Scores sum;Visits;AverageScore");

	    GameRepository theRepository = GameRepository.getDefaultRepository();

    	//GameRepository theRepository = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");

	    for(String gameKey : theRepository.getGameKeys()) {
	        if(gameKey.contains("laikLee")) continue;

	        //if(gameKey.equals("ticTacHeaven")) continue;

	        // TODO: change code so that if there is only one game to test we won't run through the whole sequence of keys.
	        if(gameToTest != null && !gameKey.equals(gameToTest)) continue;

	        System.out.println("Detected activation in game " + gameKey + ".");

	        Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey));

	        ThreadContext.put("LOG_FOLDER", generalLogFolder +  "/logs/" + fakeMatch.getMatchId());

	        GamerLogger.log("MCSSpeedTest", "Testing on game " + gameKey);

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

	        // Try to initialize the prover state machine.
	        // If initialization fails, skip the test.
	    	long initStart = System.currentTimeMillis();
	        theProverMachine.initialize(description, initStart + givenInitTime);

			initializationTime = System.currentTimeMillis() - initStart;

			/***************************************/
			System.gc();
			/***************************************/

			Random r = new Random();
			int maxSearchDepth = 500;

			long testStart = System.currentTimeMillis();

			GamerLogger.log("MCSSpeedTest", "Starting speed test.");

			playingRole = theProverMachine.getRoles().get(0);

			numRoles = theProverMachine.getRoles().size();

			ProverMCSManager MCSmanager = new ProverMCSManager(new ProverRandomPlayout(theProverMachine),
					theProverMachine, playingRole, maxSearchDepth, r);

			try{
				GamerLogger.log("MCSSpeedTest", "Starting search.");

				MCSmanager.search(theProverMachine.getInitialState(), System.currentTimeMillis() + testTime);
				ProverCompleteMoveStats finalMove = MCSmanager.getBestMove();

				GamerLogger.log("MCSSpeedTest", "Search ended correctly.");
				chosenMove = finalMove.getTheMove();
			    scoresSum = finalMove.getScoreSum();
			    visits = finalMove.getVisits();
			    if(visits != 0){
			    	averageScore = ((double) scoresSum) / ((double) visits);
			    }
			    iterations = MCSmanager.getIterations();
				visitedNodes = MCSmanager.getVisitedNodes();
				searchTime = MCSmanager.getSearchTime();
				if(searchTime != 0){
			    	iterationsPerSecond = ((double) iterations * 1000)/((double) searchTime);
			    	nodesPerSecond = ((double) visitedNodes * 1000)/((double) searchTime);
				}
			}catch(Exception e){
				GamerLogger.logError("MCSSpeedTest", "MCTS failed during execution. Impossible to continue the speed test. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
				GamerLogger.logStackTrace("MCSSpeedTest", e);
				System.out.println("Stopping test on game " + gameKey + ". Exception during search execution.");
			}

			testDuration = System.currentTimeMillis() - testStart;

	        GamerLogger.log(FORMAT.PLAIN_FORMAT, "MCSSpeedTest", "");

	    	ThreadContext.put("LOG_FOLDER", generalLogFolder);

	        GamerLogger.log(FORMAT.CSV_FORMAT, "MCSSpeedTestTable", gameKey + ";" + numRoles + ";" + initializationTime + ";" + testDuration + ";" + searchTime + ";" + iterations + ";" + visitedNodes + ";" + iterationsPerSecond + ";" + nodesPerSecond + ";" + playingRole + ";" + chosenMove + ";" + scoresSum + ";" + visits + ";" + averageScore + ";");

	        /***************************************/
	        System.gc();
	        GdlPool.drainPool();
	        /***************************************/

	    }
	}
}
