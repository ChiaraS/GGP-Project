package csironi.ggp.course.experiments.tournaments.singleprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
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
public class TourneyRunner {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	/**
	 *
	 */
	public TourneyRunner() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length < 8){
			System.out.println("Impossible to start tourney, missing inputs. Please give the following parameters to the command line:");
			System.out.println("[tourneyName] [gameKey] [startClock(sec)] [playClock(sec)] [propnetCreationTime(ms)] [numParallelMatches] [matchesPerConfiguration] [listOfGamersTypes]");
			return;
		}

		// 1. Extract the desired configuration from the command line.
		String tourneyName = args[0];
		String gameKey = args[1];
		int startClock = Integer.valueOf(args[2]);
		int playClock = Integer.valueOf(args[3]);
		long creationTime = Long.valueOf(args[4]);
		int numParallelMatches = Integer.valueOf(args[5]);
		int matchesPerGamerType = Integer.valueOf(args[6]);

		List<Class<?>> gamersClasses = new ArrayList<Class<?>>();

		String gamerType;
    	for (int i = 7; i < args.length; i++){
    		gamerType = args[i];
    		Class<?> theCorrespondingClass = null;
    		for (Class<?> gamerClass : ProjectSearcher.INTERNAL_PROPNET_GAMERS.getConcreteClasses()) {
        		if(gamerClass.getSimpleName().equals(gamerType)){
        			theCorrespondingClass = gamerClass;
        		}
        	}
    		if(theCorrespondingClass == null){
    			System.out.println("Impossible to start tourney, unexisting gamer type " + gamerType + ".");
    			return;
    		}else{
    			gamersClasses.add(theCorrespondingClass);
    		}
		}

    	// 2. Get the game and check that the number of gamer types is not greater than the number of roles in the game.

    	//Game game = GameRepository.getDefaultRepository().getGame(gameKey);

    	GameRepository gameRepo = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");

    	Game game = gameRepo.getGame(gameKey);

    	if(game == null){
    		System.out.println("Impossible to start tourney: specified game not found in the repository.");
			return;
    	}

    	int expectedRoles = Role.computeRoles(game.getRules()).size();
    	/*if (gamersClasses.size() > expectedRoles) {
    		System.out.println("Impossible to start tourney: number of gamer types to test is bigger than the number of roles in the game.");
			return;
    	}*/

    	// 3. Inputs are correct, officially start the tourney.

    	String mainLogFolder = System.currentTimeMillis() + "." + tourneyName;
    	ThreadContext.put("LOG_FOLDER", mainLogFolder);

    	GamerLogger.startFileLogging();

    	String gamerTypesList = "[ ";
    	for (Class<?> gamerClass : gamersClasses) {
    		gamerTypesList += (gamerClass.getSimpleName() + " ");
    	}
    	gamerTypesList += "]";

    	GamerLogger.log("TourneyRunner", "Starting tourney " + tourneyName + " for game " + gameKey + " with following settings: START_CLOCK=" +
    			startClock + "s, PLAY_CLOCK=" + playClock + "s, PROPNET_CREATION_TIME=" + creationTime + "ms, NUM_PARALLEL_MATCHES=" +
    			numParallelMatches + ", NUM_MATCHES_PER_GAMER_TYPE=" + matchesPerGamerType + ", GAMER_TYPES=" + gamerTypesList + ".");

    	// 4. Compute all combinations of gamer types.
    	List<List<Integer>> combinations = Combinator.getCombinations(gamersClasses.size(), expectedRoles);

    	int matchesPerCombination = (matchesPerGamerType / (Combinator.getLastCombinationsPerElement() * Combinator.getLastPermutationsPerCombination()));

    	if(matchesPerGamerType%(Combinator.getLastCombinationsPerElement() * Combinator.getLastPermutationsPerCombination()) != 0){
    		matchesPerCombination++;
    	}

    	// 5. For each combination run the given amount of matches.
    	List<Gdl> description = game.getRules();

    	for(List<Integer> combination : combinations){

    		// Prompt the JVM to do garbage collection, because we've hopefully just freed a lot of stuff.
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
    		List<Class<?>> combinationClasses = new ArrayList<Class<?>>();
    		for(Integer i : combination){
    			combinationClasses.add(gamersClasses.get(i.intValue()));
    			comboIndices += i.toString();
    		}

    		GamerLogger.log("TourneyRunner", "Starting sub-tourney for combination " + comboIndices + ".");

    		ThreadContext.put("LOG_FOLDER", mainLogFolder + "/Combination" + comboIndices);
    		boolean completed = runMatchesForCombination(game, description, startClock, playClock, creationTime, combinationClasses, numParallelMatches, matchesPerCombination);
    		ThreadContext.put("LOG_FOLDER", mainLogFolder);

    		if(completed){
    			GamerLogger.log("TourneyRunner", "Ended sub-tourney for combination " + comboIndices + ".");
    		}else{
    			GamerLogger.logError("TourneyRunner", "Interrupted sub-tourney for combination " + comboIndices + ".");
    		}
    	}

    	GamerLogger.log("TourneyRunner", "Tourney completed.");

	}



	private static boolean runMatchesForCombination(Game game, List<Gdl> description, int startClock, int playClock,
			long creationTime, List<Class<?>> combinationClasses, int numParallelMatches, int matchesPerCombination){

		GamerLogger.log("TourneyRunner", "Starting sub-tourney.");

		// Create the executor as a pool with the desired number of threads
		// (corresponding to the number of matches we want to run in parallel).
		ExecutorService executor = Executors.newFixedThreadPool(numParallelMatches);

		for(int i = 0; i < matchesPerCombination; i++){
			executor.execute(new MatchRunner(i, game, description, startClock, playClock, creationTime, combinationClasses));
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