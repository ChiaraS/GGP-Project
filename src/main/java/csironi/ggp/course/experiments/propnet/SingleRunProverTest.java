package csironi.ggp.course.experiments.propnet;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MCSException;
import org.ggp.base.player.gamer.statemachine.MCS.manager.prover.ProverMCSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.strategies.playout.ProverRandomPlayout;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.cache.NoSyncRefactoredCachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

/**
 * Inputs this program gets:
 *
 * - NECESSARY INPUTS:
 * 		[mainLogFolder] = the folder containing the statistics file for the given game. In this folder this
 * 						  program will look for the .csv file where to save the statistics (in one line).
 * 		[testLogFolder] = the log folder where this program must save all its log files.
 * 		[gameKey] = the key of the game to test.
 *
 * - OPTIONAL INPUTS (NOTE: you can either specify all or none of them, but not only part of them.):
 * 		[givenInitTime] = the maximum time (in milliseconds) that the program has available to create and
 * 						  initialize the state machine. (Default value: 420000ms)
 * 		[searchTime] = the amount of time (in milliseconds) each search test (MCS and MCTS test) must last. Both
 * 					   the MCS and the MCTS search tests will run the search from the root node for (approximately)
 * 					   this amount of time. (Default value: 60000ms)
 *  	[withCache] = true if the state machine based on the prover must use the cache, false otherwise. (Default
 *  				  value: false)
 *  	[cacheType] = specifies which version of the cache to use. Possible types are:
 *  							- old: implementation that equals the one already provided in this code base
 *  							- nosync: refactored cache that should spend less time searching entries in the
 *  									  cache and doesn't synchronize code and is not thread safe
 *
 *
 * @author C.Sironi
 *
 */
public class SingleRunProverTest {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	public static void main(String[] args) {

		if(args.length < 3){
			System.out.println("[SingleRunProverTest] Impossible to run program. Specify at least a main log folder, the log folder for this run of the test and the key of the game to be tested.");
			return;
		}

    	String mainLogFolder = args[0];
    	String myLogFolder = args[1];
    	String gameKey = args[2];

    	ThreadContext.put("LOG_FOLDER", myLogFolder);

    	GamerLogger.startFileLogging();

    	GamerLogger.log("SingleRunProverTester", "Single run Prover test for game " + gameKey + ".");
    	GamerLogger.log("SingleRunProverTester", "Logging in folder " + myLogFolder + ".");

       	long givenInitTime = 420000L;
    	long searchTime = 60000L;
    	boolean withCache = false;
    	String cacheType = null;

    	if(args.length == 7){
	    	try{
				givenInitTime = Long.parseLong(args[3]);
			}catch(NumberFormatException nfe){
				GamerLogger.log("SingleRunProverTester", "Inconsistent maximum initialization time specification! Using default value.");
				givenInitTime = 420000L;
			}
			try{
				searchTime = Long.parseLong(args[4]);
			}catch(NumberFormatException nfe){
				GamerLogger.log("SingleRunProverTester", "Inconsistent search time specification! Using default value.");
				searchTime = 60000L;
			}

			withCache = Boolean.parseBoolean(args[5]);

			cacheType = args[6];

    	}

    	String testSettings = "Settings for current test run:\n";
    	testSettings += "[givenInitTime] = " + givenInitTime + "\n";
    	testSettings += "[searchTime] = " + searchTime + "\n";
    	testSettings += "[withCache] = " + withCache + "\n";
    	testSettings += "[cacheType] = " + cacheType + "\n";

    	GamerLogger.log("SingleRunProverTester", testSettings);

    	//GameRepository gameRepo = GameRepository.getDefaultRepository();

    	GameRepository gameRepo = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");

    	Game game = gameRepo.getGame(gameKey);

    	if(game == null){
    		GamerLogger.logError("SingleRunProverTester", "Impossible to test Prover for game " + gameKey + ": specified game not found in the repository.");
			return;
    	}

    	File theCSVFile = new File(mainLogFolder + "/ProverStatistics.csv");

    	if(!theCSVFile.exists()){
    		ThreadContext.put("LOG_FOLDER", mainLogFolder);
    		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ProverStatistics", "TotalStateMachineInitTime;MCSSearchDuration;MCSIterations;MCSVisitedNodes;MCSIterationsPerSecond;MCSNodesPerSecond;MCTSSearchDuration;MCTSIterations;MCTSVisitedNodes;MCTSIterationsPerSecond;MCTSNodesPerSecond;");
    		ThreadContext.put("LOG_FOLDER",  myLogFolder);
    	}

    	List<Gdl> description = game.getRules();

    	singleTestRun(mainLogFolder, myLogFolder, description, givenInitTime, searchTime, withCache, cacheType);

	}

	private static void singleTestRun(String mainLogFolder, String myLogFolder, List<Gdl> description, long givenInitTime, long searchTime, boolean withCache, String cacheType){

		GamerLogger.log("SingleRunProverTester", "Starting Prover test.");

		long totalStateMachineInitTime = -1;

		long mcsSearchDuration = -1;
        int mcsIterations = -1;
        int mcsVisitedNodes = -1;
        double mcsIterationsPerSecond = -1;
        double mcsNodesPerSecond = -1;

        long mctsSearchDuration = -1;
        int mctsIterations = -1;
        int mctsVisitedNodes = -1;
        double mctsIterationsPerSecond = -1;
        double mctsNodesPerSecond = -1;

		/******************************** MCS SPEED TEST *********************************/

		StateMachine theProverMachine;

	    if(withCache){

	    	switch(cacheType){
	    	case "nosync":
	    		theProverMachine = new NoSyncRefactoredCachedStateMachine(new ProverStateMachine());
	    		break;
	    	default:
	    		theProverMachine = new CachedStateMachine(new ProverStateMachine());
	    	}
        }else{
        	theProverMachine = new ProverStateMachine();
        }

		Random r;
		int maxSearchDepth;
		int numRoles;

		GamerLogger.log("SingleRunProverTester", "Starting MCS speed test.");

		try {
			long start = System.currentTimeMillis();

			theProverMachine.initialize(description, System.currentTimeMillis() + givenInitTime);

			totalStateMachineInitTime = System.currentTimeMillis() - start;

		    r = new Random();
		    maxSearchDepth = 500;
		    ExplicitRole playingRole = theProverMachine.getRoles().get(0);
		    numRoles = theProverMachine.getRoles().size();

		    ProverMCSManager MCSmanager = new ProverMCSManager(new ProverRandomPlayout(theProverMachine),
		    		theProverMachine, playingRole, maxSearchDepth, r);

		    GamerLogger.log("SingleRunProverTester", "Starting MCS search.");

		    MCSmanager.search(theProverMachine.getInitialState(), System.currentTimeMillis() + searchTime);

		    GamerLogger.log("SingleRunProverTester", "MCS search ended correctly.");

		   	mcsSearchDuration = MCSmanager.getSearchTime();
		   	mcsIterations = MCSmanager.getIterations();
		    mcsVisitedNodes = MCSmanager.getVisitedNodes();

		    if(mcsSearchDuration != 0){
		    	mcsIterationsPerSecond = ((double) mcsIterations * 1000)/((double) mcsSearchDuration);
			    mcsNodesPerSecond = ((double) mcsVisitedNodes * 1000)/((double) mcsSearchDuration);
		    }

		    GamerLogger.log("SingleRunProverTester", "MCS speed test successful.");

		} catch (StateMachineInitializationException e) {
	       	GamerLogger.logError("SingleRunProverTester", "State machine " + theProverMachine.getName() + " initialization failed, impossible to test MCS for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	       	GamerLogger.logStackTrace("SingleRunProverTester", e);
		} catch (MCSException e) {
	       	GamerLogger.logError("SingleRunProverTester", "Search failed for MCSManager. Impossible to test MCS for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	       	GamerLogger.logStackTrace("SingleRunProverTester", e);
		}

		collect(); // TODO: Leave or not?

		/******************************** MCTS SPEED TEST *********************************/

/*		GamerLogger.log("SingleRunProverTester", "Starting MCTS speed test.");

		if(withCache){
			theProverMachine = new CachedStateMachine(new ProverStateMachine());
	    }else{
	      	theProverMachine = new ProverStateMachine();
	    }

		try {
			theProverMachine.initialize(description, System.currentTimeMillis() + givenInitTime);

		    r = new Random();
		    maxSearchDepth = 500;
		    double c = 0.7;
		    double unexploredMoveDefaultSelectionValue = Double.MAX_VALUE;
		    double uctOffset = 0.01;
		    int gameStep = 1;
		    int gameStepOffset = 2;

		    Role playingRole = theProverMachine.getRoles().get(0);
		    numRoles = theProverMachine.getRoles().size();

		    ProverTreeNodeFactory theNodeFactory = new ProverDecoupledTreeNodeFactory(theProverMachine);

		    ProverMCTSManager MCTSmanager = new ProverMCTSManager(
		    		new ProverUCTSelection(numRoles, playingRole, r, uctOffset, new ProverUCTEvaluator(c, unexploredMoveDefaultSelectionValue)),
		        	new ProverRandomExpansion(numRoles, playingRole, r), new ProverRandomPlayout(theProverMachine),
		        	new ProverStandardBackpropagation(numRoles, playingRole),
		        	new ProverMaximumScoreChoice(0, r), null, null, theNodeFactory,
		        	theProverMachine, gameStepOffset, maxSearchDepth);

		    GamerLogger.log("SingleRunProverTester", "Starting MCTS search.");

	        MCTSmanager.search(theProverMachine.getInitialState(), System.currentTimeMillis() + searchTime, gameStep);

	        GamerLogger.log("SingleRunProverTester", "MCTS search ended correctly.");

	        mctsSearchDuration = MCTSmanager.getSearchTime();
		    mctsIterations = MCTSmanager.getIterations();
		    mctsVisitedNodes = MCTSmanager.getVisitedNodes();

		    if(mctsSearchDuration != 0){
		       	mctsIterationsPerSecond = ((double) mctsIterations * 1000)/((double) mctsSearchDuration);
			    mctsNodesPerSecond = ((double) mctsVisitedNodes * 1000)/((double) mctsSearchDuration);
		    }

		    GamerLogger.log("SingleRunProverTester", "MCTS speed test successful.");

		} catch (StateMachineInitializationException e) {
	       	GamerLogger.logError("SingleRunProverTester", "State machine " + theProverMachine.getName() + " initialization failed, impossible to test MCTS for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	       	GamerLogger.logStackTrace("SingleRunProverTester", e);
		} catch (MCTSException e) {
	        	GamerLogger.logError("SingleRunPNTester", "Search failed for MCTSManager. Impossible to test MCTS for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	        	GamerLogger.logStackTrace("SingleRunPNTester", e);
		}
		*/

		/************************** LOG *******************************/

		ThreadContext.put("LOG_FOLDER", mainLogFolder);

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ProverStatistics", totalStateMachineInitTime + ";" +
				mcsSearchDuration + ";" + mcsIterations + ";" +
				mcsVisitedNodes + ";" + mcsIterationsPerSecond + ";" + mcsNodesPerSecond + ";" +
				mctsSearchDuration + ";" + mctsIterations + ";" + mctsVisitedNodes + ";" +
				mctsIterationsPerSecond + ";" + mctsNodesPerSecond + ";");

		ThreadContext.put("LOG_FOLDER",  myLogFolder);

	}

	private static void collect(){
		long endGCTime = System.currentTimeMillis() + 5000;
	    for (int ii = 0; ii < 1000 && System.currentTimeMillis() < endGCTime; ii++){

	    	//System.out.println("Calling GC: " + System.currentTimeMillis());

	    	System.gc();
	        try {Thread.sleep(1);} catch (InterruptedException lEx) {/* Whatever */}
	    }
	}


}
