/**
 *
 */
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.GamePlayer;
import org.ggp.base.player.gamer.statemachine.MCTS.SlowDUCTMCTSGamer;
import org.ggp.base.player.gamer.statemachine.MCTS.SlowSUCTMCTSGamer;
import org.ggp.base.server.GameServer;
import org.ggp.base.server.exception.GameServerException;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetCreationManager;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.implementation.internalPropnet.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.implementation.internalPropnet.SeparateInternalPropnetStateMachine;

/**
 * ATTENTION! ALWAYS MAKE SURE THAT LOG_FOLDER IS SET IN THREADCONTEXT BEFORE RUNNING THIS THREAD!
 * OTHERWISE THERE WILL BE A PROBLEM WHEN SAVING THE SCORES!
 * @author C.Sironi
 *
 */
public class MatchRunner extends Thread{

	static{

		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
		System.setProperty("isThreadContextMapInheritable", "true");

		LOGGER = LogManager.getRootLogger();
		CSV_LOGGER = LogManager.getLogger("CSVLogger");

	}

	/**
	 * Static reference to the logger
	 */
	private static final Logger LOGGER;

	/**
	 * Static reference to the logger for the match score
	 */
	private static final Logger CSV_LOGGER;


	private int ID;

	private String tourneyName;
	private String gameKey;
	private int startClock;
	private int playClock;
	private long creationTime;
	private boolean invert;

	/**
	 * Needed to know where to log the scores
	 */
	private String resultFolder;


	public MatchRunner(int ID, String tourneyName, String gameKey, int startClock, int playClock,
			long creationTime, boolean invert){

		this.ID = ID;
		this.tourneyName = tourneyName;
		this.gameKey = gameKey;
		this.startClock = startClock;
		this.playClock = playClock;
		this.creationTime = creationTime;
		this.invert = invert;

		this.resultFolder = "";

	}

	@Override
	public void run(){

		System.out.println("Starting " + this.ID);

		this.resultFolder = ThreadContext.get("LOG_FOLDER");

		String logFolder = this.resultFolder;

		if(logFolder != null){
			logFolder += "/MatchRunner-" + this.ID;
		}else{
			logFolder = "/MatchRunner-" + this.ID;
		}

		LOGGER.info("[MatchRunner] Started MatchRunner " + this.ID + ". Writing logs to file " + logFolder + "/logFile.log");

		// LOGGING DETAILS
		ThreadContext.put("LOG_FOLDER", logFolder);
		LOGGER.info("[GamePlayer] Starting logs for MatchRunner " + this.ID + ".");
		// LOGGING DETAILS

		String matchName = this.ID + "." + this.tourneyName + "." + this.gameKey + "." + System.currentTimeMillis();

		LOGGER.info("[MatchRunner] Starting new match: " + matchName);

		Game game = GameRepository.getDefaultRepository().getGame(this.gameKey);

		// Create the propnet
		GameRepository theRepository = GameRepository.getDefaultRepository();

		List<Gdl> description = theRepository.getGame(this.gameKey).getRules();

        // Create the executor service that will run the propnet manager that creates the propnet
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Create the propnet creation manager
        SeparateInternalPropnetCreationManager manager = new SeparateInternalPropnetCreationManager(description, System.currentTimeMillis() + creationTime);

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
			LOGGER.error("[MatchRunner] Program interrupted. Match of game "+ this.gameKey +" won't be performed.", e);
			this.resetLogSettings();
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
				LOGGER.error("[MatchRunner] Program interrupted. Match of game "+ this.gameKey +" won't be performed.", e);
				this.resetLogSettings();
				Thread.currentThread().interrupt();
				return;
			}
		}

		// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.
		if(manager.getImmutablePropnet() == null || manager.getInitialPropnetState() == null){
			LOGGER.error("[MatchRunner] Impossible to play the match. Propnet and/or propnet state are null.");
			this.resetLogSettings();
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
			LOGGER.error("[MatchRunner] Impossible to play the match. Error when creating game player.", e);
			this.resetLogSettings();
			return;
		}
		GamePlayer suctPlayer = null;
		try {
			suctPlayer = new GamePlayer(9148 + (this.ID * 2), suctGamer);
		} catch (IOException e) {
			LOGGER.error("[MatchRunner] Impossible to play the match. Error when creating game player.", e);
			this.resetLogSettings();
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
			LOGGER.error("[MatchRunner] Impossible to play the match. Error when creating game server.", e);
			ductPlayer.shutdown();
			suctPlayer.shutdown();
			this.resetLogSettings();
			return;
		}
		server.start();
		try {
			server.join();
		} catch (InterruptedException e) {
			LOGGER.error("[MatchRunner] Program interrupted. Impossible to complete the match.", e);
			server.abort();
			ductPlayer.shutdown();
			suctPlayer.shutdown();
			this.resetLogSettings();
			Thread.currentThread().interrupt();
			return;
		}

		LOGGER.info("[MatchRunner] Execution of match " + matchName + " completed.");

		// Open up the directory for this tournament.
		File f = new File(this.resultFolder);
		if (!f.exists()) {
			f.mkdir();
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
		f = new File(this.resultFolder + "/" + matchName + ".json");
		if (f.exists()) f.delete();
		try {
			bw = new BufferedWriter(new FileWriter(f));
			bw.write(match.toJSON());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			LOGGER.error("[MatchRunner] Match completed correctly, but impossible to save match information on JSON file.", e);
		}

		// Save the goals in the "/scores" file for the tournament.

		List<Integer> goals;
		try {
			goals = server.getGoals();
		} catch (GoalDefinitionException | StateMachineException e) {
			LOGGER.error("[MatchRunner] Match completed correctly, but impossible to save final scores.", e);
			this.resetLogSettings();
			return;
		}

		ThreadContext.put("LOG_FOLDER", this.resultFolder);
		ThreadContext.put("LOG_FILE", "scores");

		for (int i = 0; i < goals.size(); i++){

			CSV_LOGGER.info(this.ID + ";" + matchName + ";" + playerNames.get(i) + ";" + goals.get(i) + ";");

		}

		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){

		}

		ductPlayer.shutdown();
		suctPlayer.shutdown();

		this.resetLogSettings();

		System.out.println("Ending " + this.ID);

	}

	private void resetLogSettings(){

		if(this.resultFolder == null){
			ThreadContext.remove("LOG_FOLDER");
		}else{
			ThreadContext.put("LOG_FOLDER", this.resultFolder);
		}
		ThreadContext.remove("LOG_FILE");
	}

}
