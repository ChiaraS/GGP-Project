package csironi.ggp.course.experiments.tournaments.independentprocesses;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.Role;

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

		/** 1. Extract the desired configuration from the command line and check that all inputs are correct. **/

		if(args.length < 8){
			System.out.println("Impossible to start tourney, missing inputs. Please give the following parameters to the command line:");
			System.out.println("[tourneyName] [gameKey] [startClock(sec)] [playClock(sec)] [propnetCreationTime(ms)] [numParallelMatches] [matchesPerGamerType] [listOfGamersTypes]");
			return;
		}

		String tourneyName;
		String gameKey;
		int startClock;
		int playClock;
		long pnCreationTime;
		int numParallelMatches;
		int matchesPerGamerType;
		List<String> theGamersTypes;

		tourneyName = args[0];
		gameKey = args[1];

		GameRepository gameRepo = GameRepository.getDefaultRepository();

    	//GameRepository gameRepo = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");

    	Game game = gameRepo.getGame(gameKey);

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
			numParallelMatches = Integer.valueOf(args[5]);
		}catch(NumberFormatException e){
			System.out.println("Impossible to start tourney runner, wrong input. The number of parallel matches must be an integer value, not " + args[5] + ".");
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
    		if(theCorrespondingClass == null){
    			System.out.println("Impossible to start tourney, unexisting gamer type " + gamerType + ".");
    			return;
    		}else{
    			theGamersTypes.add(gamerType);
    		}
		}

    	/** 2. Officially start the tourney and start logging. **/

    	String mainLogFolder = System.currentTimeMillis() + "." + tourneyName;
    	ThreadContext.put("LOG_FOLDER", mainLogFolder);
    	GamerLogger.startFileLogging();

    	String gamerTypesList = "[ ";
    	for (String s : theGamersTypes) {
    		gamerTypesList += (s + " ");
    	}
    	gamerTypesList += "]";

    	GamerLogger.log("TourneyRunner", "Starting tourney " + tourneyName + " for game " + gameKey + " with following settings: START_CLOCK=" +
    			startClock + "s, PLAY_CLOCK=" + playClock + "s, PROPNET_CREATION_TIME=" + pnCreationTime + "ms, NUM_PARALLEL_MATCHES=" +
    			numParallelMatches + ", NUM_MATCHES_PER_GAMER_TYPE=" + matchesPerGamerType + ", GAMER_TYPES=" + gamerTypesList + ".");

    	/** 2. Compute all combinations of gamer types. **/

    	int expectedRoles = Role.computeRoles(game.getRules()).size();
    	List<List<Integer>> combinations = Combinator.getCombinations(theGamersTypes.size(), expectedRoles);

    	int matchesPerCombination = (matchesPerGamerType / (Combinator.getLastCombinationsPerElement() * Combinator.getLastPermutationsPerCombination()));

    	if(matchesPerGamerType%(Combinator.getLastCombinationsPerElement() * Combinator.getLastPermutationsPerCombination()) != 0){
    		matchesPerCombination++;
    	}

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

    		GamerLogger.log("TourneyRunner", "Starting sub-tourney for combination " + comboIndices + ".");

    		ThreadContext.put("LOG_FOLDER", mainLogFolder + "/Combination" + comboIndices);
    		boolean completed = runMatchesForCombination(gameKey, startClock, playClock, pnCreationTime, theComboGamersTypes, numParallelMatches, matchesPerCombination);
    		ThreadContext.put("LOG_FOLDER", mainLogFolder);

    		if(completed){
    			GamerLogger.log("TourneyRunner", "Ended sub-tourney for combination " + comboIndices + ".");
    		}else{
    			GamerLogger.logError("TourneyRunner", "Interrupted sub-tourney for combination " + comboIndices + ".");
    		}
    	}

    	GamerLogger.log("TourneyRunner", "Tourney completed.");

	}



	private static boolean runMatchesForCombination(String gameKey, int startClock, int playClock,
			long pnCreationTime, List<String> theGamersTypes, int numParallelMatches, int matchesPerCombination){

		GamerLogger.log("TourneyRunner", "Starting sub-tourney.");

		// Create the executor as a pool with the desired number of threads
		// (corresponding to the number of matches we want to run in parallel).
		ExecutorService executor = Executors.newFixedThreadPool(numParallelMatches);

		// Create the settings for the process
		List<String> theSettings = new ArrayList<String>();

		theSettings.add("java");
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


		for(int i = 0; i < matchesPerCombination; i++){
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
			GamerLogger.logError("TourneyRunner", "Sub-tourney interrupted before completion.");
			GamerLogger.logStackTrace("TourneyRunner", e);
			Thread.currentThread().interrupt();
			return false;
		}

		if(!executor.isTerminated()){
			GamerLogger.logError("TourneyRunner", "Sub-tourney is taking too long. Interrupting it.");
			executor.shutdownNow(); // This instruction interrupts all threads.
			return false;
		}else{
			GamerLogger.log("TourneyRunner", "Sub-tourney completed.");
			return true;
		}
	}


}
