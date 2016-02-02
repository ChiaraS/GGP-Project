/**
 *
 */
package csironi.ggp.course.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ggp.base.player.GamePlayer;
import org.ggp.base.server.GameServer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetCreationManager;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.implementation.internalPropnet.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.internalPropnet.SeparateInternalPropnetStateMachine;

/**
 * @author C.Sironi
 *
 */
public class MatchRunner {

	static{

		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
		System.setProperty("isThreadContextMapInheritable", "true");

		LOGGER = LogManager.getRootLogger();

	}

	/**
	 * Static reference to the logger
	 */
	private static final Logger LOGGER;


	public void runMatch(String tourneyName, String gameKey, int startClock, int playClock,
			long initializationTime, boolean invert){

		Game game = GameRepository.getDefaultRepository().getGame(gameKey);

		int expectedRoles = Role.computeRoles(game.getRules()).size();
		if (2 != expectedRoles) {
			throw new RuntimeException("Game " + gameKey + "cannot be used to test the gamers because it is not a 2 players game.");
		}

		ThreadContext.put("LOG_FOLDER", System.currentTimeMillis() + "MatchRunner");
		ThreadContext.put("LOG_FILE", "MatchRunner.log");

		// Create the propnet
		GameRepository theRepository = GameRepository.getDefaultRepository();

		List<Gdl> description = theRepository.getGame(gameKey).getRules();

        // Create the executor service that will run the propnet manager that creates the propnet
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Create the propnet creation manager
        SeparateInternalPropnetCreationManager manager = new SeparateInternalPropnetCreationManager(description, System.currentTimeMillis() + initializationTime);

        // Start the manager
  	  	executor.execute(manager);

  	  	// Shutdown executor to tell it not to accept any more task to execute.
		// Note that this doesn't interrupt previously started tasks.
		executor.shutdown();

		// Tell the executor to wait until the currently running task has completed execution or the timeout has elapsed.
		try{
			executor.awaitTermination(initializationTime, TimeUnit.MILLISECONDS);
		}catch(InterruptedException e){ // The thread running the verifier has been interrupted => stop the test
			executor.shutdownNow(); // Interrupt everything
			LOGGER.error("[MatchRunner] Program interrupted. Test on game "+ gameKey +" won't be completed.", e);
			Thread.currentThread().interrupt();
			return;
		}

		// Here the available time has elapsed, so we must interrupt the thread if it is still running.
		executor.shutdownNow();

		// Wait for the thread to actually terminate
		while(!executor.isTerminated()){

			// If the thread didn't terminate, wait for a minute and then check again
			try{
				executor.awaitTermination(1, TimeUnit.MINUTES);
			}catch(InterruptedException e) {
				// If this exception is thrown it means the thread that is executing the verification
				// of the state machine has been interrupted. If we do nothing this state machine could be stuck in the
				// while loop anyway until all tasks in the executor have terminated, thus we break out of the loop and return.
				// What happens to the still running tasks in the executor? Who will make sure they terminate?
				LOGGER.error("[MatchRunner] Program interrupted. Test on game "+ gameKey +" won't be completed.", e);
				Thread.currentThread().interrupt();
				return;
			}
		}

		// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.
		if(manager.getImmutablePropnet() == null || manager.getInitialPropnetState() == null){
			LOGGER.error("[MatchRunner] Impossible to play the match. Propnet and/or propnet state are null.");
		}

		InternalPropnetStateMachine theMachine1 = new SeparateInternalPropnetStateMachine(manager.getImmutablePropnet(), manager.getInitialPropnetState());

		InternalPropnetStateMachine theMachine2 = new SeparateInternalPropnetStateMachine(manager.getImmutablePropnet(), manager.getInitialPropnetState());

		GLTestSlowDUCTGamer ductGamer = new GLTestSlowDUCTGamer(theMachine1);

		L4J2TestSlowDUCTGamer suctGamer = new L4J2TestSlowDUCTGamer(theMachine2);

		GamePlayer ductPlayer =  new GamePlayer(9147, ductGamer);
		GamePlayer suctPlayer = new GamePlayer(9148, suctGamer);

		int ductPort = ductPlayer.getGamerPort();
		int suctPort = suctPlayer.getGamerPort();

		ductPlayer.start();
		suctPlayer.start();

		List<String> hostNames = new ArrayList<String>();
		List<String> playerNames = new ArrayList<String>();
		List<Integer> portNumbers = new ArrayList<Integer>();

		hostNames.add("127.0.0.1");
		hostNames.add("127.0.0.1");

		if(invert){

			portNumbers.add(suctPort);
			portNumbers.add(ductPort);

			playerNames.add("SUCT");
			playerNames.add("DUCT");

		}else{
			portNumbers.add(ductPort);
			portNumbers.add(suctPort);

			playerNames.add("DUCT");
			playerNames.add("SUCT");
		}

		String matchName = tourneyName + "." + gameKey + "." + System.currentTimeMillis();
		Match match = new Match(matchName, -1, startClock, playClock, game);
		match.setPlayerNamesFromHost(playerNames);

		// Actually run the match, using the desired configuration.
		GameServer server = new GameServer(match, hostNames, portNumbers);
		server.start();
		server.join();

		// Open up the directory for this tournament.
		// Create a "scores" file if none exists.
		File f = new File(tourneyName);
		if (!f.exists()) {
			f.mkdir();
			f = new File(tourneyName + "/scores");
			f.createNewFile();
		}


		BufferedWriter bw;

		/**
		 * Do not save the match history in an XML
		 */
		/*
		// Open up the XML file for this match, and save the match there.
		f = new File(tourneyName + "/" + matchName + ".xml");
		if (f.exists()) f.delete();
		bw = new BufferedWriter(new FileWriter(f));
		bw.write(match.toXML());
		bw.flush();
		bw.close();
		*/



		// Open up the JSON file for this match, and save the match there.
		f = new File(tourneyName + "/" + matchName + ".json");
		if (f.exists()) f.delete();
		bw = new BufferedWriter(new FileWriter(f));
		bw.write(match.toJSON());
		bw.flush();
		bw.close();


		// Save the goals in the "/scores" file for the tournament.
		bw = new BufferedWriter(new FileWriter(tourneyName + "/scores", true));
		List<Integer> goals = server.getGoals();
		String goalStr = "";
		String playerStr = "";
		for (int i = 0; i < goals.size(); i++)
		{
			Integer goal = server.getGoals().get(i);
			goalStr += Integer.toString(goal);
			playerStr += playerNames.get(i);
			if (i != goals.size() - 1)
			{
				playerStr += ",";
				goalStr += ",";
			}
		}
		bw.write("\n" + playerStr + "=" + goalStr);
		bw.flush();
		bw.close();

		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){

		}

		ductPlayer.shutdown();
		suctPlayer.shutdown();

	}

}
