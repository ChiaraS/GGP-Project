package csironi.ggp.course.experiments.tournaments.independentprocesses;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.ThreadContext;
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

		String tourneyName;
		String gameKey;
		int startClock;
		int playClock;
		long pnCreationTime;
		// Number of parallel player threads that should run at the same time. NOTE that this requirement is loosely satisfied.
		// The actual number of player threads that will be running will be the closest feasible number of threads that allows
		// to run matches with the given number of roles for the game.
		int numParallelPlayers;
		int matchesPerGamerType;
		List<String> theGamersTypes;

		int runNumber;

		String mainLogFolder;

		Game game;

		//GameRepository gameRepo = GameRepository.getDefaultRepository();

    	GameRepository gameRepo = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");

		if(args.length == 1){

			System.out.println("Trying to continue old tourney.");

			mainLogFolder = args[0];

			File mainLogFolderFile = new File(mainLogFolder);

			if(!mainLogFolderFile.isDirectory()){
				System.out.println("Impossible to continue old tourney, cannot find the specified tourney folder.");
				return;
			}

			File propertiesFile = new File(mainLogFolder + "/tourney.properties");

			if(!propertiesFile.isFile()){
				System.out.println("Impossible to continue old tourney, cannot find the .properties file for the tourney.");
				return;
			}

			//System.out.println("!FoundFile");

			FileReader reader;
			try {
				reader = new FileReader(propertiesFile);

				Properties props = new Properties();

				// load the properties file:
				props.load(reader);

				reader.close();

				tourneyName = props.getProperty("tourneyName");
				gameKey = props.getProperty("gameKey");

		    	game = gameRepo.getGame(gameKey);

		    	if(game == null){
		    		System.out.println("Impossible to start tourney: specified game not found in the repository.");
					return;
		    	}

				startClock = Integer.parseInt(props.getProperty("startClock"));
				playClock = Integer.parseInt(props.getProperty("playClock"));
				pnCreationTime = Long.parseLong(props.getProperty("pnCreationTime"));
				numParallelPlayers = Integer.parseInt(props.getProperty("numParallelPlayers"));
				matchesPerGamerType = Integer.parseInt(props.getProperty("matchesPerGamerType"));

				String[] theGamersTypesString = props.getProperty("theGamersTypes").split(";");

				theGamersTypes = new ArrayList<String>();

				for(int i = 0; i < theGamersTypesString.length; i++){

					Class<?> theCorrespondingClass = null;

		    		//System.out.println(ProjectSearcher.INTERNAL_PROPNET_GAMERS.getConcreteClasses().size());

		    		for (Class<?> gamerClass : ProjectSearcher.INTERNAL_PROPNET_GAMERS.getConcreteClasses()) {

		    			//System.out.println(gamerClass.getSimpleName());

		        		if(gamerClass.getSimpleName().equals(theGamersTypesString[i])){
		        			theCorrespondingClass = gamerClass;
		        		}
		        	}
		    		if(theCorrespondingClass == null){
		    			System.out.println("Impossible to start tourney, unexisting gamer type " + theGamersTypesString[i] + ".");
		    			return;
		    		}else{
		    			theGamersTypes.add(theGamersTypesString[i]);
		    		}
				}

				runNumber = (Integer.parseInt(props.getProperty("runNumber")) + 1);

				props.setProperty("runNumber", ""+runNumber);

	    	    FileWriter writer = new FileWriter(propertiesFile);

	    	    props.store(writer, null);

	    	    writer.close();

			} catch (IOException | NumberFormatException e) {
				System.out.println("Impossible to continue old tourney, cannot correctly read/write the .properties file for the tourney.");
				return;
			}

		}else{

			System.out.println("Trying to start new tourney.");

			/** 1. Extract the desired configuration from the command line and check that all inputs are correct. **/

			if(args.length < 8){
				System.out.println("Impossible to start tourney, missing inputs. Please give the following parameters to the command line:");
				System.out.println("[tourneyName] [gameKey] [startClock(sec)] [playClock(sec)] [propnetCreationTime(ms)] [numParallelMatches] [matchesPerGamerType] [listOfGamersTypes]");
				return;
			}

			tourneyName = args[0];
			gameKey = args[1];

	    	game = gameRepo.getGame(gameKey);

	    	if(game == null){
	    		System.out.println("Impossible to start tourney: specified game not found in the repository.");
				return;
	    	}

			try{
				startClock = Integer.valueOf(args[2]);
			}catch(NumberFormatException e){
				System.out.println("Impossible to start tourney runner, wrong input. The start clock must be an integer value, not " + args[2] + ".");
				return;
			}

			try{
				playClock = Integer.valueOf(args[3]);
			}catch(NumberFormatException e){
				System.out.println("Impossible to start tourney runner, wrong input. The play clock must be an integer value, not " + args[3] + ".");
				return;
			}

			try{
				pnCreationTime = Long.valueOf(args[4]);
			}catch(NumberFormatException e){
				System.out.println("Impossible to start tourney runner, wrong input. The PropNet creation time must be a long value, not " + args[4] + ".");
				return;
			}

			try{
				numParallelPlayers = Integer.valueOf(args[5]);
			}catch(NumberFormatException e){
				System.out.println("Impossible to start tourney runner, wrong input. The number of parallel players must be an integer value, not " + args[5] + ".");
				return;
			}

			try{
				matchesPerGamerType = Integer.valueOf(args[6]);
			}catch(NumberFormatException e){
				System.out.println("Impossible to start tourney runner, wrong input. The number of matches per game must be an integer value, not " + args[6] + ".");
				return;
			}

			theGamersTypes = new ArrayList<String>();

			String gamerType;
	    	for (int i = 7; i < args.length; i++){
	    		gamerType = args[i];
	    		Class<?> theCorrespondingClass = null;

	    		//System.out.println(ProjectSearcher.INTERNAL_PROPNET_GAMERS.getConcreteClasses().size());

	    		for (Class<?> gamerClass : ProjectSearcher.INTERNAL_PROPNET_GAMERS.getConcreteClasses()) {

	    			//System.out.println(gamerClass.getSimpleName());

	        		if(gamerClass.getSimpleName().equals(gamerType)){
	        			theCorrespondingClass = gamerClass;
	        		}
	        	}
	    		for (Class<?> gamerClass : ProjectSearcher.PROVER_GAMERS.getConcreteClasses()) {

	    			//System.out.println(gamerClass.getSimpleName());

	        		if(gamerClass.getSimpleName().equals(gamerType)){
	        			theCorrespondingClass = gamerClass;
	        		}
	        	}
	    		if(theCorrespondingClass == null){
	    			System.out.println("Impossible to start tourney, unexisting gamer type " + gamerType + ".");
	    			return;
	    		}else{
	    			theGamersTypes.add(gamerType);
	    		}
			}

	    	runNumber = 0;

	    	mainLogFolder = System.currentTimeMillis() + "." + tourneyName;

	    	/////////////////////////////////////////////////////////////////////////////////////////////
	    	// Save in a properties file the settings of the tournament so that they can be used in case
	    	// we want to extend the same tourney in the future by adding more matches.
	    	File tourneyProperties = new File(mainLogFolder + "/tourney.properties");

	    	try {

	    		if(!tourneyProperties.exists()){
	    			File mainLogFolderFile = new File(mainLogFolder);

	    			if(!mainLogFolderFile.exists()){
	    				mainLogFolderFile.mkdirs();
	    			}
		    		tourneyProperties.createNewFile();
		    	}

	    	    Properties props = new Properties();
	    	    props.setProperty("tourneyName", tourneyName);
	    	    props.setProperty("gameKey", gameKey);
	    	    props.setProperty("startClock", ""+startClock);
	    	    props.setProperty("playClock", ""+playClock);
	    	    props.setProperty("pnCreationTime", ""+pnCreationTime);
	    	    props.setProperty("numParallelPlayers", ""+numParallelPlayers);
	    	    props.setProperty("matchesPerGamerType", ""+matchesPerGamerType);

	    	    String gamersTypesString = "";
	    	    for(String s: theGamersTypes){
	    	    	gamersTypesString+= (s + ";");
	    	    }

	    	    props.setProperty("theGamersTypes", gamersTypesString);
	    	    props.setProperty("runNumber", ""+runNumber);

	    	    FileWriter writer = new FileWriter(tourneyProperties);

	    	    props.store(writer, null);

	    	    writer.close();
	    	} catch (IOException ex) {
	    		System.out.println("Cannot write .properties file for the tourney.");
	    		tourneyProperties.delete();
	    		ex.printStackTrace();
	    	}

	    	///////////////////////////////////////////////////////////////////////////////////////////////
		}

		//System.out.println("!OfficiallyStarting");

    	/** 2. Officially start the tourney and start logging. **/

    	ThreadContext.put("LOG_FOLDER", mainLogFolder);
    	GamerLogger.startFileLogging();

    	String gamerTypesList = "[ ";
    	for (String s : theGamersTypes) {
    		gamerTypesList += (s + " ");
    	}
    	gamerTypesList += "]";

    	GamerLogger.log("TourneyRunner"+runNumber, "Starting tourney " + tourneyName + " for game " + gameKey + " with following settings: START_CLOCK=" +
    			startClock + "s, PLAY_CLOCK=" + playClock + "s, PROPNET_CREATION_TIME=" + pnCreationTime + "ms, DESIRED_NUM_PARALLEL_PLAYERS=" +
    			numParallelPlayers + ", MIN_NUM_MATCHES_PER_GAMER_TYPE=" + matchesPerGamerType + ", GAMER_TYPES=" + gamerTypesList + ".");

    	/** 3. Compute all combinations of gamer types. **/

    	int expectedRoles = ExplicitRole.computeRoles(game.getRules()).size();
    	List<List<Integer>> combinations = Combinator.getCombinations(theGamersTypes.size(), expectedRoles);

    	int matchesPerCombination = (matchesPerGamerType / (Combinator.getLastCombinationsPerElement() * Combinator.getLastPermutationsPerCombination()));

    	if(matchesPerGamerType%(Combinator.getLastCombinationsPerElement() * Combinator.getLastPermutationsPerCombination()) != 0){
    		matchesPerCombination++;
    	}

    	int numParallelMatches = Math.round(((float) numParallelPlayers) / ((float) expectedRoles));

    	GamerLogger.log("TourneyRunner"+runNumber, "Computed following parameters for tourney: NUM_ROLES=" + expectedRoles +
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
    			theComboGamersTypes.add(theGamersTypes.get(i.intValue()));
    			comboIndices += i.toString();
    		}

    		GamerLogger.log("TourneyRunner"+runNumber, "Starting sub-tourney for combination " + comboIndices + ".");

    		ThreadContext.put("LOG_FOLDER", mainLogFolder + "/Combination" + comboIndices);
    		boolean completed = runMatchesForCombination(runNumber, gameKey, startClock, playClock, pnCreationTime, theComboGamersTypes, numParallelMatches, matchesPerCombination);
    		ThreadContext.put("LOG_FOLDER", mainLogFolder);

    		if(completed){
    			GamerLogger.log("TourneyRunner"+runNumber, "Ended sub-tourney for combination " + comboIndices + ".");
    		}else{
    			GamerLogger.logError("TourneyRunner"+runNumber, "Interrupted sub-tourney for combination " + comboIndices + ".");
    		}
    	}

    	GamerLogger.log("TourneyRunner"+runNumber, "Tourney completed.");

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
		theSettings.add("GMCTUNE03IndependentSingleMatchRunner.jar");
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
