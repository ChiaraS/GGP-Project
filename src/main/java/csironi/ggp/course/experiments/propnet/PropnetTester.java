package csironi.ggp.course.experiments.propnet;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.game.CloudGameRepository;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.LocalFolderGameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.logging.GamerLogger;

/**
 * NOTE!: this class must be updated to run the SingleRunPNTester.java class, because the class has been modified.
 *
 * Inputs this program gets:
 *
 * [fileSetting] = the .properties file that specifies the settings for this experiment. (Give full path if the file is not
 * 				   in the current working directory)
 *
 * NECESSARY ENTRIES FOR THE FILE:
 * 		[mainLogFolder] = the outer folder containing all the logs and files produced by this program and its subprograms.
 * 		[gameKey] = the key of the game to test (use the string "ALL" to test all games in the repository at once).
 *
 * OPTIONAL ENTRIES FOR THE FILE:
 * 		[repetitions] = number of times a single test (Random + MCS + MCTS speed test) has to be run for each game. (Default value: 100)
 * 		[givenInitTime] = the maximum time (in milliseconds) that the each test run has available to create the PropNet
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
 *  					  optimization 2, followed by optimization 3).  To let the manager perform no optimizations
 *  					  give the string "none" as argument, if you want to use the default optimizations give the
 *  					  string "default" as input. (Default value: "none")
 *  	[withCache] = true if the state machine based on the propnet must use the cache, false otherwise. (Default
 *  				  value: false)
 *  	[cacheType] = specifies which version of the cache to use. Possible types are:
 *  							- old: implementation that equals the one already provided in this code base
 *  							- ref: refactored cache that should spend less time searching entries in the cache
 *  							- nosync: same as ref but doesn't synchronize code and is not thread safe
 *  				  (Default value: nosync)
 *  	[repositoryType] = "folder" if we want to use a local folder containing .kif files for the games, "local" if
 *  						we want to use a local copy of a remote repository without updating it, "remote" if we
 *  						want to use a remote repository updating the local copy if any exists.
 *  	[repositoryLocation] = - If ([repositoryType] == "folder") specify the path of the folder where the .kif files
 *  							 are stored (e.g. )
 *  						   - If ([repositoryType] == "local") specify the path of the local copy of a remote repository
 *  						   - If ([repositoryType] == "remote") specify the URL of the remote repository
 *   	[managerSettingsFolder] = path of the folder where the .properties files for the managers are stored. (Default value:
 *  							  GamerConfiguration.gamersSettingsFolderPath)
 *  	[randomSearchManagerSettingsFile] = name of the .properties file that specifies the settings for the random
 *  								   		manager used in the experiment (Default value: RandomManager.properties).
 *  								   		If the file does not exist, the test with the random manager will be skipped.
 *  	[mcsSearchManagerSettingsFile] = name of the .properties file that specifies the settings for the MCS
 *  								   	 manager used in the experiment (Default value: McsManager.properties).
 * 										 If the file does not exist, the test with the MCS manager will be skipped.
 *  	[mctsSearchManagerSettingsFile] = name of the .properties file that specifies the settings for the MCTS
 *  								   	  manager used in the experiment (Default value: MctsManager.properties).
 *  									  If the file does not exist, the test with the MCTS manager will be skipped.
 *
 *
 * @author C.Sironi
 *
 */
public class PropnetTester {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	public static void main(String[] args){

		if(args.length != 1){
			System.out.println("[PropnetTester] Impossible to run program. Specify the .properties file for the experiment.");
			return;
		}

		File propertiesFile = new File(args[0]);

		if(!propertiesFile.exists()) {
			System.out.println("[PropnetTester] Impossible to run program. Cannot find .properties file for the experiment.");
			return;
		}

    	String mainLogFolder;
    	String gameKey;
    	int repetitions = 100;
       	long givenInitTime = 420000L;
       	String searchBudget = "60000ms";
    	String optimizationsString = "none";
    	boolean withCache = false;
    	String cacheType = "none";
       	String repositoryType = "remote";
    	String repositoryLocation = "games.ggp.org/base";
    	String managerSettingsFolder = GamerConfiguration.gamersSettingsFolderPath;
    	String randomSearchManagerSettingsFile = "RandomManager.properties";
    	String mcsSearchManagerSettingsFile = "McsManager.properties";
    	String mctsSearchManagerSettingsFile = "MctsManager.properties";

    	FileReader reader;

		try {
			reader = new FileReader(propertiesFile);

			Properties props = new Properties();

			// load the properties file:
			props.load(reader);

			reader.close();

			if(props.getProperty("mainLogFolder") == null) {
				System.out.println("[PropnetTester] Impossible to run program. The .properties file must specify a main folder for the logs.");
				return;
			}
			mainLogFolder = props.getProperty("mainLogFolder");

			if(props.getProperty("gameKey") == null) {
				System.out.println("[PropnetTester] Impossible to run program. The .properties file must specify the game key.");
				return;
			}
			gameKey = props.getProperty("gameKey");

    		try{
    			repetitions = Integer.parseInt(props.getProperty("repetitions"));
			}catch(NumberFormatException nfe){
				GamerLogger.log("PropNetTester", "Missing or inconsistent number of test repetitions! Using default value.");
				repetitions = 100;
			}
	    	try{
				givenInitTime = Long.parseLong(props.getProperty("givenInitTime"));
			}catch(NumberFormatException nfe){
				GamerLogger.log("PropNetTester", "Missing or inconsistent maximum initialization time specification! Using default value.");
				givenInitTime = 420000L;
			}

	    	searchBudget = props.getProperty("searchBudget");

			optimizationsString = props.getProperty("optimizations");

			if(optimizationsString == null || optimizationsString.equals("null")){
				optimizationsString = "none";
			}

			withCache = Boolean.parseBoolean(props.getProperty("withCache"));

			cacheType = props.getProperty("cacheType");

			if(cacheType == null || cacheType.equals("null")){
				cacheType = "none";
			}

	       	repositoryType = props.getProperty("repositoryType");
	       	if(repositoryType == null || repositoryType.equals("null")){
	       		repositoryType = "remote";
			}

	    	repositoryLocation = props.getProperty("repositoryLocation");
	    	if(repositoryLocation == null || repositoryLocation.equals("null")){
	    		repositoryLocation = "games.ggp.org/base";
			}

	    	managerSettingsFolder = props.getProperty("managerSettingsFolder");
	    	if(managerSettingsFolder == null || managerSettingsFolder.equals("null")){
	    		managerSettingsFolder = GamerConfiguration.gamersSettingsFolderPath;
			}

	    	randomSearchManagerSettingsFile = props.getProperty("randomSearchManagerSettingsFile");
	    	if(randomSearchManagerSettingsFile == null || randomSearchManagerSettingsFile.equals("null")){
	    		randomSearchManagerSettingsFile = "RandomManager.properties";
			}

	    	mcsSearchManagerSettingsFile = props.getProperty("mcsSearchManagerSettingsFile");
	    	if(mcsSearchManagerSettingsFile == null || mcsSearchManagerSettingsFile.equals("null")){
	    		mcsSearchManagerSettingsFile = "McsManager.properties";
			}

	    	mctsSearchManagerSettingsFile = props.getProperty("mctsSearchManagerSettingsFile");
	    	if(mctsSearchManagerSettingsFile == null || mctsSearchManagerSettingsFile.equals("null")){
	    		mctsSearchManagerSettingsFile = "MctsManager.properties";
			}

    	} catch (IOException | NumberFormatException e) {
			System.out.println("Impossible to perform experiment, cannot correctly read/write the .properties file.");
			e.printStackTrace();
			return;
		}

    	ThreadContext.put("LOG_FOLDER", mainLogFolder);

    	GamerLogger.startFileLogging();

    	GamerLogger.log(GamerLogger.FORMAT.PLAIN_FORMAT, "Logger", "");
    	GamerLogger.log(GamerLogger.FORMAT.PLAIN_FORMAT, "PropNetTester", "");

    	GamerLogger.log("PropNetTester", "Starting PropNet tester.");

    	if(gameKey.equals("ALL")){
    		GamerLogger.log("PropNetTester", "Running PropNet test for ALL games.");
    	}else{
    		GamerLogger.log("PropNetTester", "Running PropNet test for game " + gameKey + ".");
    	}
    	GamerLogger.log("PropNetTester", "Logging in folder " + mainLogFolder + ".");

    	String testSettings = "Settings for current tests:\n";
    	testSettings += "[repetitions] = " + repetitions + "\n";
    	testSettings += "[givenInitTime] = " + givenInitTime + "\n";
    	testSettings += "[searchBudget] = " + searchBudget + "\n";
    	testSettings += "[optimizations] = " + optimizationsString + "\n";
    	testSettings += "[withCache] = " + withCache + "\n";
    	testSettings += "[cacheType] = " + cacheType + "\n";
    	testSettings += "[repositoryType] = " + repositoryType + "\n";
    	testSettings += "[repositoryLocation] = " + repositoryLocation + "\n";
    	testSettings += "[managerSettingsFolder] = " + managerSettingsFolder + "\n";
    	testSettings += "[randomSearchManagerSettingsFile] = " + randomSearchManagerSettingsFile + "\n";
    	testSettings += "[mcsSearchManagerSettingsFile] = " + mcsSearchManagerSettingsFile + "\n";
    	testSettings += "[mctsSearchManagerSettingsFile] = " + mctsSearchManagerSettingsFile + "\n";

    	GamerLogger.log("PropNetTester", testSettings);

    	GameRepository gameRepo;

    	if(gameKey.equals("ALL")){

        	switch(repositoryType) {
	    	case "folder":
	    		//gameRepo = new LocalFolderGameRepository(GamerConfiguration.defaultLocalFolderGameRepositoryFolderPath);
	    		gameRepo = new LocalFolderGameRepository(repositoryLocation);
	    		break;
	    	case "local":
	    		//gameRepo = new ManualUpdateLocalGameRepository(GamerConfiguration.defaultLocalGameRepositoryFolderPath + "/" + GamerConfiguration.defaultGGPBaseRepo/*GamerConfiguration.defaultStanfordRepo*/);
	    		gameRepo = new ManualUpdateLocalGameRepository(repositoryLocation);
	    		break;
	    	default:
	    		gameRepo = new CloudGameRepository(repositoryLocation);
	    		break;
        	}

    	    //GameRepository theRepository = GameRepository.getDefaultRepository();

    		// WINDOWS
    		//GameRepository theRepository = new ManualUpdateLocalGameRepository("C:/Users/c.sironi/\"BITBUCKET REPOS\"/GGP-Base/GGPBase-GameRepo-03022016");

    		// LINUX
    	    //GameRepository theRepository = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");


    	    for(String aGameKey : gameRepo.getGameKeys()) {
    	        if(aGameKey.contains("laikLee")) continue;

    	        testGame(mainLogFolder, aGameKey, repetitions, givenInitTime, searchBudget, optimizationsString,
    	        		withCache, cacheType, repositoryType, repositoryLocation, managerSettingsFolder,
    	        		randomSearchManagerSettingsFile, mcsSearchManagerSettingsFile, mctsSearchManagerSettingsFile);

    	    }

    	}else{
    		testGame(mainLogFolder, gameKey, repetitions, givenInitTime, searchBudget, optimizationsString,
    				withCache, cacheType, repositoryType, repositoryLocation, managerSettingsFolder,
    				randomSearchManagerSettingsFile, mcsSearchManagerSettingsFile, mctsSearchManagerSettingsFile);
    	}

	}

	private static void testGame(String mainLogFolder, String gameKey, int repetitions, long givenInitTime,
			String searchBudget, String optimizationsString, boolean withCache, String cacheType,
			String repositoryType, String repositoryLocation, String managerSettingsFolder,
			String randomSearchManagerSettingsFile, String mcsSearchManagerSettingsFile,
			String mctsSearchManagerSettingsFile){

		String gameFolder = mainLogFolder + "/" + gameKey;

    	ThreadContext.put("LOG_FOLDER", gameFolder);

    	String toLog = "Starting PropNet test for game " + gameKey + " with following settings:";
    	toLog += "\n[repetitions] = " + repetitions;
    	toLog += "\n[givenInitTime] = " + givenInitTime;
    	toLog += "\n[searchBudget] = " + searchBudget;
    	toLog += "\n[optimizations] = " + optimizationsString;
    	toLog += "\n[withCache] = " + withCache;
    	toLog += "\n[cacheType] = " + cacheType;
    	toLog += "\n[repositoryType] = " + repositoryType;
    	toLog += "\n[repositoryLocation] = " + repositoryLocation;
    	toLog += "\n[managerSettingsFolder] = " + managerSettingsFolder;
    	toLog += "\n[randomSearchManagerSettingsFile] = " + randomSearchManagerSettingsFile;
    	toLog += "\n[mcsSearchManagerSettingsFile] = " + mcsSearchManagerSettingsFile;
    	toLog += "\n[mctsSearchManagerSettingsFile] = " + mctsSearchManagerSettingsFile;

    	GamerLogger.log("PropnetTester", toLog);

		String singleRunID = null;
		String singleRunFolder = null;
		File logFile = null;
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "SingleRunPNTest.jar", gameFolder, singleRunFolder, gameKey, ""+givenInitTime, searchBudget, optimizationsString, ""+withCache, cacheType, repositoryType, repositoryLocation, managerSettingsFolder, randomSearchManagerSettingsFile, mcsSearchManagerSettingsFile, mctsSearchManagerSettingsFile);

		//System.out.println(pb.command());

		for(int i = 0; i < repetitions; i++){

			Process process = null;

			try{

				singleRunID = System.currentTimeMillis()+"-"+gameKey;
				singleRunFolder = gameFolder + "/" + singleRunID;

				GamerLogger.log("PropnetTester", "Starting single run " + singleRunID + ".");

				File logFolder = new File(singleRunFolder);
				if(!logFolder.exists()){
					logFolder.mkdirs();
				}

				logFile = new File(singleRunFolder + "/ConsoleOutput.log");

				if(!logFile.exists()){
					logFile.createNewFile();
				}

				pb.command().set(4, singleRunFolder);

				//System.out.println(pb.command());

				pb.redirectOutput(logFile);
				pb.redirectError(logFile);

				process = pb.start();

				process.waitFor();

				if(process.exitValue() != 0){
					GamerLogger.logError("PropnetTester", "Something went wrong when running single run " + singleRunID + " with the following arguments: " + pb.command() + ".");
				}else{
					GamerLogger.log("PropnetTester", "Correctly completed single run " + singleRunID + ".");
				}
			}catch(IOException ioe){
				GamerLogger.logError("PropnetTester", "Error setting up and starting the process that runs single run of test " + singleRunID + ".");
				GamerLogger.logStackTrace("PropnetTester", ioe);
			}catch(InterruptedException ie){
				GamerLogger.logError("PropnetTester", "Test interrupted while waiting for single test run process to complete execution for test " + singleRunID + ".");
				GamerLogger.logStackTrace("PropnetTester", ie);
				if(process != null){
					process.destroy();
				}
				Thread.currentThread().interrupt();
			}
		}

		GamerLogger.log("PropnetTester", "Test for game " + gameKey + " ended.");
	}


}
