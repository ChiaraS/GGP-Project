package csironi.ggp.course.LoggerTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ggp.base.player.GamePlayer;
import org.ggp.base.player.L4J2GamePlayer;
import org.ggp.base.player.gamer.statemachine.MCTS.GLTestSlowDUCTGamer;
import org.ggp.base.player.gamer.statemachine.MCTS.L4J2TestSlowDUCTGamer;
import org.ggp.base.server.GameServer;
import org.ggp.base.server.exception.GameServerException;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.factory.exceptions.GdlFormatException;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.propnet.creationManager.SeparatePropnetCreationManager;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.L4J2InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.implementation.propnet.L4J2SeparateInternalPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;

/**
 * This class runs a match of a game between a player that uses the GamerLogger and a player that uses Log4J2.
 * Both players will be given a copy of the same propnet to keep the comparison fair.
 *
 * Arguments of main method:
 * - [tourneyName] = name of the tournament.
 * - [gameKey] = key of the game for which to run a match.
 * - [startClock] = time available for metagaming.
 * - [playClock] = time available to choose an action at every game step.
 * - [initializationTime] = time available to build the propnet.
 *
 * @author C.Sironi
 *
 */
public class LoggersComparison {

	static{

		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
		System.setProperty("isThreadContextMapInheritable", "true");

	}

	public static void main(String[] args) throws IOException, SymbolFormatException, GdlFormatException, InterruptedException, GoalDefinitionException, GameServerException, StateMachineException
	{
		// Extract the desired configuration from the command line.
		String tourneyName = args[0];
		String gameKey = args[1];
		Game game = GameRepository.getDefaultRepository().getGame(gameKey);
		int startClock = Integer.valueOf(args[2]);
		int playClock = Integer.valueOf(args[3]);
		long initializationTime = Long.valueOf(args[4]);
		boolean invert = Boolean.parseBoolean(args[5]);

		int expectedRoles = Role.computeRoles(game.getRules()).size();
		if (2 != expectedRoles) {
			throw new RuntimeException("Game " + gameKey + "cannot be used to test the loggers because it is not a 2 players game.");
		}

		GamerLogger.setSpilloverLogfile(System.currentTimeMillis() + "LoggerComparison.log");

		// Create the propnet
		GameRepository theRepository = GameRepository.getDefaultRepository();

		List<Gdl> description = theRepository.getGame(gameKey).getRules();

        // Create the executor service that will run the propnet manager that creates the propnet
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Create the propnet creation manager
        SeparatePropnetCreationManager manager = new SeparatePropnetCreationManager(description, System.currentTimeMillis() + initializationTime);

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
			GamerLogger.logError("LoggerComparison", "State machine verification interrupted. Test on game "+ gameKey +" won't be completed.");
			GamerLogger.logStackTrace("LoggerComparison", e);
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
				GamerLogger.logError("LoggerComparison", "State machine verification interrupted. Test on game "+ gameKey +" won't be completed.");
				GamerLogger.logStackTrace("LoggerComparison", e);
				Thread.currentThread().interrupt();
				return;
			}
		}

		// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.
		if(manager.getImmutablePropnet() == null || manager.getInitialPropnetState() == null){
			GamerLogger.logError("LoggerComparison", "Impossible to play the match. Propnet and/or propnet state are null.");
		}

		InternalPropnetStateMachine theGLMachine = new SeparateInternalPropnetStateMachine(manager.getImmutablePropnet(), manager.getInitialPropnetState());

		L4J2InternalPropnetStateMachine theL4J2Machine = new L4J2SeparateInternalPropnetStateMachine(manager.getImmutablePropnet(), manager.getInitialPropnetState());

		GLTestSlowDUCTGamer glGamer = new GLTestSlowDUCTGamer(theGLMachine);

		L4J2TestSlowDUCTGamer l4j2Gamer = new L4J2TestSlowDUCTGamer(theL4J2Machine);

		GamePlayer glPlayer =  new GamePlayer(9147, glGamer);
		L4J2GamePlayer l4j2Player = new L4J2GamePlayer(9148, l4j2Gamer);

		int glPort = glPlayer.getGamerPort();
		int l4j2Port = l4j2Player.getGamerPort();

		glPlayer.start();
		l4j2Player.start();

		List<String> hostNames = new ArrayList<String>();
		List<String> playerNames = new ArrayList<String>();
		List<Integer> portNumbers = new ArrayList<Integer>();

		hostNames.add("127.0.0.1");
		hostNames.add("127.0.0.1");

		if(invert){

			portNumbers.add(l4j2Port);
			portNumbers.add(glPort);

			playerNames.add("L4J2");
			playerNames.add("GL");

		}else{
			portNumbers.add(glPort);
			portNumbers.add(l4j2Port);

			playerNames.add("GL");
			playerNames.add("L4J2");
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

		glPlayer.shutdown();
		l4j2Player.shutdown();
	}

}
