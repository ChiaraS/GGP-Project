package csironi.ggp.course.experiments.tournaments.independentprocesses;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.GamePlayer;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.player.gamer.statemachine.propnet.InternalPropnetGamer;
import org.ggp.base.server.GameServer;
import org.ggp.base.server.exception.GameServerException;
import org.ggp.base.util.configuration.GamerConfiguration;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.game.ManualUpdateLocalGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.propnet.creationManager.PropNetManagerRunner;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetManager;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.implementation.propnet.SeparateInternalPropnetStateMachine;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

public class IndependentSingleMatchRunner {

	static{
		System.setProperty("isThreadContextMapInheritable", "true");
	}

	/**
	 * @param args this method expects the following parameters:
	 * [logFolder] = the folder of the combination being currently tested, in which this match should create its own log folder
	 * [ID] = an integer ID of the match (normally goes from 0 to n for each combination of players in a tourney where we want n matches
	 * for each combination)
	 * [gameKey] = the game key
	 * [startClock(sec)] = start clock of the match (in seconds)
	 * [playClock(sec)] = play clock of the match (in seconds)
	 * [pnCreationTime(ms)] = available time to create the propnet (in milliseconds)
	 * [theGamerTypes (one or more)] = list of gamer types that we want to include in the experiment. Each gamer must be specified with
	 * the exact name of the class that implements such gamer. If the gamer is a subclass of the ConfigurableStateMachineGamer it must
	 * be specified with the exact name of the class that implements it AND the path of the file in which its settings are specified,
	 * separated by "-" (e.g. MctsGamer-C:\Users\c.sironi\BITBUCKET REPOS\GGP-Base\GamersSettings\DuctMctsGamer.properties, or
	 * MctsGamer-Duct.properties)
	 */
	public static void main(String[] args) {

		/** 1. Check the correctness of the inputs **/

		if(args.length < 7){
			System.out.println("Impossible to start match runner, missing inputs. Please give the following parameters to the command line:");
			System.out.println("[logFolder] [ID] [gameKey] [startClock(sec)] [playClock(sec)] [pnCreationTime(ms)] [theGamerTypes (one or more)]");
			return;
		}

		String logFolder; // Don't log directly in here or you might overwrite some old log
		int ID;
		String gameKey;
		int startClock;
		int playClock;
		long pnCreationTime;
		List<Class<?>> theGamersClasses = new ArrayList<Class<?>>();
		List<String> theGamersSettingsFilePaths = new ArrayList<String>();

		logFolder = args[0];

		try{
			ID = Integer.parseInt(args[1]);
		}catch(NumberFormatException e){
			System.out.println("Impossible to start match runner, wrong input. The match ID must be an integer value, not " + args[1] + ".");
			return;
		}

		gameKey = args[2];

		//GameRepository gameRepo = GameRepository.getDefaultRepository();

		// LINUX
    	//GameRepository gameRepo = new ManualUpdateLocalGameRepository("/home/csironi/GAMEREPOS/GGPBase-GameRepo-03022016");

    	GameRepository gameRepo = new ManualUpdateLocalGameRepository(GamerConfiguration.defaultLocalGameRepositoryFolderPath);

    	Game game = gameRepo.getGame(gameKey);

    	if(game == null){
    		System.out.println("Impossible to start match runner: specified game " + gameKey + " not found in the repository.");
			return;
    	}

		try{
			startClock = Integer.parseInt(args[3]);
		}catch(NumberFormatException e){
			System.out.println("Impossible to start match runner, wrong input. The start clock must be an integer value, not " + args[3] + ".");
			return;
		}

		try{
			playClock = Integer.parseInt(args[4]);
		}catch(NumberFormatException e){
			System.out.println("Impossible to start match runner, wrong input. The play clock must be an integer value, not " + args[4] + ".");
			return;
		}

		try{
			pnCreationTime = Long.parseLong(args[5]);
		}catch(NumberFormatException e){
			System.out.println("Impossible to start match runner, wrong input. The propnet creation time must be an long value, not " + args[5] + ".");
			return;
		}

		boolean buildPropnet = false;
		String gamerType;
    	for (int i = 6; i < args.length; i++){
    		gamerType = args[i];
    		Class<?> theCorrespondingClass = null;
    		for (Class<?> gamerClass : ProjectSearcher.INTERNAL_PROPNET_GAMERS.getConcreteClasses()) {
        		if(gamerClass.getSimpleName().equals(gamerType)){
        			theCorrespondingClass = gamerClass;
        			//!!!!!!! if(MctsGamer.class.isAssignableFrom(theCorrespondingClass){the cass is subclass of MctsGamer}
        			buildPropnet = true;
        		}
        	}
    		for (Class<?> gamerClass : ProjectSearcher.PROVER_GAMERS.getConcreteClasses()) {
        		if(gamerClass.getSimpleName().equals(gamerType)){
        			theCorrespondingClass = gamerClass;
        		}
        	}
    		if(theCorrespondingClass == null){
    			System.out.println("Impossible to start match runner, wrong input. Unexisting gamer type " + gamerType + ".");
    			return;
    		}else{
    			theGamersClasses.add(theCorrespondingClass);
    		}
		}

    	int expectedRoles = ExplicitRole.computeRoles(game.getRules()).size();

    	if(theGamersClasses.size() != expectedRoles){
    		System.out.println("Impossible to start match runner, wrong input. Detected " + theGamersClasses.size() + " gamer types for a game with " + expectedRoles + " roles.");
    		return;
    	}

		/** 2. Here we checked all the inputs. Now we can try to start the match. **/

		System.out.println("Starting " + ID);

		if(logFolder == null || logFolder.equals("")){
			ThreadContext.put("LOG_FOLDER", "MatchRunner" + ID);
		}else{
			ThreadContext.put("LOG_FOLDER", logFolder + "/" + "MatchRunner" + ID);
		}

		GamerLogger.startFileLogging();

		String matchName = ID + "." + gameKey + "." + System.currentTimeMillis();

		GamerLogger.log("MatchRunner", "Started MatchRunner " + ID + ".");

		GamerLogger.log("MatchRunner", "Starting new match: " + matchName);


		List<Gdl> description = game.getRules();

		SeparateInternalPropnetManager manager = null;

		/** 3. Try to create the PropNet if needed. **/

		if(buildPropnet){

			/****************** ONLY USE IF RUNNING PROPNET EXPERIMENTS - start **********************

			OptimizationCaller[] optimizations;

			optimizations = new OptimizationCaller[4];


			optimizations[0] = new RemoveAnonPropositions();
			optimizations[1] = new OptimizeAwayConstants();
			optimizations[2] = new OptimizeAwayConstantValueComponents();
			optimizations[3] = new RemoveOutputlessComponents();

			****************** ONLY USE IF RUNNING PROPNET EXPERIMENTS - end **********************/

			GamerLogger.log("MatchRunner", "Creating the propnet.");

			manager =  new SeparateInternalPropnetManager(description, System.currentTimeMillis() + pnCreationTime);

			//manager =  new SeparateInternalPropnetManager(description, System.currentTimeMillis() + pnCreationTime, optimizations);

			PropNetManagerRunner.runPropNetManager(manager, pnCreationTime);

			// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.
			if(manager.getImmutablePropnet() == null || manager.getInitialPropnetState() == null){
				GamerLogger.logError("MatchRunner", "Impossible to play the match. Propnet and/or propnet state are null.");
				return;
			}
		}

		/** 4. Try to create and start the players. **/

		// Create the players.
		List<GamePlayer> thePlayers = new ArrayList<GamePlayer>();

		List<String> hostNames = new ArrayList<String>();
		List<String> playerNames = new ArrayList<String>();
		List<Integer> portNumbers = new ArrayList<Integer>();

		StateMachineGamer theGamer;
		InternalPropnetGamer thePropnetGamer;
		int i = 0;
		for(Class<?> gamerClass : theGamersClasses){
			try {
				theGamer  = (StateMachineGamer) gamerClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				GamerLogger.logError("MatchRunner", "Impossible to play the match. Error when instantiating the gamer " + gamerClass.getSimpleName() + ".");
				GamerLogger.logStackTrace("MatchRunner", e);
				return;
			}

			if(theGamer instanceof InternalPropnetGamer){
				thePropnetGamer  = (InternalPropnetGamer) theGamer;
				thePropnetGamer.setExternalStateMachine(new SeparateInternalPropnetStateMachine(manager.getImmutablePropnet(), manager.getInitialPropnetState()));
			}

			try {
				thePlayers.add(new GamePlayer(9000 + i + (ID * theGamersClasses.size()), theGamer));
			} catch (IOException e) {
				GamerLogger.logError("MatchRunner", "Impossible to play the match. Error when creating game player for gamer " + theGamer.getName() + ".");
				GamerLogger.logStackTrace("MatchRunner", e);
				return;
			}
			hostNames.add("127.0.0.1");
			playerNames.add(theGamer.getName());
			portNumbers.add(thePlayers.get(i).getGamerPort());
			i++;
		}

		// Start all players (after creating them all so that if the creation of one of them throws an exception
		// we won't have to think about stopping all previously started players).
		for(GamePlayer player : thePlayers){
			player.start();
		}

		Match match = new Match(matchName, -1, startClock, playClock, game);
		match.setPlayerNamesFromHost(playerNames);

		/** 5. Create and start the server. **/

		// Actually run the match, using the desired configuration.
		GameServer server;
		try {
			server = new GameServer(match, hostNames, portNumbers);
		} catch (GameServerException e) {
			GamerLogger.logError("MatchRunner", "Impossible to play the match. Error when creating game server.");
			GamerLogger.logStackTrace("MatchRunner", e);
			for(GamePlayer player : thePlayers){
				player.shutdown();
			}
			return;
		}

		server.start();

		try {
			server.join();
		} catch (InterruptedException e) {
			GamerLogger.logError("MatchRunner", "Program interrupted. Impossible to complete the match.");
			GamerLogger.logStackTrace("MatchRunner", e);
			server.abort();
			for(GamePlayer player : thePlayers){
				player.shutdown();
			}
			Thread.currentThread().interrupt();
			return;
		}

		GamerLogger.log("MatchRunner", "Execution of match " + matchName + " completed.");

		for(GamePlayer player : thePlayers){
			player.shutdown();
		}

		/** 6. Save the match outcome. **/
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
			return;
		}

		String toLog = ID + ";" + matchName + ";";

		for(int j = 0; j < goals.size(); j++){

			toLog += playerNames.get(j) + ";" + goals.get(j) + ";";

		}

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "scores", toLog);

		System.out.println("Ending " + ID);

	}

}
