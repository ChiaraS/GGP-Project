package csironi.ggp.course.experiments.propnet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MCSException;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.HybridMCSManager;
import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.LocalFolderGameRepository;
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
import org.ggp.base.util.propnet.creationManager.optimizationcallers.SimplifyLogicGates;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.abstractsm.CompactStateMachine;
import org.ggp.base.util.statemachine.cache.NoSyncRefactoredSeparateInternalPropnetCachedStateMachine;
import org.ggp.base.util.statemachine.cache.RefactoredSeparateInternalPropnetCachedStateMachine;
import org.ggp.base.util.statemachine.cache.SeparateInternalPropnetCachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

/**
 * Inputs this program gets:
 *
 * - NECESSARY INPUTS:
 * 		[mainLogFolder] = the folder containing the statistics file for the given game. In this folder this
 * 						  program will look for the .csv file where to save the statistics (in one line).
 * 		[testLogFolder] = the log folder where this program must save all its log files.
 * 		[gameKey] = the key of the game to test.
 * 		[myRoleIndex] = the index of the role that should be considered as the playing role.
 *
 * - OPTIONAL INPUTS (NOTE: you can either specify all or none of them, but not only part of them.):
 * 		[givenInitTime] = the maximum time (in milliseconds) that the program has available to create the PropNet
 * 						  and perform the optimizations on it (if any is specified).
 * 						  This time is also used as limit for the state machine to initialize, but it isn't really
 * 						  taken into account since the state machine based on the PropNet receives the PropNet
 * 						  from outside only once it has been already created. (Default value: 420000ms)
 * 		[searchBudget] = the positive search budget that each search test (Random, MCS and MCTS test) has available to run.
 * 						 It must be specified as an integer number (e.g. 60000) + measure unit (ms|sim).
 * 						 Following there is an example:
 * 							- 60000ms: if it is a time budget (note that in this case the budget is used approximately
 * 									   and the tests won't run exactly 60000ms)
 * 							- 60000sim: if it is a simulations budget
 * 						 Note that the budget is not split among the search tests, but each of the tests will use the
 * 						 complete budget. (Default value: 60000ms|60000sim, if the number format is wrong, but the ending
 * 						 can be parsed to "ms" or "sim", respectively. If the ending cannot be parsed correctly, or no
 * 						 value is specified the default value is: 60000ms)
 *  	[optimizations] = the optimizations that the PropNet manager must perform on the PropNet after creation.
 *  					  Each optimization corresponds to a number as follows:
 *  					  	0 = OptimizeAwayConstants
 *  						1 = RemoveAnonPropositions
 *  						2 = OptimizeAwayConstantValueComponents
 *  						3 = RemoveOutputlessComponents
 *  						4 = RemoveDuplicateGates
 *  						5 = SimplifyLogicGates
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
 *  				  (Default value: nosync)
 *  	[randomSearchManagerSettingsFile] = name of the .properties file that specifies the settings for the random
 *  								   		manager used in the experiment (Default value: RandomManager.properties).
 *  								   		McsManager.properties, MctsManager.properties).
 *  	[mcsSearchManagerSettingsFile] = name of the .properties file that specifies the settings for the MCS
 *  								   	 manager used in the experiment (Default value: McsManager.properties).
 *  	[mctsSearchManagerSettingsFile] = name of the .properties file that specifies the settings for the MCTS
 *  								   	  manager used in the experiment (Default value: MctsManager.properties).
 *
 * @author C.Sironi
 *
 */
public class SingleRunPNTest {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	public static void main(String[] args) {

		if(args.length < 4){
			System.out.println("[SingleRunPNTest] Impossible to run program. Specify at least a main log folder, the log folder for this run of the test, the key of the game to be tested and the index of the playing role.");
			return;
		}

    	String mainLogFolder = args[0];
    	String myLogFolder = args[1];
    	String gameKey = args[2];
    	int myRoleIndex = -1;
    	try{
    		myRoleIndex = Integer.parseInt(args[3]);
		}catch(NumberFormatException nfe){
			System.out.println("[SingleRunPNTest] Wrong specification of the index of the playing role.");
			return;
		}

    	ThreadContext.put("LOG_FOLDER", myLogFolder);

    	GamerLogger.startFileLogging();

    	GamerLogger.log("SingleRunPNTester", "Single run PropNet test for game " + gameKey + ".");
    	GamerLogger.log("SingleRunPNTester", "Logging in folder " + myLogFolder + ".");

       	long givenInitTime = 420000L;
    	long timeBudget = 60000L;
    	int simBudget = -1;
    	String optimizationsString = "none";
    	OptimizationCaller[] optimizations = new OptimizationCaller[0];
    	boolean withCache = false;
    	String cacheType = null;
    	File randomSearchManagerSettingsFile = new File(GamerConfiguration.gamersSettingsFolderPath + "/RandomManager.properties");
    	File mcsSearchManagerSettingsFile = new File(GamerConfiguration.gamersSettingsFolderPath + "/McsManager.properties");
    	File mctsSearchManagerSettingsFile = new File(GamerConfiguration.gamersSettingsFolderPath + "/MctsManager.properties");

    	if(args.length == 12){
	    	try{
				givenInitTime = Long.parseLong(args[4]);
			}catch(NumberFormatException nfe){
				GamerLogger.log("SingleRunPNTester", "Inconsistent maximum initialization time specification! Using default value.");
				givenInitTime = 420000L;
			}

	    	if(args[5].endsWith("ms")) { // We have a time budget
				try{
					timeBudget = Long.parseLong(args[5]);
				}catch(NumberFormatException nfe){
					GamerLogger.log("SingleRunPNTester", "Inconsistent time budget specification! Using default value.");
					timeBudget = 60000L;
				}
				if(timeBudget <= 0) {
					GamerLogger.log("SingleRunPNTester", "Non-positive time budget specification! Using default value.");
					timeBudget = 60000L;
				}
				simBudget = -1;
	    	}else if(args[5].endsWith("sim")) { // We have a simulation budget
	    		try{
	    			simBudget = Integer.parseInt(args[5]);
				}catch(NumberFormatException nfe){
					GamerLogger.log("SingleRunPNTester", "Inconsistent simulation budget specification! Using default value.");
					simBudget = 60000;
				}
	    		if(simBudget <= 0) {
					GamerLogger.log("SingleRunPNTester", "Non-positive simulation budget specification! Using default value.");
					simBudget = 60000;
				}
	    		timeBudget = -1;
	    	}else {
	    		timeBudget = 60000L;
	        	simBudget = -1;
	    	}

			optimizationsString = args[6];
			try{
				optimizations = parseOptimizations(optimizationsString);
			}catch(IllegalArgumentException e){
				GamerLogger.log("SingleRunPNTester", "Inconsistent specification of the PropNet optimizations. Using default value!");
				optimizationsString = "none";
		    	optimizations = new OptimizationCaller[0];
			}

			withCache = Boolean.parseBoolean(args[7]);

			cacheType = args[8];

			randomSearchManagerSettingsFile = new File(GamerConfiguration.gamersSettingsFolderPath + "/" + args[9]);
	    	mcsSearchManagerSettingsFile = new File(GamerConfiguration.gamersSettingsFolderPath + "/" + args[10]);
	    	mctsSearchManagerSettingsFile = new File(GamerConfiguration.gamersSettingsFolderPath + "/" + args[11]);

	    	if(!randomSearchManagerSettingsFile.exists()) {
	    		GamerLogger.log("SingleRunPNTester", "Cannot find property file for RandomSearchManager: " + randomSearchManagerSettingsFile.getPath());
	    		return;
	    	}
	    	if(!mcsSearchManagerSettingsFile.exists()) {
	    		GamerLogger.log("SingleRunPNTester", "Cannot find property file for McsSearchManager: " + mcsSearchManagerSettingsFile.getPath());
	    		return;
	    	}
	    	if(!mctsSearchManagerSettingsFile.exists()) {
	    		GamerLogger.log("SingleRunPNTester", "Cannot find property file for MctsSearchManager: " + mctsSearchManagerSettingsFile.getPath());
	    		return;
	    	}

    	}

    	String testSettings = "Settings for current test run:\n";
    	testSettings += "[gameKey] = " + gameKey + "\n";
    	testSettings += "[myRoleIndex] = " + myRoleIndex + "\n";
    	testSettings += "[givenInitTime] = " + givenInitTime + "\n";
    	testSettings += "[searchBudget] = " + (timeBudget > 0 ? (timeBudget + "ms") : (simBudget + "sim")) + "\n";
    	testSettings += "[optimizations] = " + optimizationsString + "\n";
    	testSettings += "[withCache] = " + withCache + "\n";
    	testSettings += "[cacheType] = " + cacheType + "\n";
    	testSettings += "[randomSearchManagerSettingsFilePath] = " + randomSearchManagerSettingsFile.getPath();
    	testSettings += "[mcsSearchManagerSettingsFilePath] = " + mcsSearchManagerSettingsFile.getPath();
    	testSettings += "[mctsSearchManagerSettingsFilePath] = " + mctsSearchManagerSettingsFile.getPath();

    	GamerLogger.log("SingleRunPNTester", testSettings);

    	//GameRepository gameRepo = GameRepository.getDefaultRepository();

		// WINDOWS
		//GameRepository gameRepo = new ManualUpdateLocalGameRepository("C:/Users/c.sironi/BITBUCKET REPOS/GGP-Base/GGPBase-GameRepo-03022016");

		// LINUX
    	//GameRepository gameRepo = new ManualUpdateLocalGameRepository(GamerConfiguration.defaultLocalGameRepositoryFolderPath);

    	GameRepository gameRepo = new LocalFolderGameRepository(GamerConfiguration.defaultLocalFolderGameRepositoryFolderPath);

    	Game game = gameRepo.getGame(gameKey);

    	if(game == null){
    		GamerLogger.logError("SingleRunPNTester", "Impossible to test PropNet for game " + gameKey + ": specified game not found in the repository.");
			return;
    	}

    	int numRoles = ExplicitRole.computeRoles(game.getRules()).size();

    	if(myRoleIndex >= numRoles) {
    		GamerLogger.logError("SingleRunPNTester", "Impossible to test PropNet for game " + gameKey +
    				": cannot use role at index " + myRoleIndex + " when there are only " + numRoles + " roles in the game.");
			return;
    	}

    	File theCSVFile = new File(mainLogFolder + "/PropnetStatistics.csv");

    	File theComponentsFile = new File(mainLogFolder + "/ComponentsStatistics.csv");

    	if(!theCSVFile.exists()){

    		String randomRolesHeader = "";
    		String mcsRolesHeader = "";
    		String mctsRolesHeader = "";
    		for(int i = 0; i < numRoles; i++) {
    			randomRolesHeader += ("RNDRole" + (i+1) + "Name;RNDRole" + (i+1) + "Score;");
    			mcsRolesHeader += ("MCSRole" + (i+1) + "Name;MCSRole" + (i+1) + "Score;");
    			mctsRolesHeader += ("MCTSRole" + (i+1) + "Name;MCTSRole" + (i+1) + "Score;");
    		}

    		ThreadContext.put("LOG_FOLDER", mainLogFolder);
    		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "PropnetStatistics", "PropnetBuildingTime;TotalPropnetInitTime;NumComponents;NumLinks;NumConstants;NumAnds;NumOrs;NumNots;NumPropositions;NumInits;NumGoals;NumTerminals;NumInputs;NumLegals;NumOthers;NumBases;NumTransitions;RNDSearchDuration;RNDIterations;RNDVisitedNodes;RNDIterationsPerSecond;RNDNodesPerSecond;" + randomRolesHeader + "MCSSearchDuration;MCSIterations;MCSVisitedNodes;MCSIterationsPerSecond;MCSNodesPerSecond;" + mcsRolesHeader + "MCTSSearchDuration;MCTSIterations;MCTSVisitedNodes;MCTSIterationsPerSecond;MCTSNodesPerSecond;" + mctsRolesHeader);
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



    	singleTestRun(mainLogFolder, myLogFolder, description, givenInitTime, timeBudget, simBudget, optimizations, withCache, cacheType,
    			numRoles, myRoleIndex, randomSearchManagerSettingsFile, mcsSearchManagerSettingsFile, mctsSearchManagerSettingsFile);

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
				case "5":
					optimizations[i] = new SimplifyLogicGates();
					break;
				default:
					throw new IllegalArgumentException();
			}
		}

		return optimizations;
	}

	private static void singleTestRun(String mainLogFolder, String myLogFolder, List<Gdl> description, long givenInitTime,
			long timeBudget, int simBudget, OptimizationCaller[] optimizations, boolean withCache, String cacheType, int numRoles, int myRoleIndex,
			File randomSearchManagerSettingsFile, File mcsSearchManagerSettingsFile, File mctsSearchManagerSettingsFile){

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

		long randomSearchDuration = -1;
        int randomIterations = -1;
        int randomVisitedNodes = -1;
        double randomIterationsPerSecond = -1;
        double randomNodesPerSecond = -1;
        double[] randomScoreSums = new double[numRoles];
        for(int i = 0; i < randomScoreSums.length; i++) {
        	randomScoreSums[i] = -1;
        }

		long mcsSearchDuration = -1;
        int mcsIterations = -1;
        int mcsVisitedNodes = -1;
        double mcsIterationsPerSecond = -1;
        double mcsNodesPerSecond = -1;
        double[] mcsScoreSums = new double[numRoles];
        for(int i = 0; i < mcsScoreSums.length; i++) {
        	mcsScoreSums[i] = -1;
        }

        long mctsSearchDuration = -1;
        int mctsIterations = -1;
        int mctsVisitedNodes = -1;
        double mctsIterationsPerSecond = -1;
        double mctsNodesPerSecond = -1;
        double[] mctsScoreSums = new double[numRoles];
        for(int i = 0; i < mctsScoreSums.length; i++) {
        	mctsScoreSums[i] = -1;
        }

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

			/******************************** RANDOM SPEED TEST *********************************/

			// Create the state machine giving it the propnet and the propnet state.
			// NOTE that if any of the two is null, it means that the propnet creation/initialization went wrong
			// and this will be detected by the state machine during initialization.

			InternalPropnetStateMachine thePropnetMachine;

			propnetState = manager.getInitialPropnetState();

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

			GamerLogger.log("SingleRunPNTester", "Starting Random speed test.");

			try {
				thePropnetMachine.initialize(description, System.currentTimeMillis() + givenInitTime);

				GamerSettings gamerSettings;
				try {
					FileReader reader = new FileReader(randomSearchManagerSettingsFile);
					Properties props = new Properties();

					// load the properties file:
					props.load(reader);

					reader.close();

					gamerSettings = new GamerSettings(props);

					//this.configureGamer(gamerSettings);

				} catch (FileNotFoundException e) {
					//this.gamerSettings = null;
					GamerLogger.logError("Gamer", "Impossible to create manager, cannot find the .properties file with the settings: " + randomSearchManagerSettingsFile.getPath() + ".");
					throw new RuntimeException("Impossible to create manager, cannot find the .properties file with the settings.");
				} catch (IOException e) {
					//this.gamerSettings = null;
					GamerLogger.logError("Gamer", "Impossible to create manager, exception when reading the .properties file with the settings: " + randomSearchManagerSettingsFile.getPath() + ".");
					throw new RuntimeException("Impossible to create manager, exception when reading the .properties file with the settings.");
				}

		        HybridRandomManager randomManager = new HybridRandomManager(new Random(), gamerSettings, randomSearchManagerSettingsFile.getName().split("\\.")[0]);

		        AbstractStateMachine abstractStateMachine = new CompactStateMachine(thePropnetMachine);

				//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", "Game step;Thinking time(ms);Search time(ms);Iterations;Visited nodes;Iterations/second;Nodes/second;Chosen move;Move score sum;Move visits;Avg move score;Avg search score " + rolesList + ";");

		    	randomManager.setUpManager(abstractStateMachine, numRoles, myRoleIndex, Long.MAX_VALUE);

				GamerLogger.log("GamerSettings", randomManager.printSearchManager());

				// OFFICIALLY STARTING!!!

		        GamerLogger.log("SingleRunPNTester", "Starting Random search.");

		        int numExpectedIterations = randomManager.getNumExpectedIterations();
		        // If we want to limit the simulations, we set the limit here in the manager...
		        if(simBudget > 0) {	// If using simulation budget, set the number of expected iterations to the simulation budget
		        	randomManager.setNumExpectedIterations(simBudget);
		        }else { // If using time budget, make sure it will be considered by setting to -1 the number of expected iterations
		        	randomManager.setNumExpectedIterations(-1);
		        }

		        randomManager.beforeMoveActions(1, false);

		        randomManager.search(abstractStateMachine.getInitialState(), System.currentTimeMillis() + timeBudget);

		        // Reset the number of expected iterations to the original value.

		       	GamerLogger.log("SingleRunPNTester", "Random search ended correctly.");

		       	randomSearchDuration = randomManager.getStepSearchDuration();
		       	randomIterations = randomManager.getStepIterations();
		       	randomVisitedNodes = randomManager.getStepVisitedNodes();

		        if(randomSearchDuration != 0){
		        	randomIterationsPerSecond = ((double) randomIterations * 1000)/((double) randomSearchDuration);
		        	randomNodesPerSecond = ((double) randomVisitedNodes * 1000)/((double) randomSearchDuration);
		        }

		        randomScoreSums = randomManager.getStepScoreSumForRoles();

		        GamerLogger.log("SingleRunPNTester", "Random speed test successful.");

			} catch (StateMachineInitializationException e) {
	        	GamerLogger.logError("SingleRunPNTester", "State machine " + thePropnetMachine.getName() + " initialization failed, impossible to test Random for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	        	GamerLogger.logStackTrace("SingleRunPNTester", e);
			} catch (MCSException e) {
	        	GamerLogger.logError("SingleRunPNTester", "Search failed for RandomManager. Impossible to test Random for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
	        	GamerLogger.logStackTrace("SingleRunPNTester", e);
			}

			propnetState = null;
			thePropnetMachine = null;

			collect(); // TODO: Leave or not?

			/******************************** MCS SPEED TEST *********************************/

			/**
			 * DIFFERENCE BETWEEN MCS AND RANDOM SEARCH:
			 * MCS search visits each move of the playing role in the current state in sequence until the
			 * search budget expires. For each visited move, it completes the joint move randomly and performs
			 * a random simulation from the resulting state. The obtained reward is used to update statistics
			 * about the move of my role performed in the initial state. Completely random search, instead, simply
			 * performs random playouts from the current state, collecting the sum of all the rewards obtained by
			 * each of the roles during all the performed simulations for the game step. (To summarize: MCS ->
			 * random playout for the next state after the initial one, Random -> random playout already from the
			 * initial state.
			 */

			// Create the state machine giving it the propnet and the propnet state.
			// NOTE that if any of the two is null, it means that the propnet creation/initialization went wrong
			// and this will be detected by the state machine during initialization.

			propnetState = manager.getInitialPropnetState();

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

		    //GamerLogger.log("SingleRunPNTester", "Testing machine: " + thePropnetMachine.getClass().getSimpleName());

			GamerLogger.log("SingleRunPNTester", "Starting MCS speed test.");

			try {
				thePropnetMachine.initialize(description, System.currentTimeMillis() + givenInitTime);

				GamerSettings gamerSettings;
				try {
					FileReader reader = new FileReader(mcsSearchManagerSettingsFile);
					Properties props = new Properties();

					// load the properties file:
					props.load(reader);

					reader.close();

					gamerSettings = new GamerSettings(props);

					//this.configureGamer(gamerSettings);

				} catch (FileNotFoundException e) {
					//this.gamerSettings = null;
					GamerLogger.logError("Gamer", "Impossible to create gamer, cannot find the .properties file with the settings: " + mcsSearchManagerSettingsFile.getPath() + ".");
					throw new RuntimeException("Impossible to create gamer, cannot find the .properties file with the settings.");
				} catch (IOException e) {
					//this.gamerSettings = null;
					GamerLogger.logError("Gamer", "Impossible to create gamer, exception when reading the .properties file with the settings: " + mcsSearchManagerSettingsFile.getPath() + ".");
					throw new RuntimeException("Impossible to create gamer, exception when reading the .properties file with the settings.");
				}

		        HybridMCSManager mcsManager = new HybridMCSManager(new Random(), gamerSettings, mcsSearchManagerSettingsFile.getName().split("\\.")[0]);

		        AbstractStateMachine abstractStateMachine = new CompactStateMachine(thePropnetMachine);

		        /*
		    	String rolesList = "[ ";
		    	for(int roleIndex = 0; roleIndex < abstractStateMachine.getRoles().size(); roleIndex++){
		    		rolesList += (abstractStateMachine.convertToExplicitRole((abstractStateMachine.getRoles().get(roleIndex))) + " ");
		    	}
		    	rolesList += "]";
		    	*/
				//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", "Game step;Thinking time(ms);Search time(ms);Iterations;Visited nodes;Iterations/second;Nodes/second;Chosen move;Move score sum;Move visits;Avg move score;Avg search score " + rolesList + ";");

				mcsManager.setUpManager(abstractStateMachine, numRoles, myRoleIndex, Long.MAX_VALUE);

				GamerLogger.log("GamerSettings", mcsManager.printSearchManager());

				// OFFICIALLY STARTING!!!

		        GamerLogger.log("SingleRunPNTester", "Starting MCS search.");

		        int numExpectedIterations = mcsManager.getNumExpectedIterations();
		        // If we want to limit the simulations, we set the limit here in the manager...
		        if(simBudget > 0) {	// If using simulation budget, set the number of expected iterations to the simulation budget
		        	mcsManager.setNumExpectedIterations(simBudget);
		        }else { // If using time budget, make sure it will be considered by setting to -1 the number of expected iterations
		        	mcsManager.setNumExpectedIterations(-1);
		        }

		        mcsManager.beforeMoveActions(1, false);

		        mcsManager.search(abstractStateMachine.getInitialState(), System.currentTimeMillis() + timeBudget);

		        // Reset the number of expected iterations to the original value.

		       	GamerLogger.log("SingleRunPNTester", "MCS search ended correctly.");

		       	mcsSearchDuration = mcsManager.getStepSearchDuration();
		       	mcsIterations = mcsManager.getStepIterations();
		        mcsVisitedNodes = mcsManager.getStepVisitedNodes();

		        if(mcsSearchDuration != 0){
		        	mcsIterationsPerSecond = ((double) mcsIterations * 1000)/((double) mcsSearchDuration);
			        mcsNodesPerSecond = ((double) mcsVisitedNodes * 1000)/((double) mcsSearchDuration);
		        }

		        mcsScoreSums = mcsManager.getStepScoreSumForRoles();

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
