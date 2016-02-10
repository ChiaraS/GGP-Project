package csironi.ggp.course.experiments;

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
import org.ggp.base.util.statemachine.Role;

/**
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

		// Extract the desired configuration from the command line.
		String tourneyName = args[0];
		String gameKey = args[1];
		int startClock = Integer.valueOf(args[2]);
		int playClock = Integer.valueOf(args[3]);
		long creationTime = Long.valueOf(args[4]);
		int numParallelMatches = Integer.valueOf(args[5]);
		int matchesPerConfiguration = Integer.valueOf(args[6]);

		//Game game = GameRepository.getDefaultRepository().getGame(gameKey);

		GameRepository gameRepo = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");

		Game game = gameRepo.getGame(gameKey);

		int expectedRoles = Role.computeRoles(game.getRules()).size();
		if (2 != expectedRoles) {
			throw new RuntimeException("Game " + gameKey + "cannot be used to test the players because it is not a 2 players game.");
		}

		//Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1, game);

		ThreadContext.put("LOG_FOLDER", System.currentTimeMillis() + tourneyName);

		//GamerLogger.startFileLogging(fakeMatch, tourneyName);
		GamerLogger.startFileLogging();

		GamerLogger.log("TourneyRunner", "Starting tourney " + tourneyName + " for game " + gameKey + " with following settings: START_CLOCK=" +
				startClock + "s, PLAY_CLOCK=" + playClock + "s, PROPNET_CREATION_TIME=" + creationTime + "ms, NUM_PARALLEL_MATCHES=" +
				numParallelMatches + ", NUM_MATCHES_PER_CONFIG=" + matchesPerConfiguration + ".");

		List<Gdl> description = game.getRules();

		// Create the executor as a pool with the desired number of threads
		// (corresponding to the number of matches we want to run in parallel).
		ExecutorService executor = Executors.newFixedThreadPool(numParallelMatches);

		for(int i = 0; i < matchesPerConfiguration; i++){
			executor.execute(new MatchRunner(i, /*tourneyName,*/ game, description, startClock, playClock, creationTime, false));
			executor.execute(new MatchRunner((i+matchesPerConfiguration), /*tourneyName,*/ game, description, startClock, playClock, creationTime, true));
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
			GamerLogger.logError("TourneyRunner", "Tourney interrupted before completion.");
			GamerLogger.logStackTrace("TourneyRunner", e);
			Thread.currentThread().interrupt();
			return;
		}

		if(!executor.isTerminated()){
			GamerLogger.logError("TourneyRunner", "Tourney is taking too long. Interrupting it.");
			executor.shutdownNow(); // This instruction interrupts all threads.
		}else{
			GamerLogger.log("TourneyRunner", "Tourney completed.");
		}
	}

}