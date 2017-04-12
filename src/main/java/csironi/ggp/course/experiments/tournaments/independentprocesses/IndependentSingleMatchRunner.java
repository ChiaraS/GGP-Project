package csironi.ggp.course.experiments.tournaments.independentprocesses;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.GamePlayer;
import org.ggp.base.player.gamer.statemachine.ConfigurableStateMachineGamer;
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
	 * [numMatches] = number of repetition of the match that must be performed. Note that if using the PropNet, a new one will still be
	 * created for all the player before any repetition.
	 * [startID] = an integer ID of the first match (normally goes from 0 to n for each combination of players in a tourney where we want
	 * n matches for each combination). For the i-th repetiton of the match we will use the ID startID+i.
	 * [gameKey] = the game key
	 * [startClock(sec)] = start clock of the match (in seconds)
	 * [playClock(sec)] = play clock of the match (in seconds)
	 * [pnCreationTime(ms)] = available time to create the propnet (in milliseconds)
	 * [theGamerTypes (one or more)] = list of gamer types that we want to include in the experiment. Each gamer must be specified with
	 * the exact name of the class that implements such gamer. If the gamer is a subclass of the ConfigurableStateMachineGamer it must
	 * be specified with the exact name of the class that implements it AND the name of the file in which its settings are specified,
	 * separated by "-" (e.g. MctsGamer-GraveDuct.properties, or MctsGamer-Duct.properties). NOTE that the specified .properties file
	 * must be in the folder with the path gamersSettingsFolderPath specified in the GamerConfiguration class.
	 */
	public static void main(String[] args) {

		/** 1. Check the correctness of the inputs **/

		if(args.length < 8){
			System.out.println("Impossible to start match runner, missing inputs. Please give the following parameters to the command line:");
			System.out.println("[logFolder] [numMatches] [startID] [gameKey] [startClock(sec)] [playClock(sec)] [pnCreationTime(ms)] [theGamerTypes (one or more)]");
			return;
		}

		String logFolder; // Don't log directly in here or you might overwrite some old log
		int numMatches;
		int startID;
		String gameKey;
		int startClock;
		int playClock;
		long pnCreationTime;
		List<Class<?>> theGamersClasses = new ArrayList<Class<?>>();

		logFolder = args[0];

		try{
			numMatches = Integer.parseInt(args[1]);
		}catch(NumberFormatException e){
			System.out.println("Impossible to start match runner, wrong input. The number of matches must be an integer value, not " + args[1] + ".");
			return;
		}

		try{
			startID = Integer.parseInt(args[2]);
		}catch(NumberFormatException e){
			System.out.println("Impossible to start match runner, wrong input. The match ID must be an integer value, not " + args[2] + ".");
			return;
		}

		gameKey = args[3];

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
			startClock = Integer.parseInt(args[4]);
		}catch(NumberFormatException e){
			System.out.println("Impossible to start match runner, wrong input. The start clock must be an integer value, not " + args[4] + ".");
			return;
		}

		try{
			playClock = Integer.parseInt(args[5]);
		}catch(NumberFormatException e){
			System.out.println("Impossible to start match runner, wrong input. The play clock must be an integer value, not " + args[5] + ".");
			return;
		}

		try{
			pnCreationTime = Long.parseLong(args[6]);
		}catch(NumberFormatException e){
			System.out.println("Impossible to start match runner, wrong input. The propnet creation time must be an long value, not " + args[6] + ".");
			return;
		}

		boolean buildPropnet = false;
		String[] gamerTypes = new String[args.length-7];
		String[] gamerSettings = new String[args.length-7];
    	for (int i = 7; i < args.length; i++){
    		if(args[i].endsWith(".properties")){
    			String[] s = args[i].split("-");
    			gamerTypes[i-7] = s[0];
    			gamerSettings[i-7] = s[1];
    		}else{
    			gamerTypes[i-7] = args[i];
    			gamerSettings[i-7] = null;
    		}
    	}

    	for(int i = 0; i < gamerTypes.length; i++){

    		Class<?> theCorrespondingClass = null;
    		for (Class<?> gamerClass : ProjectSearcher.INTERNAL_PROPNET_GAMERS.getConcreteClasses()) {
        		if(gamerClass.getSimpleName().equals(gamerTypes[i])){
        			theCorrespondingClass = gamerClass;
        			buildPropnet = true;
        			if(ConfigurableStateMachineGamer.class.isAssignableFrom(theCorrespondingClass)){ // The class is subclass of ConfigurableStateMachineGamer
        				// If the gamer is configurable than the settings file must be specified
        				if(gamerSettings[i] == null){
        					System.out.println("Impossible to start match runner, wrong input. No settings file specified for gamer type " + gamerTypes[i] + ".");
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
    			System.out.println("Impossible to start match runner, wrong input. Unexisting gamer type " + gamerTypes[i] + ".");
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

		/** Here we checked all the inputs. Now we can try to start the match. **/

		System.out.println("Starting " + startID);

		if(logFolder == null || logFolder.equals("")){
			ThreadContext.put("LOG_FOLDER", "MatchRunner" + startID);
		}else{
			ThreadContext.put("LOG_FOLDER", logFolder + "/" + "MatchRunner" + startID);
		}

		GamerLogger.startFileLogging();

		GamerLogger.log("MatchRunner", "Started MatchRunner " + startID + ".");

		/** 2. First of all we try to create the players. **/

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
				if(ConfigurableStateMachineGamer.class.isAssignableFrom(gamerClass)){
					theGamer = (StateMachineGamer) gamerClass.getConstructor(String.class).newInstance(GamerConfiguration.gamersSettingsFolderPath + "/" + gamerSettings[i]);
				}else{
					theGamer = (StateMachineGamer) gamerClass.newInstance();
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				//System.out.println("Impossible to play the match. Error when instantiating the gamer " + gamerClass.getSimpleName() + ".");
				//e.printStackTrace();
				GamerLogger.logError("MatchRunner", "Impossible to play the match. Error when instantiating the gamer " + gamerClass.getSimpleName() + ".");
				GamerLogger.logStackTrace("MatchRunner", e);
				return;
			}

			try {
				thePlayers.add(new GamePlayer(9000 + i + (startID * theGamersClasses.size()), theGamer));
			} catch (IOException e) {
				//System.out.println("Impossible to play the match. Error when creating game player for gamer " + theGamer.getName() + ".");
				//e.printStackTrace();
				GamerLogger.logError("MatchRunner", "Impossible to play the match. Error when creating game player for gamer " + theGamer.getName() + ".");
				GamerLogger.logStackTrace("MatchRunner", e);
				return;
			}
			hostNames.add("127.0.0.1");
			playerNames.add(theGamer.getName());
			portNumbers.add(thePlayers.get(i).getGamerPort());
			i++;
		}

		for(GamePlayer gamePlayer : thePlayers){
			gamePlayer.start();
		}

    	int matchID;
    	for(int repetition = 0; repetition < numMatches; repetition++){

    		matchID = startID * numMatches + repetition;

			String matchName = matchID + "." + gameKey + "." + System.currentTimeMillis();

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
					for(GamePlayer player : thePlayers){
						player.shutdown();
					}
					return;
				}
			}

			/** 4. Set the propnet for all players that can use it. **/
			for(GamePlayer gamePlayer : thePlayers){
				if(gamePlayer.getGamer() instanceof InternalPropnetGamer){
					thePropnetGamer  = (InternalPropnetGamer) gamePlayer.getGamer();
					thePropnetGamer.setExternalStateMachine(new SeparateInternalPropnetStateMachine(manager.getImmutablePropnet(), manager.getInitialPropnetState()));
				}
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


				String toLog = startID + ";" + matchName + ";";

				for(int j = 0; j < goals.size(); j++){

					toLog += playerNames.get(j) + ";" + goals.get(j) + ";";

				}

				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "scores", toLog);
			} catch (GoalDefinitionException | StateMachineException e) {
				GamerLogger.logError("MatchRunner", "Match completed correctly, but impossible to save final scores.");
				GamerLogger.logStackTrace("MatchRunner", e);
			}

			// This is just an extra check to make sure that the gamers have stopped completely the previous game
			// and are ready to start a new one before we set the new propnet.
			boolean ready = true;

			//System.out.println("Ready = " + ready);

			for(GamePlayer player : thePlayers){
				//boolean isStopped = player.isGamerStopped();

				//System.out.println("Player = " + player.getGamer().getName() + ", isStopped = " + isStopped);
				ready = (ready && player.isGamerStopped());

			}

			//System.out.println("Ready = " + ready);

			int count = 60; // Try 60 times (i.e. 1 minute) to check if players are done, otherwise stop match runner.

			while(!ready && count > 0){

				try {Thread.sleep(1000);} catch (InterruptedException lEx) {/* Whatever */}

				ready = true;

				for(GamePlayer player : thePlayers){
					//boolean isStopped = player.isGamerStopped();

					//System.out.println("Player = " + player.getGamer().getName() + ", isStopped = " + isStopped);
					ready = (ready && player.isGamerStopped());
				}
				//System.out.println("Ready = " + ready);
				count--;
			}

			if(!ready){
				GamerLogger.logError("MatchRunner", "Problem with players after end of game. At least one player is not ready to continue with the next game. Impossible to complete this run of sequential matches.");
				for(GamePlayer player : thePlayers){
					player.shutdown();
				}
				return;
			}

    	}

		for(GamePlayer player : thePlayers){
			player.shutdown();
		}

		System.out.println("Ending " + startID);

	}

}
