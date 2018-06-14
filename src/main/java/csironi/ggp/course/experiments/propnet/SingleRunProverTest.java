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
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.HybridMcsManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.exceptions.MCTSException;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.HybridMctsManager;
import org.ggp.base.player.gamer.statemachine.RNDSimulations.HybridRandomManager;
import org.ggp.base.player.gamer.statemachine.RNDSimulations.exceptions.RandomException;
import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.abstractsm.ExplicitStateMachine;
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
 * 		[myRoleIndex] = the index of the role that should be considered as the playing role.
 *
 * - OPTIONAL INPUTS (NOTE: you can either specify all or none of them, but not only part of them.):
 * 		[givenInitTime] = the maximum time (in milliseconds) that the program has available to initialize the state
 * 						  machine (Default value: 420000ms).
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
 *  	[withCache] = true if the state machine based on the prover must use the cache, false otherwise. (Default
 *  				  value: false)
 *  	[cacheType] = specifies which version of the cache to use. Possible types are:
 *  							- old: implementation that equals the one already provided in this code base
 *  							- nosync: refactored cache that should spend less time searching entries in the
 *  									  cache and doesn't synchronize code and is not thread safe
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
public class SingleRunProverTest {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	public static void main(String[] args) {

		if(args.length < 4){
			System.out.println("[SingleRunProverTest] Impossible to run program. Specify at least a main log folder, the log folder for this run of the test, the key of the game to be tested and the index of the playing role.");
			return;
		}

    	String mainLogFolder = args[0];
    	String myLogFolder = args[1];
    	String gameKey = args[2];
    	int myRoleIndex = -1;
    	try{
    		myRoleIndex = Integer.parseInt(args[3]);
		}catch(NumberFormatException nfe){
			System.out.println("[SingleRunProverTest] Wrong specification of the index of the playing role.");
			return;
		}

    	ThreadContext.put("LOG_FOLDER", myLogFolder);

    	GamerLogger.startFileLogging();

    	GamerLogger.log("SingleRunProverTester", "Single run Prover test for game " + gameKey + ".");
    	GamerLogger.log("SingleRunProverTester", "Logging in folder " + myLogFolder + ".");

       	long givenInitTime = 420000L;
       	long timeBudget = 60000L;
    	int simBudget = -1;


    	boolean withCache = false;
    	String cacheType = null;
    	File randomSearchManagerSettingsFile = new File(GamerConfiguration.gamersSettingsFolderPath + "/RandomManager.properties");
    	File mcsSearchManagerSettingsFile = new File(GamerConfiguration.gamersSettingsFolderPath + "/McsManager.properties");
    	File mctsSearchManagerSettingsFile = new File(GamerConfiguration.gamersSettingsFolderPath + "/MctsManager.properties");

    	if(args.length == 11){
	    	try{
				givenInitTime = Long.parseLong(args[4]);
			}catch(NumberFormatException nfe){
				GamerLogger.log("SingleRunProverTester", "Inconsistent maximum initialization time specification! Using default value.");
				givenInitTime = 420000L;
			}

	    	if(args[5].endsWith("ms")) { // We have a time budget
				try{
					timeBudget = Long.parseLong(args[5].substring(0, args[5].length()-2));
				}catch(NumberFormatException nfe){
					GamerLogger.log("SingleRunProverTester", "Inconsistent time budget specification! Using default value.");
					timeBudget = 60000L;
				}
				if(timeBudget <= 0) {
					GamerLogger.log("SingleRunProverTester", "Non-positive time budget specification! Using default value.");
					timeBudget = 60000L;
				}
				simBudget = -1;
	    	}else if(args[5].endsWith("sim")) { // We have a simulation budget
	    		try{
	    			simBudget = Integer.parseInt(args[5].substring(0, args[5].length()-3));
				}catch(NumberFormatException nfe){
					GamerLogger.log("SingleRunProverTester", "Inconsistent simulation budget specification! Using default value.");
					simBudget = 60000;
				}
	    		if(simBudget <= 0) {
					GamerLogger.log("SingleRunProverTester", "Non-positive simulation budget specification! Using default value.");
					simBudget = 60000;
				}
	    		timeBudget = -1;
	    	}else {
	    		timeBudget = 60000L;
	        	simBudget = -1;
	    	}

			withCache = Boolean.parseBoolean(args[6]);

			cacheType = args[7];

			randomSearchManagerSettingsFile = new File(GamerConfiguration.gamersSettingsFolderPath + "/" + args[8]);
	    	mcsSearchManagerSettingsFile = new File(GamerConfiguration.gamersSettingsFolderPath + "/" + args[9]);
	    	mctsSearchManagerSettingsFile = new File(GamerConfiguration.gamersSettingsFolderPath + "/" + args[10]);

	    	if(!randomSearchManagerSettingsFile.exists()) {
	    		GamerLogger.log("SingleRunProverTester", "Cannot find property file for RandomSearchManager: " + randomSearchManagerSettingsFile.getPath());
	    		return;
	    	}
	    	if(!mcsSearchManagerSettingsFile.exists()) {
	    		GamerLogger.log("SingleRunProverTester", "Cannot find property file for McsSearchManager: " + mcsSearchManagerSettingsFile.getPath());
	    		return;
	    	}
	    	if(!mctsSearchManagerSettingsFile.exists()) {
	    		GamerLogger.log("SingleRunProverTester", "Cannot find property file for MctsSearchManager: " + mctsSearchManagerSettingsFile.getPath());
	    		return;
	    	}

    	}

    	String testSettings = "Settings for current test run:\n";
    	testSettings += "[gameKey] = " + gameKey + "\n";
    	testSettings += "[myRoleIndex] = " + myRoleIndex + "\n";
    	testSettings += "[givenInitTime] = " + givenInitTime + "\n";
    	testSettings += "[searchBudget] = " + (timeBudget > 0 ? (timeBudget + "ms") : (simBudget + "sim")) + "\n";
    	testSettings += "[withCache] = " + withCache + "\n";
    	testSettings += "[cacheType] = " + cacheType + "\n";
    	testSettings += "[randomSearchManagerSettingsFilePath] = " + randomSearchManagerSettingsFile.getPath()+ "\n";
    	testSettings += "[mcsSearchManagerSettingsFilePath] = " + mcsSearchManagerSettingsFile.getPath()+ "\n";
    	testSettings += "[mctsSearchManagerSettingsFilePath] = " + mctsSearchManagerSettingsFile.getPath()+ "\n";

    	GamerLogger.log("SingleRunProverTester", testSettings);

    	//GameRepository gameRepo = GameRepository.getDefaultRepository();

		// WINDOWS
		//GameRepository gameRepo = new ManualUpdateLocalGameRepository("C:/Users/c.sironi/BITBUCKET REPOS/GGP-Base/GGPBase-GameRepo-03022016");

    	GameRepository gameRepo = new ManualUpdateLocalGameRepository(GamerConfiguration.defaultLocalGameRepositoryFolderPath + "/" + GamerConfiguration.defaultStanfordRepo);

    	//GameRepository gameRepo = new LocalFolderGameRepository(GamerConfiguration.defaultLocalFolderGameRepositoryFolderPath);

    	Game game = gameRepo.getGame(gameKey);

    	if(game == null){
    		GamerLogger.logError("SingleRunProverTester", "Impossible to test Prover for game " + gameKey + ": specified game not found in the repository.");
			return;
    	}

    	List<ExplicitRole> explicitRoles = ExplicitRole.computeRoles(game.getRules());
    	int numRoles = explicitRoles.size();

    	if(myRoleIndex >= numRoles) {
    		GamerLogger.logError("SingleRunProverTester", "Impossible to test Prover for game " + gameKey +
    				": cannot use role at index " + myRoleIndex + " when there are only " + numRoles + " roles in the game.");
			return;
    	}

    	File theCSVFile = new File(mainLogFolder + "/ProverStatistics.csv");

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
    		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ProverStatistics", "TotalStateMachineInitTime;RNDSearchDuration;RNDIterations;RNDVisitedNodes;RNDIterationsPerSecond;RNDNodesPerSecond;" + randomRolesHeader + "MCSSearchDuration;MCSIterations;MCSVisitedNodes;MCSIterationsPerSecond;MCSNodesPerSecond;" + mcsRolesHeader + "MCTSSearchDuration;MCTSIterations;MCTSVisitedNodes;MCTSIterationsPerSecond;MCTSNodesPerSecond;" + mctsRolesHeader);
    		ThreadContext.put("LOG_FOLDER",  myLogFolder);
    	}

    	List<Gdl> description = game.getRules();

    	singleTestRun(mainLogFolder, myLogFolder, description, givenInitTime, timeBudget, simBudget, withCache, cacheType,
    			explicitRoles, myRoleIndex, randomSearchManagerSettingsFile, mcsSearchManagerSettingsFile, mctsSearchManagerSettingsFile);

	}

	private static void singleTestRun(String mainLogFolder, String myLogFolder, List<Gdl> description, long givenInitTime,
			long timeBudget, int simBudget, boolean withCache, String cacheType,
			List<ExplicitRole> explicitRoles, int myRoleIndex, File randomSearchManagerSettingsFile,
			File mcsSearchManagerSettingsFile, File mctsSearchManagerSettingsFile){

		GamerLogger.log("SingleRunProverTester", "Starting Prover test.");

		int numRoles = explicitRoles.size();

		long totalStateMachineInitTime = -1;

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

		/******************************** RANDOM SPEED TEST *********************************/

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

		GamerLogger.log("SingleRunProverTester", "Starting Random speed test.");

		try {
			long start = System.currentTimeMillis();

			theProverMachine.initialize(description, System.currentTimeMillis() + givenInitTime);

			totalStateMachineInitTime = System.currentTimeMillis() - start;

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
				GamerLogger.logError("SingleRunProverTester", "Impossible to create manager, cannot find the .properties file with the settings: " + randomSearchManagerSettingsFile.getPath() + ".");
				throw new RuntimeException("Impossible to create manager, cannot find the .properties file with the settings.");
			} catch (IOException e) {
				//this.gamerSettings = null;
				GamerLogger.logError("SingleRunProverTester", "Impossible to create manager, exception when reading the .properties file with the settings: " + randomSearchManagerSettingsFile.getPath() + ".");
				throw new RuntimeException("Impossible to create manager, exception when reading the .properties file with the settings.");
			}

	        HybridRandomManager randomManager = new HybridRandomManager(new Random(), gamerSettings, randomSearchManagerSettingsFile.getName().split("\\.")[0]);

	        AbstractStateMachine abstractStateMachine = new ExplicitStateMachine(theProverMachine);

			//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", "Game step;Thinking time(ms);Search time(ms);Iterations;Visited nodes;Iterations/second;Nodes/second;Chosen move;Move score sum;Move visits;Avg move score;Avg search score " + rolesList + ";");

	    	randomManager.setUpManager(abstractStateMachine, numRoles, myRoleIndex, Long.MAX_VALUE);

			GamerLogger.log("GamerSettings", randomManager.printSearchManager());

			// OFFICIALLY STARTING!!!

	        GamerLogger.log("SingleRunProverTester", "Starting Random search.");

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
	        randomManager.setNumExpectedIterations(numExpectedIterations);

	       	GamerLogger.log("SingleRunProverTester", "Random search ended correctly.");

	       	randomSearchDuration = randomManager.getStepSearchDuration();
	       	randomIterations = randomManager.getStepIterations();
	       	randomVisitedNodes = randomManager.getStepVisitedNodes();

	        if(randomSearchDuration != 0){
	        	randomIterationsPerSecond = ((double) randomIterations * 1000)/((double) randomSearchDuration);
	        	randomNodesPerSecond = ((double) randomVisitedNodes * 1000)/((double) randomSearchDuration);
	        }

	        randomScoreSums = randomManager.getStepScoreSumForRoles();

	        GamerLogger.log("SingleRunProverTester", "Random speed test successful.");

		} catch (StateMachineInitializationException e) {
        	GamerLogger.logError("SingleRunProverTester", "State machine " + theProverMachine.getName() + " initialization failed, impossible to test Random for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
        	GamerLogger.logStackTrace("SingleRunProverTester", e);
		} catch (RandomException e) {
        	GamerLogger.logError("SingleRunProverTester", "Search failed for RandomManager. Impossible to test Random for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
        	GamerLogger.logStackTrace("SingleRunProverTester", e);
		}

		theProverMachine = null;

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

	    //GamerLogger.log("SingleRunProverTester", "Testing machine: " + theProverMachine.getClass().getSimpleName());

		GamerLogger.log("SingleRunProverTester", "Starting MCS speed test.");

		try {
			theProverMachine.initialize(description, System.currentTimeMillis() + givenInitTime);

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
				GamerLogger.logError("SingleRunProverTester", "Impossible to create gamer, cannot find the .properties file with the settings: " + mcsSearchManagerSettingsFile.getPath() + ".");
				throw new RuntimeException("Impossible to create gamer, cannot find the .properties file with the settings.");
			} catch (IOException e) {
				//this.gamerSettings = null;
				GamerLogger.logError("SingleRunProverTester", "Impossible to create gamer, exception when reading the .properties file with the settings: " + mcsSearchManagerSettingsFile.getPath() + ".");
				throw new RuntimeException("Impossible to create gamer, exception when reading the .properties file with the settings.");
			}

	        HybridMcsManager mcsManager = new HybridMcsManager(new Random(), gamerSettings, mcsSearchManagerSettingsFile.getName().split("\\.")[0]);

	        AbstractStateMachine abstractStateMachine = new ExplicitStateMachine(theProverMachine);

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

	        GamerLogger.log("SingleRunProverTester", "Starting MCS search.");

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
	        mcsManager.setNumExpectedIterations(numExpectedIterations);

	       	GamerLogger.log("SingleRunProverTester", "MCS search ended correctly.");

	       	mcsSearchDuration = mcsManager.getStepSearchDuration();
	       	mcsIterations = mcsManager.getStepIterations();
	        mcsVisitedNodes = mcsManager.getStepVisitedNodes();

	        if(mcsSearchDuration != 0){
	        	mcsIterationsPerSecond = ((double) mcsIterations * 1000)/((double) mcsSearchDuration);
		        mcsNodesPerSecond = ((double) mcsVisitedNodes * 1000)/((double) mcsSearchDuration);
	        }

	        mcsScoreSums = mcsManager.getStepScoreSumForRoles();

	        GamerLogger.log("SingleRunProverTester", "MCS speed test successful.");

		} catch (StateMachineInitializationException e) {
        	GamerLogger.logError("SingleRunProverTester", "State machine " + theProverMachine.getName() + " initialization failed, impossible to test MCS for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
        	GamerLogger.logStackTrace("SingleRunProverTester", e);
		} catch (MCSException e) {
        	GamerLogger.logError("SingleRunProverTester", "Search failed for MCSManager. Impossible to test MCS for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
        	GamerLogger.logStackTrace("SingleRunProverTester", e);
		}

		theProverMachine = null;

		collect(); // TODO: Leave or not?

		/******************************** MCTS SPEED TEST *********************************/

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

		GamerLogger.log("SingleRunProverTester", "Starting MCTS speed test.");

		try {
			theProverMachine.initialize(description, System.currentTimeMillis() + givenInitTime);

			GamerSettings gamerSettings;
			try {
				FileReader reader = new FileReader(mctsSearchManagerSettingsFile);
				Properties props = new Properties();

				// load the properties file:
				props.load(reader);

				reader.close();

				gamerSettings = new GamerSettings(props);

				//this.configureGamer(gamerSettings);

			} catch (FileNotFoundException e) {
				//this.gamerSettings = null;
				GamerLogger.logError("SingleRunProverTester", "Impossible to create gamer, cannot find the .properties file with the settings: " + mctsSearchManagerSettingsFile.getPath() + ".");
				throw new RuntimeException("Impossible to create gamer, cannot find the .properties file with the settings.");
			} catch (IOException e) {
				//this.gamerSettings = null;
				GamerLogger.logError("SingleRunProverTester", "Impossible to create gamer, exception when reading the .properties file with the settings: " + mctsSearchManagerSettingsFile.getPath() + ".");
				throw new RuntimeException("Impossible to create gamer, exception when reading the .properties file with the settings.");
			}

	        HybridMctsManager mctsManager = new HybridMctsManager(new Random(), gamerSettings, mctsSearchManagerSettingsFile.getName().split("\\.")[0]);

	        AbstractStateMachine abstractStateMachine = new ExplicitStateMachine(theProverMachine);

	        /*
	    	String rolesList = "[ ";
	    	for(int roleIndex = 0; roleIndex < abstractStateMachine.getRoles().size(); roleIndex++){
	    		rolesList += (abstractStateMachine.convertToExplicitRole((abstractStateMachine.getRoles().get(roleIndex))) + " ");
	    	}
	    	rolesList += "]";
	    	*/
			//GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Stats", "Game step;Thinking time(ms);Search time(ms);Iterations;Visited nodes;Iterations/second;Nodes/second;Chosen move;Move score sum;Move visits;Avg move score;Avg search score " + rolesList + ";");

			mctsManager.setUpManager(abstractStateMachine, numRoles, myRoleIndex, Long.MAX_VALUE);

			GamerLogger.log("GamerSettings", mctsManager.printSearchManager());

			// OFFICIALLY STARTING!!!

	        GamerLogger.log("SingleRunProverTester", "Starting MCTS search.");

	        int numExpectedIterations = mctsManager.getNumExpectedIterations();
	        // If we want to limit the simulations, we set the limit here in the manager...
	        if(simBudget > 0) {	// If using simulation budget, set the number of expected iterations to the simulation budget
	        	mctsManager.setNumExpectedIterations(simBudget);
	        }else { // If using time budget, make sure it will be considered by setting to -1 the number of expected iterations
	        	mctsManager.setNumExpectedIterations(-1);
	        }

	        mctsManager.beforeMoveActions(1, false);

	        mctsManager.search(abstractStateMachine.getInitialState(), System.currentTimeMillis() + timeBudget, 1);

	        // Reset the number of expected iterations to the original value.
	        mctsManager.setNumExpectedIterations(numExpectedIterations);

	       	GamerLogger.log("SingleRunProverTester", "MCTS search ended correctly.");

	       	mctsSearchDuration = mctsManager.getStepSearchDuration();
	       	mctsIterations = mctsManager.getStepIterations();
	        mctsVisitedNodes = mctsManager.getStepVisitedNodes();

	        if(mctsSearchDuration != 0){
	        	mctsIterationsPerSecond = ((double) mctsIterations * 1000)/((double) mctsSearchDuration);
		        mctsNodesPerSecond = ((double) mctsVisitedNodes * 1000)/((double) mctsSearchDuration);
	        }

	        mctsScoreSums = mctsManager.getStepScoreSumForRoles();

			mctsManager.afterMoveActions(); // Not really needed

	        GamerLogger.log("SingleRunProverTester", "MCTS speed test successful.");

		} catch (StateMachineInitializationException e) {
        	GamerLogger.logError("SingleRunProverTester", "State machine " + theProverMachine.getName() + " initialization failed, impossible to test MCTS for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
        	GamerLogger.logStackTrace("SingleRunProverTester", e);
		} catch (MCTSException e) {
        	GamerLogger.logError("SingleRunProverTester", "Search failed for MCTSManager. Impossible to test MCTS for this game. Cause: [" + e.getClass().getSimpleName() + "] " + e.getMessage() );
        	GamerLogger.logStackTrace("SingleRunProverTester", e);
		}

		theProverMachine = null;

		collect(); // TODO: Leave or not?

		/************************** LOG *******************************/

		ThreadContext.put("LOG_FOLDER", mainLogFolder);

		String randomRolesNamesAndScores = "";
		String mcsRolesNamesAndScores = "";
		String mctsRolesNamesAndScores = "";
		for(int i = 0; i < explicitRoles.size(); i++) {
			randomRolesNamesAndScores += (explicitRoles.get(i).toString() + ";" + randomScoreSums[i] + ";");
			mcsRolesNamesAndScores += (explicitRoles.get(i).toString() + ";" + mcsScoreSums[i] + ";");
			mctsRolesNamesAndScores += (explicitRoles.get(i).toString() + ";" + mctsScoreSums[i] + ";");
		}

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ProverStatistics", totalStateMachineInitTime + ";" +
				randomSearchDuration + ";" + randomIterations + ";" + randomVisitedNodes + ";" +
				randomIterationsPerSecond + ";" + randomNodesPerSecond + ";" + randomRolesNamesAndScores +
				mcsSearchDuration + ";" + mcsIterations + ";" +	mcsVisitedNodes + ";" +
				mcsIterationsPerSecond + ";" + mcsNodesPerSecond + ";" + mcsRolesNamesAndScores +
				mctsSearchDuration + ";" + mctsIterations + ";" + mctsVisitedNodes + ";" +
				mctsIterationsPerSecond + ";" + mctsNodesPerSecond + ";" + mctsRolesNamesAndScores);

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
