package csironi.ggp.course.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.GamePlayer;
import org.ggp.base.player.gamer.statemachine.MCTS.SlowDUCTMCTSGamer;
import org.ggp.base.player.gamer.statemachine.MCTS.SlowSUCTMCTSGamer;
import org.ggp.base.server.GameServer;
import org.ggp.base.server.exception.GameServerException;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetCreationManager;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;

public class MatchRunner extends Thread{

	private int ID;

	private String tourneyName;
	private Game game;
	private String gameKey;
	private List<Gdl> description;
	private int startClock;
	private int playClock;
	private long creationTime;
	private boolean invert;

	public MatchRunner(int ID, String tourneyName, Game game, List<Gdl> description, int startClock, int playClock,
			long creationTime, boolean invert){

		this.ID = ID;
		this.tourneyName = tourneyName;
		this.game = game;
		this.gameKey = game.getKey();
		this.description = description;
		this.startClock = startClock;
		this.playClock = playClock;
		this.creationTime = creationTime;
		this.invert = invert;

	}

	@Override
	public void run(){

		System.out.println("Starting " + this.ID);

		String matchName = this.ID + "." + this.gameKey + "." + System.currentTimeMillis();

		String oldFolder = ThreadContext.get("LOG_FOLDER");

		if(oldFolder == null){
			ThreadContext.put("LOG_FOLDER", "MatchRunner" + this.ID);
		}else{
			ThreadContext.put("LOG_FOLDER", oldFolder + "/" + "MatchRunner" + this.ID);
		}

		GamerLogger.log("MatchRunner", "Started MatchRunner " + this.ID + ".");

		GamerLogger.log("MatchRunner", "Starting new match: " + matchName);

		// Create the executor service that will run the propnet manager that creates the propnet
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Create the propnet creation manager
        SeparateInternalPropnetCreationManager manager = new SeparateInternalPropnetCreationManager(this.description, System.currentTimeMillis() + creationTime);

        // Start the manager
  	  	executor.execute(manager);

  	  	// Shutdown executor to tell it not to accept any more task to execute.
		// Note that this doesn't interrupt previously started tasks.
		executor.shutdown();

		// Tell the executor to wait until the currently running task has completed execution or the timeout has elapsed.
		try{
			executor.awaitTermination(creationTime, TimeUnit.MILLISECONDS);
		}catch(InterruptedException e){ // The thread running the verifier has been interrupted => stop the test
			executor.shutdownNow(); // Interrupt everything
			GamerLogger.logError("MatchRunner", "Program interrupted. Match of game "+ this.gameKey +" won't be performed.");
			GamerLogger.logStackTrace("MatchRunner", e);
			this.resetLogFolder(oldFolder);
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
				GamerLogger.logError("MatchRunner", "Program interrupted. Match of game "+ this.gameKey +" won't be performed.");
				GamerLogger.logStackTrace("MatchRunner", e);
				this.resetLogFolder(oldFolder);
				Thread.currentThread().interrupt();
				return;
			}
		}

		// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.
		if(manager.getImmutablePropnet() == null || manager.getInitialPropnetState() == null){
			GamerLogger.logError("MatchRunner", "Impossible to play the match. Propnet and/or propnet state are null.");
			this.resetLogFolder(oldFolder);
			return;
		}

		InternalPropnetStateMachine theMachine1 = new SeparateInternalPropnetStateMachine(manager.getImmutablePropnet(), manager.getInitialPropnetState());

		InternalPropnetStateMachine theMachine2 = new SeparateInternalPropnetStateMachine(manager.getImmutablePropnet(), manager.getInitialPropnetState());

		SlowDUCTMCTSGamer ductGamer = new SlowDUCTMCTSGamer(theMachine1);

		SlowSUCTMCTSGamer suctGamer = new SlowSUCTMCTSGamer(theMachine2);

		GamePlayer ductPlayer = null;
		try {
			ductPlayer = new GamePlayer(9147 + (this.ID * 2), ductGamer);
		} catch (IOException e) {
			GamerLogger.logError("MatchRunner", "Impossible to play the match. Error when creating game player.");
			GamerLogger.logStackTrace("MatchRunner", e);
			this.resetLogFolder(oldFolder);
			return;
		}
		GamePlayer suctPlayer = null;
		try {
			suctPlayer = new GamePlayer(9148 + (this.ID * 2), suctGamer);
		} catch (IOException e) {
			GamerLogger.logError("MatchRunner", "Impossible to play the match. Error when creating game player.");
			GamerLogger.logStackTrace("MatchRunner", e);
			this.resetLogFolder(oldFolder);
			return;
		}

		int ductPort = ductPlayer.getGamerPort();
		int suctPort = suctPlayer.getGamerPort();

		ductPlayer.start();
		suctPlayer.start();

		List<String> hostNames = new ArrayList<String>();
		List<String> playerNames = new ArrayList<String>();
		List<Integer> portNumbers = new ArrayList<Integer>();

		hostNames.add("127.0.0.1");
		hostNames.add("127.0.0.1");

		if(this.invert){

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

		Match match = new Match(matchName, -1, this.startClock, this.playClock, game);
		match.setPlayerNamesFromHost(playerNames);

		// Actually run the match, using the desired configuration.
		GameServer server;
		try {
			server = new GameServer(match, hostNames, portNumbers);
		} catch (GameServerException e) {
			GamerLogger.logError("MatchRunner", "Impossible to play the match. Error when creating game server.");
			GamerLogger.logStackTrace("MatchRunner", e);
			ductPlayer.shutdown();
			suctPlayer.shutdown();
			this.resetLogFolder(oldFolder);
			return;
		}
		server.start();
		try {
			server.join();
		} catch (InterruptedException e) {
			GamerLogger.logError("MatchRunner", "Program interrupted. Impossible to complete the match.");
			GamerLogger.logStackTrace("MatchRunner", e);
			server.abort();
			ductPlayer.shutdown();
			suctPlayer.shutdown();
			this.resetLogFolder(oldFolder);
			Thread.currentThread().interrupt();
			return;
		}

		GamerLogger.log("MatchRunner", "Execution of match " + matchName + " completed.");

		// Open up the directory for this tournament.
		//File f = new File(oldFolder);
		//if (!f.exists()) {
		//	f.mkdir();
		//}

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
		File f = new File(ThreadContext.get("LOG_FOLDER") + "/" + matchName + ".json");
		if (f.exists()) f.delete();
		try {
			bw = new BufferedWriter(new FileWriter(f));
			bw.write(match.toJSON());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			GamerLogger.logError("MatchRunner", "Match completed correctly, but impossible to save match information on JSON file.");
			GamerLogger.logStackTrace("MatchRunner", e);
		}

		// Save the goals in the "/scores" file for the tournament.

		List<Integer> goals;
		try {
			goals = server.getGoals();
		} catch (GoalDefinitionException | StateMachineException e) {
			GamerLogger.logError("MatchRunner", "Match completed correctly, but impossible to save final scores.");
			GamerLogger.logStackTrace("MatchRunner", e);
			this.resetLogFolder(oldFolder);
			return;
		}

		//this.resetLogFolder(oldFolder);

		String toLog = this.ID + ";" + matchName + ";";

		for(int i = 0; i < goals.size(); i++){

			toLog += playerNames.get(i) + ";" + goals.get(i) + ";";

		}

		//GamerLogger.logSync(GamerLogger.FORMAT.CSV_FORMAT, "scores", toLog);

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "scores", toLog);

		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){

		}

		ductPlayer.shutdown();
		suctPlayer.shutdown();

		this.resetLogFolder(oldFolder);

		System.out.println("Ending " + this.ID);

	}

	private void resetLogFolder(String oldFolder){
		if(oldFolder == null){
			ThreadContext.remove("LOG_FOLDER");
		}else{
			ThreadContext.put("LOG_FOLDER", oldFolder);
		}
	}

}
