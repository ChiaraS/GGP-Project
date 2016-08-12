package csironi.ggp.course.experiments.propnet;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MCSException;
import org.ggp.base.player.gamer.statemachine.MCS.manager.propnet.InternalPropnetMCSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.RandomPlayout;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.DynamicPropNet;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.creationManager.PropNetManagerRunner;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetManager;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.OptimizationCaller;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.OptimizeAwayConstantValueComponents;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.OptimizeAwayConstants;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.RemoveAnonPropositions;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.RemoveDuplicateGates;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.RemoveOutputlessComponents;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.cache.NoSyncRefactoredSeparateInternalPropnetCachedStateMachine;
import org.ggp.base.util.statemachine.cache.RefactoredSeparateInternalPropnetCachedStateMachine;
import org.ggp.base.util.statemachine.cache.SeparateInternalPropnetCachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

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
 * 		[givenInitTime] = the maximum time (in milliseconds) that the program has available to create the PropNet
 * 						  and perform the optimizations on it (if any is specified).
 * 						  This time is also used as limit for the state machine to initialize, but it isn't really
 * 						  taken into account since the state machine based on the PropNet receives the PropNet
 * 						  from outside only once it has been already created. (Default value: 420000ms)
 * 		[searchTime] = the amount of time (in milliseconds) each search test (MCS and MCTS test) must last. Both
 * 					   the MCS and the MCTS search tests will run the search from the root node for (approximately)
 * 					   this amount of time. (Default value: 60000ms)
 *  	[optimizations] = the optimizations that the PropNet manager must perform on the PropNet after creation.
 *  					  Each optimization corresponds to a number as follows:
 *  					  	0 = OptimizeAwayConstants
 *  						1 = RemoveAnonPropositions
 *  						2 = OptimizeAwayConstantValueComponents
 *  						3 = RemoveOutputlessComponents
 *  					  The optimizations to be performed must be specified with their corresponding numbers,
 *  					  separated by "-", in the order we want the manager to perform them (e.g. the input "0-1-2-3"
 *  					  will make the manager perform optimization 0, followed by optimization 1, followed by
 *  					  optimization 2, followed by optimization 3). To let the manager perform no optimizations
 *  					  give the string "none" as argument, if you want to use the default optimizations give the
 *  					  string "default" as input. (Default value: "none")
 *  	[withCache] = true if the state machine based on the propnet must use the cache, false otherwise. (Default
 *  				  value: false)
 *  	[cacheType] = specifies which version of the cache to use. Possible types are:
 *  							- old: implementation that equals the one already provided in this code base
 *  							- ref: refactored cache that should spend less time searching entries in the cache
 *  							- nosync: same as ref but doesn't synchronize code and is not thread safe
 *
 *
 * @author C.Sironi
 *
 */
public class SingleRunPNTest {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	public static void main(String[] args) {

		if(args.length < 3){
			System.out.println("[SingleRunPNTest] Impossible to run program. Specify at least a main log folder, the log folder for this run of the test and the key of the game to be tested.");
			return;
		}

    	String mainLogFolder = args[0];
    	String myLogFolder = args[1];
    	String gameKey = args[2];

    	ThreadContext.put("LOG_FOLDER", myLogFolder);

    	GamerLogger.startFileLogging();

    	GamerLogger.log("SingleRunPNTester", "Single run PropNet test for game " + gameKey + ".");
    	GamerLogger.log("SingleRunPNTester", "Logging in folder " + myLogFolder + ".");

       	long givenInitTime = 420000L;
    	long searchTime = 60000L;
    	String optimizationsString = "none";
    	OptimizationCaller[] optimizations = new OptimizationCaller[0];
    	boolean withCache = false;
    	String cacheType = null;

    	if(args.length == 8){
	    	try{
				givenInitTime = Long.parseLong(args[3]);
			}catch(NumberFormatException nfe){
				GamerLogger.log("SingleRunPNTester", "Inconsistent maximum initialization time specification! Using default value.");
				givenInitTime = 420000L;
			}
			try{
				searchTime = Long.parseLong(args[4]);
			}catch(NumberFormatException nfe){
				GamerLogger.log("SingleRunPNTester", "Inconsistent search time specification! Using default value.");
				searchTime = 60000L;
			}

			optimizationsString = args[5];
			try{
				optimizations = parseOptimizations(optimizationsString);
			}catch(IllegalArgumentException e){
				GamerLogger.log("SingleRunPNTester", "Inconsistent specification of the PropNet optimizations. Using default value!");
				optimizationsString = "none";
		    	optimizations = new OptimizationCaller[0];
			}

			withCache = Boolean.parseBoolean(args[6]);

			cacheType = args[7];

    	}

    	String testSettings = "Settings for current test run:\n";
    	testSettings += "[givenInitTime] = " + givenInitTime + "\n";
    	testSettings += "[searchTime] = " + searchTime + "\n";
    	testSettings += "[optimizations] = " + optimizationsString + "\n";
    	testSettings += "[withCache] = " + withCache + "\n";
    	testSettings += "[cacheType] = " + cacheType + "\n";

    	GamerLogger.log("SingleRunPNTester", testSettings);

    	//GameRepository gameRepo = GameRepository.getDefaultRepository();

		// WINDOWS
		//GameRepository gameRepo = new ManualUpdateLocalGameRepository("C:/Users/c.sironi/BITBUCKET REPOS/GGP-Base/GGPBase-GameRepo-03022016");

		// LINUX
    	GameRepository gameRepo = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");

    	Game game = gameRepo.getGame(gameKey);

    	if(game == null){
    		GamerLogger.logError("SingleRunPNTester", "Impossible to test PropNet for game " + gameKey + ": specified game not found in the repository.");
			return;
    	}

    	File theCSVFile = new File(mainLogFolder + "/PropnetStatistics.csv");

    	File theComponentsFile = new File(mainLogFolder + "/ComponentsStatistics.csv");

    	if(!theCSVFile.exists()){
    		ThreadContext.put("LOG_FOLDER", mainLogFolder);
    		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "PropnetStatistics", "PropnetBuildingTime;TotalPropnetInitTime;NumComponents;NumLinks;NumConstants;NumAnds;NumOrs;NumNots;NumPropositions;NumInits;NumGoals;NumTerminals;NumInputs;NumLegals;NumOthers;NumBases;NumTransitions;MCSSearchDuration;MCSIterations;MCSVisitedNodes;MCSIterationsPerSecond;MCSNodesPerSecond;MCTSSearchDuration;MCTSIterations;MCTSVisitedNodes;MCTSIterationsPerSecond;MCTSNodesPerSecond;");
    		ThreadContext.put("LOG_FOLDER",  myLogFolder);
    	}

    	if(!theComponentsFile.exists()){
    		ThreadContext.put("LOG_FOLDER", mainLogFolder);

    		String csvOptimizations = "InitComponents;";

    		if(optimizations != null){
	    		for(OptimizationCaller c : optimizations){
	    			csvOptimizations += c.getClass().getSimpleName() + ";";
	    		}
    		}

    		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ComponentsStatistics", csvOptimizations);
    		ThreadContext.put("LOG_FOLDER",  myLogFolder);
    	}

    	List<Gdl> description = game.getRules();

    	singleTestRun(mainLogFolder, myLogFolder, description, givenInitTime, searchTime, optimizations, withCache, cacheType);

	}

	private static OptimizationCaller[] parseOptimizations(String opts){

		if(opts.equalsIgnoreCase("none")){
			return new OptimizationCaller[0];
		}

		if(opts.equalsIgnoreCase("default")){
			return null;
		}

		String[] splitOpts = opts.split("-");

		if(splitOpts.length < 1){
			throw new IllegalArgumentException();
		}

		OptimizationCaller[] optimizations = new OptimizationCaller[splitOpts.length];

		for(int i = 0; i < splitOpts.length; i++){
			switch(splitOpts[i]){
				case "0":
					optimizations[i] = new OptimizeAwayConstants();
					break;
				case "1":
					optimizations[i] = new RemoveAnonPropositions();
					break;
				case "2":
					optimizations[i] = new OptimizeAwayConstantValueComponents();
					break;
				case "3":
					optimizations[i] = new RemoveOutputlessComponents();
					break;
				case "4":
					optimizations[i] = new RemoveDuplicateGates();
					break;
				default:
					throw new IllegalArgumentException();
			}
		}

		return optimizations;
	}

	private static void singleTestRun(String mainLogFolder, String myLogFolder, List<Gdl> description, long givenInitTime, long searchTime, OptimizationCaller[] optimizations, boolean withCache, String cacheType){

		GamerLogger.log("SingleRunPNTester", "Starting PropNet test.");

		long propnetBuildingTime = -1; //Only creation and initialization of the object
		long totalPropnetInitTime = -1; // Creation, initialization of the object eventual optimizations and initialization of the state.

		int numComponents = -1;
		int numLinks = -1;
		int numConstants = -1;
		int numAnds = -1;
		int numOrs = -1;
		int numNots = -1;
		int numPropositions = -1;
		int numInits = -1;
		int numGoals = -1;
		int numTerminals = -1;
		int numInputs = -1;
		int numLegals = -1;
		int numOthers = -1;
		int numBases = -1;
		int numTransitions = -1;

		int[] componentsStats = null;

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

        /*************************** PROPNET CREATION **********************************/

		GamerLogger.log("SingleRunPNTester", "Creating the propnet.");

		SeparateInternalPropnetManager manager =  new SeparateInternalPropnetManager(description, System.currentTimeMillis() + givenInitTime, optimizations);

		PropNetManagerRunner.runPropNetManager(manager, givenInitTime);

		DynamicPropNet dynamicPropnet = manager.getDynamicPropnet();
		ImmutablePropNet immutablePropnet = manager.getImmutablePropnet();
		ImmutableSeparatePropnetState propnetState = manager.getInitialPropnetState();

		if(dynamicPropnet != null && immutablePropnet != null && propnetState != null){

			GamerLogger.log("SingleRunPNTester", "Propnet creation successful.");

			propnetBuildingTime = manager.getPropnetConstructionTime();
			totalPropnetInitTime = manager.getTotalInitTime();

			numComponents = dynamicPropnet.getSize();
			numLinks = dynamicPropnet.getNumLinks();
			numConstants = dynamicPropnet.getNumConstants();
			numAnds = dynamicPropnet.getNumAnds();
			numOrs = dynamicPropnet.getNumOrs();
			numNots = dynamicPropnet.getNumNots();
			numPropositions = dynamicPropnet.getNumPropositions();
			numInits = dynamicPropnet.getNumInits();
			numGoals = dynamicPropnet.getNumGoals();
			numTerminals = dynamicPropnet.getNumTerminals();
			numInputs = dynamicPropnet.getNumInputs();
			numLegals = dynamicPropnet.getNumLegals();
			numOthers = dynamicPropnet.getNumOthers();
			numBases = dynamicPropnet.getNumBases();
			numTransitions = dynamicPropnet.getNumTransitions();

			componentsStats = manager.getComponentsStats();

			collect(); // TODO: Leave or not?

			/******************************** MCS SPEED TEST *********************************/

			// Create the state machine giving it the propnet and the propnet state.
			// NOTE that if any of the two is null, it means that the propnet creation/initialization went wrong
			// and this will be detected by the state machine during initialization.

			InternalPropnetStateMachine thePropnetMachine;

		    if(withCache){

		    	switch(cacheType){
		    	case "ref":
		    		thePropnetMachine = new RefactoredSeparateInternalPropnetCachedStateMachine(new SeparateInternalPropnetStateMachine(immutablePropnet, propnetState));
		    		break;
		    	case "nosync":
		    		thePropnetMachine = new NoSyncRefactoredSeparateInternalPropnetCachedStateMachine(new SeparateInternalPropnetStateMachine(immutablePropnet, propnetState));
		    		break;
		    	default:
		    		thePropnetMachine = new SeparateInternalPropnetCachedStateMachine(new SeparateInternalPropnetStateMachine(immutablePropnet, propnetState));
		    	}
		    }else{
	        	thePropnetMachine = new SeparateInternalPropnetStateMachine(immutablePropnet, propnetState);
	        }

		    GamerLogger.log("SingleRunPNTester", "Testing machine: " + thePropnetMachine.getClass().getSimpleName());

			Random r;
			int maxSearchDepth;
		    int numRoles;

			GamerLogger.log("SingleRunPNTester", "Starting MCS speed test.");

			try {
				thePropnetMachine.initialize(description, System.currentTimeMillis() + givenInitTime);

		        r = new Random();
		        maxSearchDepth = 500;
		        InternalPropnetRole internalPlayingRole = thePropnetMachine.getInternalRoles()[0];
		        numRoles = thePropnetMachine.getInternalRoles().length;

		        InternalPropnetMCSManager MCSmanager = new InternalPropnetMCSManager(new RandomPlayout(thePropnetMachine),
		        		thePropnetMachine, internalPlayingRole, maxSearchDepth, r);

		        GamerLogger.log("SingleRunPNTester", "Starting MCS search.");

		       	MCSmanager.search(thePropnetMachine.getInternalInitialState(), System.currentTimeMillis() + searchTime);

		       	GamerLogger.log("SingleRunPNTester", "MCS search ended correctly.");

		       	mcsSearchDuration = MCSmanager.getSearchTime();
		       	mcsIterations = MCSmanager.getIterations();
		        mcsVisitedNodes = MCSmanager.getVisitedNodes();

		        if(mcsSearchDuration != 0){
		        	mcsIterationsPerSecond = ((double) mcsIterations * 1000)/((double) mcsSearchDuration);
			        mcsNodesPerSecond = ((double) mcsVisitedNodes * 1000)/((double) mcsSearchDuration);
		        }

		        GamerLogger.log("SingleRunPNTester", "MCS speed test successful.");

			} catch (StateMachineInitializationException e) {
	        	GamerLogger.logError("SingleRunPNTester", "State machine " + thePropnetMachine.getName() + " initialization failed, impossible to test MCS for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	        	GamerLogger.logStackTrace("SingleRunPNTester", e);
			} catch (MCSException e) {
	        	GamerLogger.logError("SingleRunPNTester", "Search failed for MCSManager. Impossible to test MCS for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	        	GamerLogger.logStackTrace("SingleRunPNTester", e);
			}

			propnetState = null;
			thePropnetMachine = null;

			collect(); // TODO: Leave or not?

			/******************************** MCTS SPEED TEST *********************************/

/*			GamerLogger.log("SingleRunPNTester", "Starting MCTS speed test.");

			propnetState = manager.getInitialPropnetState();

		    if(withCache){
		    	thePropnetMachine = new SeparateInternalPropnetCachedStateMachine(new SeparateInternalPropnetStateMachine(immutablePropnet, propnetState));
	        }else{
	        	thePropnetMachine = new SeparateInternalPropnetStateMachine(immutablePropnet, propnetState);
	        }

			try {
				thePropnetMachine.initialize(description, System.currentTimeMillis() + givenInitTime);

		        r = new Random();
		        maxSearchDepth = 500;
		        double c = 0.7;
		        double unexploredMoveDefaultSelectionValue = Double.MAX_VALUE;
		        double uctOffset = 0.01;
		        int gameStep = 1;
		        int gameStepOffset = 2;

		        InternalPropnetRole internalPlayingRole = thePropnetMachine.getInternalRoles()[0];
		        numRoles = thePropnetMachine.getInternalRoles().length;

		        TreeNodeFactory theNodeFactory = new PnDecoupledTreeNodeFactory(thePropnetMachine);

		        InternalPropnetMCTSManager MCTSmanager = new InternalPropnetMCTSManager(
		        		new UCTSelection(numRoles, internalPlayingRole, r, uctOffset, new UCTEvaluator(c, unexploredMoveDefaultSelectionValue)),
		        		new RandomExpansion(numRoles, internalPlayingRole, r), new RandomPlayout(thePropnetMachine),
		        		new StandardBackpropagation(numRoles, internalPlayingRole),
		        		new MaximumScoreChoice(internalPlayingRole, r), null, null, theNodeFactory,
		        		thePropnetMachine, gameStepOffset, maxSearchDepth, false);

		        GamerLogger.log("SingleRunPNTester", "Starting MCTS search.");

	        	MCTSmanager.search(thePropnetMachine.getInternalInitialState(), System.currentTimeMillis() + searchTime, gameStep);

	        	GamerLogger.log("SingleRunPNTester", "MCTS search ended correctly.");

	        	mctsSearchDuration = MCTSmanager.getSearchTime();
		       	mctsIterations = MCTSmanager.getIterations();
		        mctsVisitedNodes = MCTSmanager.getVisitedNodes();

		        if(mctsSearchDuration != 0){
		        	mctsIterationsPerSecond = ((double) mctsIterations * 1000)/((double) mctsSearchDuration);
			        mctsNodesPerSecond = ((double) mctsVisitedNodes * 1000)/((double) mctsSearchDuration);
		        }

		        GamerLogger.log("SingleRunPNTester", "MCTS speed test successful.");

			} catch (StateMachineInitializationException e) {
	        	GamerLogger.logError("SingleRunPNTester", "State machine " + thePropnetMachine.getName() + " initialization failed, impossible to test MCTS for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	        	GamerLogger.logStackTrace("SingleRunPNTester", e);
			} catch (MCTSException e) {
	        	GamerLogger.logError("SingleRunPNTester", "Search failed for MCTSManager. Impossible to test MCTS for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	        	GamerLogger.logStackTrace("SingleRunPNTester", e);
			}
*/
		}

		/************************** LOG *******************************/

		ThreadContext.put("LOG_FOLDER", mainLogFolder);

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "PropnetStatistics", propnetBuildingTime + ";" +
				totalPropnetInitTime + ";" + numComponents + ";" + numLinks + ";" + numConstants + ";" +
				numAnds + ";" + numOrs + ";" + numNots + ";" + numPropositions + ";" + numInits + ";" +
				numGoals + ";" + numTerminals + ";" + numInputs + ";" + numLegals + ";" + numOthers + ";" +
				numBases + ";" + numTransitions + ";" + mcsSearchDuration + ";" + mcsIterations + ";" +
				mcsVisitedNodes + ";" + mcsIterationsPerSecond + ";" + mcsNodesPerSecond + ";" +
				mctsSearchDuration + ";" + mctsIterations + ";" + mctsVisitedNodes + ";" +
				mctsIterationsPerSecond + ";" + mctsNodesPerSecond + ";");

		String componentsNumber = "";

		for(int i = 0; i < componentsStats.length; i++){
			componentsNumber += componentsStats[i] + ";";
		}

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ComponentsStatistics", componentsNumber);

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
