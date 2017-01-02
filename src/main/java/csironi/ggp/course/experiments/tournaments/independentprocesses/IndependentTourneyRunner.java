package csironi.ggp.course.experiments.tournaments.independentprocesses;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.gamer.statemachine.ConfigurableStateMachineGamer;
import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import csironi.ggp.course.experiments.tournaments.Combinator;

/**
 * This class takes care of performing a tourney for the given game.
 * For the given game it computes all the possible combinations in which the given
 * gamer types can be assigned (with no exclusion of any of the types) to the roles
 * in the game and performs the specified amount of matches for each combination,
 * with the given settings.
 *
 * NOTE: for now it works only if #gamerTypes <= #gameRoles
 * @author C.Sironi
 *
 */
public class IndependentTourneyRunner {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//System.out.println("!!" + ProjectSearcher.INTERNAL_PROPNET_GAMERS.getConcreteClasses().size());

		if(args.length != 1){
			System.out.println("Impossible to start experiment! Wrong number of inputs!");
			System.out.println("Specify the name of the file with the settings of the experiment");
			return;
		}

		System.out.println("Trying to start experiment.");

		File propertiesFile = new File(args[0]);

		String tourneyName;
		Set<String> gameKeys = new HashSet<String>();
		int startClock;
		int playClock;
		long pnCreationTime;
		// Number of parallel player threads that should run at the same time. NOTE that this requirement is loosely satisfied.
		// The actual number of player threads that will be running will be the closest feasible number of threads that allows
		// to run matches with the given number of roles for the game.
		int numParallelPlayers;
		int matchesPerGamerType;
		String[] theGamersTypesString;
		int runNumber;


		// The current time is used to distinguish multiple separate runs of the same experiment.
		// If I want to continue a previous experiment, all tourneys with the ID saved in the properties
		// will be continued. Whenever I want to run the experiment from the beginning a new ID will be
		// associated to the run so that whenever I want to continue such experiment the tourneys of the
		// other experiment with the same name won't be continued.
		Long timeID;

		FileReader reader;

		//GameRepository gameRepo = GameRepository.getDefaultRepository();
    	//GameRepository gameRepo = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");
    	GameRepository gameRepo = new ManualUpdateLocalGameRepository(GamerConfiguration.defaultLocalGameRepositoryFolderPath);

		Game game;

		try {
			reader = new FileReader(propertiesFile);

			Properties props = new Properties();

			// load the properties file:
			props.load(reader);

			reader.close();

			tourneyName = props.getProperty("tourneyName");

			String[] gameKeysStrings = props.getProperty("gameKeys").split(";");
			// Remove duplicate game keys and check if all games are available in the repository.
			// I check here if all games are in the repository because if even one is missing I
			// don't want the experiment to start at all

	    	for(int i = 0; i < gameKeysStrings.length; i++){
	    		// If it's not a duplicate key...
	    		if(gameKeys.add(gameKeysStrings[i])){
	    			// ...check if the game is in the repository.
	    			game = gameRepo.getGame(gameKeysStrings[i]);

			    	if(game == null){
			    		System.out.println("Impossible to start experiment: specified game " + gameKeysStrings[i] + " not found in the repository.");
						return;
			    	}
	    		}
	    	}

			startClock = Integer.parseInt(props.getProperty("startClock"));
			playClock = Integer.parseInt(props.getProperty("playClock"));
			pnCreationTime = Long.parseLong(props.getProperty("pnCreationTime"));
			numParallelPlayers = Integer.parseInt(props.getProperty("numParallelPlayers"));
			matchesPerGamerType = Integer.parseInt(props.getProperty("matchesPerGamerType"));

			theGamersTypesString = props.getProperty("theGamersTypes").split(";");

			// Check if all gamers exist and the settings of the configurable ones are specified
			// Check here so that the tourney won't even start if the gamer types don't exist or are not correctly specified
			String[] gamerTypes = new String[theGamersTypesString.length];
			String[] gamerSettings = new String[theGamersTypesString.length];
	    	for(int i = 0; i < theGamersTypesString.length; i++){
	    		if(theGamersTypesString[i].endsWith(".properties")){
	    			String[] s = theGamersTypesString[i].split("-");
	    			gamerTypes[i] = s[0];
	    			gamerSettings[i] = s[1];
	    		}else{
	    			gamerTypes[i] = theGamersTypesString[i];
	    			gamerSettings[i] = null;
	    		}
	    	}

	    	for(int i = 0; i < gamerTypes.length; i++){
	    		Class<?> theCorrespondingClass = null;
	    		for (Class<?> gamerClass : ProjectSearcher.INTERNAL_PROPNET_GAMERS.getConcreteClasses()) {
	        		if(gamerClass.getSimpleName().equals(gamerTypes[i])){
	        			theCorrespondingClass = gamerClass;
	        			if(ConfigurableStateMachineGamer.class.isAssignableFrom(theCorrespondingClass)){ // The class is subclass of ConfigurableStateMachineGamer
	        				// If the gamer is configurable than the settings file must be specified
	        				if(gamerSettings[i] == null){
	        					System.out.println("Impossible to start experiment. No settings file specified for gamer type " + gamerTypes[i] + ".");
	        					return;
	        				}
	        			}
	        		}
	        	}
	    		for (Class<?> gamerClass : ProjectSearcher.PROVER_GAMERS.getConcreteClasses()) {
	        		if(gamerClass.getSimpleName().equals(gamerTypes[i])){
	        			theCorrespondingClass = gamerClass;
	        		}
	        	}
	    		if(theCorrespondingClass == null){
	    			System.out.println("Impossible to start experiment. Unexisting gamer type " + gamerTypes[i] + ".");
	    			return;
	    		}
			}

			runNumber = Integer.parseInt(props.getProperty("runNumber"));

			if(runNumber == 0){
				System.out.println("Trying to start new experiment.");
				timeID = System.currentTimeMillis();
			}else{
				System.out.println("Trying to continue old experiment.");
				String timeIDString = props.getProperty("timeID");
				if(timeIDString == null){
					System.out.println("Impossible to continue experiment. Missing timeID of experiment.");
	    			return;
				}
				timeID = Long.parseLong(timeIDString);
			}

		} catch (IOException | NumberFormatException e) {
			System.out.println("Impossible to perform experiment, cannot correctly read/write the .properties file for the tourney.");
			return;
		}

		String mainLogFolder;

		for(String gameKey : gameKeys){
			mainLogFolder = timeID + "." + tourneyName + "." + gameKey + "." + "Tourney";

			File mainLogFolderFile = new File(mainLogFolder);

			if(!mainLogFolderFile.exists()){
				if(runNumber == 0){
					mainLogFolderFile.mkdirs();
				}else{ // If it's not the first run, the folder must already exist
					System.out.println("Impossible to continue tourney for game " + gameKey + "! The corresponding folder Doesn't exist! Skipping game.");
					continue;
				}
			}else{
				if(runNumber == 0){
					System.out.println("Impossible to start new tourney for game " + gameKey + "! Cannot create folder " + mainLogFolder + " for the tourney. A folder with the same name already exists! Skipping game.");
					continue;
				}
			}

	    	/** 2. Officially start the tourney and start logging. **/

	    	ThreadContext.put("LOG_FOLDER", mainLogFolder);
	    	GamerLogger.startFileLogging();

	    	String gamerTypesList = "[ ";
	    	for (String s : theGamersTypesString) {
	    		gamerTypesList += (s + " ");
	    	}
	    	gamerTypesList += "]";

	    	GamerLogger.log("TourneyRunner" + runNumber, "Starting tourney " + tourneyName + " for game " + gameKey + " with following settings: START_CLOCK=" +
	    			startClock + "s, PLAY_CLOCK=" + playClock + "s, PROPNET_CREATION_TIME=" + pnCreationTime + "ms, DESIRED_NUM_PARALLEL_PLAYERS=" +
	    			numParallelPlayers + ", MIN_NUM_MATCHES_PER_GAMER_TYPE=" + matchesPerGamerType + ", GAMER_TYPES=" + gamerTypesList + ".");

	    	/** 3. Compute all combinations of gamer types. **/

	    	game = gameRepo.getGame(gameKey);

	    	int expectedRoles = ExplicitRole.computeRoles(game.getRules()).size();
	    	List<List<Integer>> combinations = Combinator.getCombinations(theGamersTypesString.length, expectedRoles);

	    	int matchesPerCombination = (matchesPerGamerType / (Combinator.getLastCombinationsPerElement() * Combinator.getLastPermutationsPerCombination()));

	    	if(matchesPerGamerType%(Combinator.getLastCombinationsPerElement() * Combinator.getLastPermutationsPerCombination()) != 0){
	    		matchesPerCombination++;
	    	}

	    	int numParallelMatches = Math.round(((float) numParallelPlayers) / ((float) expectedRoles));

	    	GamerLogger.log("TourneyRunner" + runNumber, "Computed following parameters for tourney: NUM_ROLES=" + expectedRoles +
	    			", NUM_COMBINATIONS=" + combinations.size() + ", NUM_PARALLEL_MATCHES=" + numParallelMatches + ", ACTUAL_NUM_PARALLEL_PLAYERS=" +
	    			numParallelMatches*expectedRoles + ", ACTUAL_NUM_MATCHES_PER_COMBINATION=" + matchesPerCombination + ", ACTUAL_NUM_MATCHES_PER_GAMER_TYPE=" +
	    			(Combinator.getLastCombinationsPerElement() * Combinator.getLastPermutationsPerCombination() * matchesPerCombination));

	    	// 5. For each combination run the given amount of matches.

	    	for(List<Integer> combination : combinations){

	    		// Prompt the JVM to do garbage collection (not sure if really helpful).
	    	    long endGCTime = System.currentTimeMillis() + 3000;
	    	    for (int ii = 0; ii < 1000 && System.currentTimeMillis() < endGCTime; ii++){

	    	    	//System.out.println("Calling GC: " + System.currentTimeMillis());

	    	    	System.gc();
	    	        try {Thread.sleep(1);} catch (InterruptedException lEx) {/* Whatever */}
	    	    }

	    		/*System.out.println("Calling GC.");
	    		for(int i = 0; i < 10; i++){
	    			System.gc();
	    		}*/

	    		String comboIndices = "";
	    		List<String> theComboGamersTypes = new ArrayList<String>();
	    		for(Integer i : combination){
	    			theComboGamersTypes.add(theGamersTypesString[i.intValue()]);
	    			comboIndices += i.toString();
	    		}

	    		GamerLogger.log("TourneyRunner" + runNumber, "Starting sub-tourney for combination " + comboIndices + ".");

	    		ThreadContext.put("LOG_FOLDER", mainLogFolder + "/Combination" + comboIndices);
	    		boolean completed = runMatchesForCombination(runNumber, gameKey, startClock, playClock, pnCreationTime, theComboGamersTypes, numParallelMatches, matchesPerCombination);
	    		ThreadContext.put("LOG_FOLDER", mainLogFolder);

	    		if(completed){
	    			GamerLogger.log("TourneyRunner" + runNumber, "Ended sub-tourney for combination " + comboIndices + ".");
	    		}else{
	    			GamerLogger.logError("TourneyRunner" + runNumber, "Interrupted sub-tourney for combination " + comboIndices + ".");
	    		}
	    	}

	    	GamerLogger.log("TourneyRunner"+runNumber, "Tourney completed.");
		}

    	// At the end of the experiment, increase the run number
		try {
			reader = new FileReader(propertiesFile);

			Properties props = new Properties();

			// load the properties file:
			props.load(reader);

			reader.close();

			if(runNumber == 0){
				props.setProperty("timeID", ""+timeID);
			}
			props.setProperty("runNumber", ""+(runNumber+1));

		    FileWriter writer = new FileWriter(propertiesFile);

		    props.store(writer, null);

		    writer.close();

		} catch (IOException | NumberFormatException e) {
			System.out.println("Impossible to perform experiment, cannot correctly read/write the .properties file for the tourney.");
			return;
		}

	}



	private static boolean runMatchesForCombination(int runNumber, String gameKey, int startClock, int playClock,
			long pnCreationTime, List<String> theGamersTypes, int numParallelMatches, int matchesPerCombination){

		GamerLogger.log("TourneyRunner"+runNumber, "Starting sub-tourney.");

		// Create the executor as a pool with the desired number of threads
		// (corresponding to the number of matches we want to run in parallel).
		ExecutorService executor = Executors.newFixedThreadPool(numParallelMatches);

		// Create the settings for the process
		List<String> theSettings = new ArrayList<String>();

		theSettings.add("java");
		//theSettings.add("-Xmx:25g");
		theSettings.add("-jar");
		theSettings.add("IndependentSingleMatchRunner.jar");
		theSettings.add(ThreadContext.get("LOG_FOLDER"));
		theSettings.add("" + 0);
		theSettings.add(gameKey);
		theSettings.add("" + startClock);
		theSettings.add("" + playClock);
		theSettings.add("" + pnCreationTime);

		for(String s : theGamersTypes){
			theSettings.add(s);
		}


		for(int i = (runNumber*matchesPerCombination); i < ((runNumber+1)*matchesPerCombination); i++){
			theSettings.set(4, ""+i);
			executor.execute(new MatchProcessRunner(i, new ArrayList<String>(theSettings), ThreadContext.get("LOG_FOLDER") + "/MatchRunner" + i));
		}

		// Shutdown executor to tell it not to accept any more task to execute.
		// Note that this doesn't interrupt previously started tasks nor avoids executing previously submitted tasks.
		executor.shutdown();

		/*
		while(!(executor.isTerminated())){
			System.out.println("Threads ALL: " + ManagementFactory.getThreadMXBean().getThreadCount());
			System.out.println("Threads ACTIVE: " + Thread.activeCount());
			try {
				executor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/

		// Tell the executor to wait until all currently running tasks have completed execution.
		try {

			executor.awaitTermination(3, TimeUnit.DAYS);

		} catch (InterruptedException e) {
			executor.shutdownNow(); // Interrupt everything
			GamerLogger.logError("TourneyRunner"+runNumber, "Sub-tourney interrupted before completion.");
			GamerLogger.logStackTrace("TourneyRunner"+runNumber, e);
			Thread.currentThread().interrupt();
			return false;
		}

		if(!executor.isTerminated()){
			GamerLogger.logError("TourneyRunner"+runNumber, "Sub-tourney is taking too long. Interrupting it.");
			executor.shutdownNow(); // This instruction interrupts all threads.
			return false;
		}else{
			GamerLogger.log("TourneyRunner"+runNumber, "Sub-tourney completed.");
			return true;
		}
	}


}
