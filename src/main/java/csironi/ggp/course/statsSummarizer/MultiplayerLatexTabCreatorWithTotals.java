package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NOTE that this class works only when each single tournament involves exactly 2 players!
 *
 *
 * This class aggregates in a double entry table (formatted in Latex) the results of
 * multiple experiments that involve a certain number of players matched against each
 * other.
 *
 * I.e. if we have the statistics for the following tournaments:
 * - PlayerA vs PlayerB
 * - PlayerA vs PlayerC
 * - PlayerB vs PlayerC
 *
 * each of which contains results for the following games: Game1, Game2, Game3,
 * then this class will output a latex representation of the following table:
 * ______________________________________________________________
 * Game1    |  PlayerA   |  PlayerB   |  PlayerC   ||  Overall   |
 * ---------|------------|------------|------------||------------|
 * PlayerA  |   		 |  win%AvsB  |  win%AvsC  || win%AvsAll |
 * PlayerB  |  win%BvsA  |			  |  win%BvsC  || win%BvsAll |
 * PlayerC  |  win%CvsA  |  win%CvsB  |			   || win%CvsAll |
 * ---------|------------|------------|------------||------------|
 * Overall  | win%AllvsA | win%AllvsA | win%AllvsA ||            |
 * ______________________________________________________________
 * Game1    |  PlayerA   |  PlayerB   |  PlayerC   ||  Overall   |
 * ---------|------------|------------|------------||------------|
 * PlayerA  |   		 |  win%AvsB  |  win%AvsC  || win%AvsAll |
 * PlayerB  |  win%BvsA  |		 	  |  win%BvsC  || win%BvsAll |
 * PlayerC  |  win%CvsA  |  win%CvsB  |			   || win%CvsAll |
 * ---------|------------|------------|------------||------------|
 * Overall  | win%AllvsA | win%AllvsA | win%AllvsA ||            |
 * ______________________________________________________________
 * Game1    |  PlayerA   |  PlayerB   |  PlayerC   ||  Overall   |
 * ---------|------------|------------|------------||------------|
 * PlayerA  |   		 |  win%AvsB  |  win%AvsC  || win%AvsAll |
 * PlayerB  |  win%BvsA  |			  |  win%BvsC  || win%BvsAll |
 * PlayerC  |  win%CvsA  |  win%CvsB  |			   || win%CvsAll |
 * ---------|------------|------------|------------||------------|
 * Overall  | win%AllvsA | win%AllvsA | win%AllvsA ||            |
 * ______________________________________________________________
 * AllGames |  PlayerA   |  PlayerB   |  PlayerC   ||  Overall   |
 * ---------|------------|------------|------------||------------|
 * PlayerA  |  		     |  win%AvsB  |  win%AvsC  || win%AvsAll |
 * PlayerB  |  win%BvsA  |			  |  win%BvsC  || win%BvsAll |
 * PlayerC  |  win%CvsA  |  win%CvsB  |			   || win%CvsAll |
 * ---------|------------|------------|------------||------------|
 * Overall  | win%AllvsA | win%AllvsA | win%AllvsA ||            |
 * ______________________________________________________________
 *
 * For each gameKey this class will represent the name as returned by the gameKeyMap.
 * The games in the tables are ordered according to the gamesOrder.
 * The class expects as input the path of a folder that contains a folder for each
 * tournament (in the examples the 3 folders with statistics for PlayerAvsPlayerB,
 * PlayerAvsPlayerC and PlayerBvsPlayerC). Each of these folders will contain the
 * "*-Stats" folder for each of the tested games.
 *
 * As optional arguments, the names (or aliases if they are being used) of the
 * players can be specified in the order in which we want to show the players in the
 * tables. If no order is specified, an arbitrary one will be used. NOTE: if an order
 * is specified, it must include all players in the experiment, otherwise the players
 * not included in the order will be left out from the tables.
 *
 * As optional extra arguments, ALIAS names for players can be specified together
 * with the players types for which the alias must be substituted. E.g. if we want
 * PlayerA and PlayerB to be considered as the same player with name PlayerAB we
 * would have to specify the following as argument:
 *
 * PlayerAB=PlayerA;PlayerB (with no spaces!)
 *
 * We can specify as many arguments as the previous one as we want. If we also
 * want PlayerC, PlayerD and PlayerE to be aliased as PlayerCDE we can specify
 * another argument as follows:
 *
 * PlayerCDE=PlayerC;PlayerD;PlayerE
 *
 * This is useful when we want to put together results for different games where we
 * accidentally gave different names to the same player, or we repeat the same experiment
 * (i.e. with the same games) for two different players and then we want to aggregate the
 * wins for each game only of the player that performed best in the game. This is also useful
 * if we simply want to simplify the names of the players.
 *
 * Example of input:
 *
 * path/of/the/folder/with/win/files   PlayerAB PlayerCD 							PlayerAB=PlayerA;PlayerB PlayerCD=PlayerC;PlayerD
 *
 * [path]							   [player name in the order we want them]      [player aliases specification]
 *
 * NOTE: player names or aliases cannot contain the character "="!
 */
public class MultiplayerLatexTabCreatorWithTotals {

	public static final Map<String, String> gameKeyMap;

	public static final List<String> gamesOrder;

	static{
		gameKeyMap = new HashMap<String, String>();

		// Maps the game keys into the corresponding game name we want to have in the paper
		gameKeyMap.put("tictactoe_3d_2player","3DTicTacToe");
		gameKeyMap.put("breakthrough","Breakthrough");
		gameKeyMap.put("knightThrough","Knightthrough");
		gameKeyMap.put("skirmish","Skirmish");
		gameKeyMap.put("battle","Battle");
		gameKeyMap.put("chinook","Chinook");
		gameKeyMap.put("chineseCheckers3","ChineseCheckers3");
		gameKeyMap.put("checkers","Checkers");
		gameKeyMap.put("connect5","Connect 5");
		gameKeyMap.put("othello-comp2007","Othello");
		gameKeyMap.put("quad_7x7","Quad");
		gameKeyMap.put("sheepAndWolf","SheepAndWolf");
		gameKeyMap.put("ttcc4_2player","TTCC4 2P");
		gameKeyMap.put("zhadu","Zhadu");
		gameKeyMap.put("ttcc4","TTCC4 3P");
		gameKeyMap.put("ticTacToe","TicTacToe");
		gameKeyMap.put("connect4","Connect 4");
		gameKeyMap.put("pentago","Pentago");
		gameKeyMap.put("reversi","Reversi");
		gameKeyMap.put("chineseCheckers4","ChineseCheckers4");
		gameKeyMap.put("chineseCheckers6","ChineseCheckers6");

		gameKeyMap.put("OverallStats","Avg Win\\%");

		gamesOrder = new ArrayList<String>();

		// Gives the order in which games results must be inserted in the table
		gamesOrder.add("tictactoe_3d_2player");
		gamesOrder.add("breakthrough");
		gamesOrder.add("knightThrough");
		gamesOrder.add("skirmish");
		gamesOrder.add("battle");
		gamesOrder.add("chinook");
		gamesOrder.add("chineseCheckers3");
		gamesOrder.add("checkers");
		gamesOrder.add("connect5");
		gamesOrder.add("othello-comp2007");
		gamesOrder.add("quad_7x7");
		gamesOrder.add("sheepAndWolf");
		gamesOrder.add("ttcc4_2player");
		gamesOrder.add("zhadu");
		gamesOrder.add("ttcc4");
		gamesOrder.add("connect4");
		gamesOrder.add("chineseCheckers4");
		gamesOrder.add("chineseCheckers6");
		gamesOrder.add("pentago");
		gamesOrder.add("reversi");
		gamesOrder.add("ticTacToe");

		gamesOrder.add("OverallStats");
	}

	public static void main(String[] args) {

		if(args.length == 0){
			System.out.println("Expecting at least 1 argument: the path of the main folder containing the folder with statistics for each pair of agents.");
			return;
		}

		String folderPath = args[0];

		File folder = new File(folderPath);

		if(!folder.isDirectory()){
			System.out.println("Cannot find the specified folder.");
		}

		String destinationFilePath = folderPath + "/Table";

		int i = 1;

		// Get the player names/aliases (if any) in the order they should appear in the table(s).
		// Note that if later in the input aliases are specified, this method expects to find aliases
		// also to specify the order. If aliases are not specified then it expects the names of the
		// players.
		List<String> playerNamesOrder = new ArrayList<String>();
		while(i < args.length && !args[i].contains("=")){
			playerNamesOrder.add(args[i]);
			i++;
		}

		// Put here all player names(or alias if specified) encountered during processing
		// This is needed when creating the tables if not all the players are specified in the playerNamesOrder
		Set<String> allPlayerNames = new HashSet<String>();

		// Get the player aliases (if any)
		// Create a map that maps each player to its alias
		Map<String,String> aliases = new HashMap<String,String>();

		// Fill the map
		String[] aliasSpecification;
		String alias;
		String[] playersWithSameAlias;
		while(i < args.length){

			aliasSpecification = args[i].split("=");

			//System.out.println(aliasSpecification);

			alias = aliasSpecification[0];

			//System.out.println();

			playersWithSameAlias = aliasSpecification[1].split(";");
			for(int j = 0; j < playersWithSameAlias.length; j++){

				//System.out.println(playersWithSameAlias[j]);

				aliases.put(playersWithSameAlias[j], alias);
			}

			i++;
		}

		//System.out.println("0");

		File[] allTourneyFolders = folder.listFiles();

		// Maps each game to the maps with the player names for the first player (row player).
		Map<String,Map<String,Map<String,ExperimentsStats>>> gamesMap = new HashMap<String,Map<String,Map<String,ExperimentsStats>>> ();

		String gameKey;

		// Maps with statistics for each single game
		Map<String,Map<String,ExperimentsStats>> playersMap;
		Map<String,ExperimentsStats> opponentsMap;
		ExperimentsStats playersPairStats;

		// Maps with overall statistics (i.e. for all games)
		Map<String,Map<String,ExperimentsStats>> allGamesPlayersMap;
		Map<String,ExperimentsStats> allGamesOpponentsMap;
		ExperimentsStats allGamesPlayersPairStats;


		String[] splitLine;
		String playerA = null;
		String playerB = null;
		String aliasPlayerA = null;
		String aliasPlayerB = null;
		List<Double> samplesPlayerA = null;
		List<Double> samplesPlayerB = null;

		for(File tourneyFolder : allTourneyFolders){

			//System.out.println(tourneyFolder.getName());

			if(tourneyFolder.isDirectory()) {

				File[] tourneyFoldersPerGame = tourneyFolder.listFiles();

				for(File gameFolder : tourneyFoldersPerGame){

					//System.out.println("   " + gameFolder.getName());

					if(gameFolder.isDirectory() && gameFolder.getName().endsWith("-Stats")){

						gameKey = getGameKey(gameFolder.getName());

						// Only consider the stats if the game is specified in the gamesOrder list.
						if(gamesOrder.contains(gameKey)) {

							playersMap = gamesMap.get(gameKey);
							if(playersMap == null) {
								playersMap = new HashMap<String,Map<String,ExperimentsStats>>();
								gamesMap.put(gameKey, playersMap);
							}

							allGamesPlayersMap = gamesMap.get("OverallStats");
							if(allGamesPlayersMap == null) {
								allGamesPlayersMap = new HashMap<String,Map<String,ExperimentsStats>>();
								gamesMap.put("OverallStats", allGamesPlayersMap);
							}

							File[] statsFoldersPerGame = gameFolder.listFiles();

							for(File statsFolder : statsFoldersPerGame){

								//System.out.println("      " + statsFolder.getName());

								if(statsFolder.getName().endsWith(".Statistics") || statsFolder.getName().endsWith(".statistics") ||
										statsFolder.getName().equals("Statistics") || statsFolder.getName().equals("statistics")){

									File[] statFiles = statsFolder.listFiles();

									playerA = null;
									playerB = null;

									for(File statFile : statFiles){

										//System.out.println("         " + statFile.getName());

										if(statFile.getName().endsWith("WinsSamples.csv")){

											splitLine = statFile.getName().split("-");

											if(playerA == null) {
												playerA = splitLine[0];
												aliasPlayerA = aliases.get(playerA); // If no alias is specified, then this will be null;
												if(aliasPlayerA == null){
													aliasPlayerA = playerA;
												}
												allPlayerNames.add(aliasPlayerA);
												samplesPlayerA = getSamplesFromFile(statFile);
											}else {
												playerB = splitLine[0];
												aliasPlayerB = aliases.get(playerB); // If no alias is specified, then this will be null;
												if(aliasPlayerB == null){
													aliasPlayerB = playerB;
												}
												allPlayerNames.add(aliasPlayerB);
												samplesPlayerB = getSamplesFromFile(statFile);
											}

										}
									}

									if(playerA == null || playerB == null || aliasPlayerA == null || aliasPlayerB == null || samplesPlayerA == null || samplesPlayerB == null) {
										System.out.println("Cannot find all necessary files for the expected two players, skipping game folder " + gameFolder.getPath() + "!");
										break;
									}

									///////////////////////////////////////////////////
									// Add samples of player A...
									opponentsMap = playersMap.get(aliasPlayerA);
									if(opponentsMap == null) {
										opponentsMap = new HashMap<String,ExperimentsStats>();
										playersMap.put(aliasPlayerA, opponentsMap);
									}
									allGamesOpponentsMap = allGamesPlayersMap.get(aliasPlayerA);
									if(allGamesOpponentsMap == null) {
										allGamesOpponentsMap = new HashMap<String,ExperimentsStats>();
										allGamesPlayersMap.put(aliasPlayerA, allGamesOpponentsMap);
									}

									// ...against player B and...
									playersPairStats = opponentsMap.get(aliasPlayerB);
									if(playersPairStats == null) {
										playersPairStats = new ExperimentsStats();
										opponentsMap.put(aliasPlayerB, playersPairStats);
									}
									allGamesPlayersPairStats = allGamesOpponentsMap.get(aliasPlayerB);
									if(allGamesPlayersPairStats == null) {
										allGamesPlayersPairStats = new ExperimentsStats();
										allGamesOpponentsMap.put(aliasPlayerB, allGamesPlayersPairStats);
									}
									for(Double win : samplesPlayerA) {
										playersPairStats.addWins(win);
										allGamesPlayersPairStats.addWins(win);
									}
									// ...against all other players
									playersPairStats = opponentsMap.get("ALL");
									if(playersPairStats == null) {
										playersPairStats = new ExperimentsStats();
										opponentsMap.put("ALL", playersPairStats);
									}
									allGamesPlayersPairStats = allGamesOpponentsMap.get("ALL");
									if(allGamesPlayersPairStats == null) {
										allGamesPlayersPairStats = new ExperimentsStats();
										allGamesOpponentsMap.put("ALL", allGamesPlayersPairStats);
									}
									for(Double win : samplesPlayerA) {
										playersPairStats.addWins(win);
										allGamesPlayersPairStats.addWins(win);
									}
									//////////////////////////////////////////////////////////
									// Add samples of player B...
									opponentsMap = playersMap.get(aliasPlayerB);
									if(opponentsMap == null) {
										opponentsMap = new HashMap<String,ExperimentsStats>();
										playersMap.put(aliasPlayerB, opponentsMap);
									}
									allGamesOpponentsMap = allGamesPlayersMap.get(aliasPlayerB);
									if(allGamesOpponentsMap == null) {
										allGamesOpponentsMap = new HashMap<String,ExperimentsStats>();
										allGamesPlayersMap.put(aliasPlayerB, allGamesOpponentsMap);
									}
									// ...against player A and...
									playersPairStats = opponentsMap.get(aliasPlayerA);
									if(playersPairStats == null) {
										playersPairStats = new ExperimentsStats();
										opponentsMap.put(aliasPlayerA, playersPairStats);
									}
									allGamesPlayersPairStats = allGamesOpponentsMap.get(aliasPlayerA);
									if(allGamesPlayersPairStats == null) {
										allGamesPlayersPairStats = new ExperimentsStats();
										allGamesOpponentsMap.put(aliasPlayerA, allGamesPlayersPairStats);
									}
									for(Double win : samplesPlayerB) {
										playersPairStats.addWins(win);
										allGamesPlayersPairStats.addWins(win);
									}
									// ...against all other players
									playersPairStats = opponentsMap.get("ALL");
									if(playersPairStats == null) {
										playersPairStats = new ExperimentsStats();
										opponentsMap.put("ALL", playersPairStats);
									}
									allGamesPlayersPairStats = allGamesOpponentsMap.get("ALL");
									if(allGamesPlayersPairStats == null) {
										allGamesPlayersPairStats = new ExperimentsStats();
										allGamesOpponentsMap.put("ALL", allGamesPlayersPairStats);
									}
									for(Double win : samplesPlayerB) {
										playersPairStats.addWins(win);
										allGamesPlayersPairStats.addWins(win);
									}
									//////////////////////////////////////////////
									// Add samples of ALL players...
									opponentsMap = playersMap.get("ALL");
									if(opponentsMap == null) {
										opponentsMap = new HashMap<String,ExperimentsStats>();
										playersMap.put("ALL", opponentsMap);
									}
									allGamesOpponentsMap = allGamesPlayersMap.get("ALL");
									if(allGamesOpponentsMap == null) {
										allGamesOpponentsMap = new HashMap<String,ExperimentsStats>();
										allGamesPlayersMap.put("ALL", allGamesOpponentsMap);
									}
									// ...against player A and...
									playersPairStats = opponentsMap.get(aliasPlayerA);
									if(playersPairStats == null) {
										playersPairStats = new ExperimentsStats();
										opponentsMap.put(aliasPlayerA, playersPairStats);
									}
									allGamesPlayersPairStats = allGamesOpponentsMap.get(aliasPlayerA);
									if(allGamesPlayersPairStats == null) {
										allGamesPlayersPairStats = new ExperimentsStats();
										allGamesOpponentsMap.put(aliasPlayerA, allGamesPlayersPairStats);
									}
									for(Double win : samplesPlayerB) {
										playersPairStats.addWins(win);
										allGamesPlayersPairStats.addWins(win);
									}
									// ...against player B.
									playersPairStats = opponentsMap.get(aliasPlayerB);
									if(playersPairStats == null) {
										playersPairStats = new ExperimentsStats();
										opponentsMap.put(aliasPlayerB, playersPairStats);
									}
									allGamesPlayersPairStats = allGamesOpponentsMap.get(aliasPlayerB);
									if(allGamesPlayersPairStats == null) {
										allGamesPlayersPairStats = new ExperimentsStats();
										allGamesOpponentsMap.put(aliasPlayerB, allGamesPlayersPairStats);
									}
									for(Double win : samplesPlayerA) {
										playersPairStats.addWins(win);
										allGamesPlayersPairStats.addWins(win);
									}
									////////////////////////////////////////////////


								}

							}
						}
					}
				}
			}
		}

		// Add to the playerNamesOrder the players that weren't specified
		for(String name : allPlayerNames){
			if(!playerNamesOrder.contains(name)){
				playerNamesOrder.add(name);
			}
		}
		// Add "ALL" as the last player name in the order
		playerNamesOrder.add("ALL");

		String latexRow;

		// This variable is used to memorize the CI as a string so that its format can be checked.
		// If only one decimal position is occupied by a number, the second decimal position is set to 0 in the string.
		// This is needed when creating a tab in latex to have all CIs with two decimal position after the comma.
		String ci;
		String[] splitCi;

		// This variable is used to memorize the win percentage as a string so that its format can be checked.
		// If the number is less than 10, we put in front of it the characters "\,\,\," that in latex will
		// correctly align the number with the other numbers.
		// This is needed when creating a tab in latex to have all CIs with two decimal position after the comma.
		String win;
		String[] splitWin;

		// Iterate over the games in order and create their row in the table
		for(String game : gamesOrder){

			playersMap = gamesMap.get(game);

			if(playersMap != null){

				writeToFile(destinationFilePath, "\\hline");

				// Prepare and write header

				// Get the latex name of the game
				String latexGameKey = gameKeyMap.get(game);
				if(latexGameKey ==  null){
					latexGameKey = game;
				}
				latexRow = latexGameKey;

				for(String gamerName : playerNamesOrder){
					latexRow += (" & " + gamerName);
				}

				latexRow += " \\\\";

				writeToFile(destinationFilePath, latexRow);

				writeToFile(destinationFilePath, "\\hline");

				for(String firstGamerName : playerNamesOrder){
					if(firstGamerName == "ALL") {
						writeToFile(destinationFilePath, "\\hline");
					}
					latexRow = firstGamerName;
					opponentsMap = playersMap.get(firstGamerName);
					if(opponentsMap != null){
						for(String secondGamerName : playerNamesOrder){
							playersPairStats = opponentsMap.get(secondGamerName);
							if(playersPairStats != null){
								if(playersPairStats.getWins().size() < 500) {
									System.out.println("Samples for game " + latexGameKey + " for players " + firstGamerName + " and " + secondGamerName + " are " + playersPairStats.getWins().size());
								}
								ci = "" + round(playersPairStats.getWinsSEM() * 1.96 * 100.0, 2);
								splitCi = ci.split("\\.");
								if(splitCi[1].length() == 1) {
									ci += "0";
								}
								win = "" + round(playersPairStats.getAvgWins() * 100.0, 1);
								splitWin = win.split("\\.");
								if(splitWin[0].length() == 1) {
									win = "\\,\\,\\," + win;
								}

								latexRow += (" & $" + win + "(\\pm" + ci + ")$");
							}else{
								latexRow += (" & ");
							}
						}
					}else{
						for(int j = 0; j < playerNamesOrder.size(); j++){
							latexRow += (" & ");
						}
					}
					latexRow += " \\\\";
					writeToFile(destinationFilePath, latexRow);
				}

				writeToFile(destinationFilePath, "\\hline");
			}

		}

	}

	private static void writeToFile(String filename, String message){
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(filename, true));
			out.write(message+"\n");
            out.close();
		} catch (IOException e) {
			System.out.println("Error writing file " + filename + ".");
			e.printStackTrace();
		}
	}

	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

	private static List<Double> getSamplesFromFile(File samplesFile){

		List<Double> theSamples = new ArrayList<Double>();

		BufferedReader br;
		String theLine;

		try {
			br = new BufferedReader(new FileReader(samplesFile));
			theLine = br.readLine(); // First line is headers
			theLine = br.readLine();

			while(theLine != null){
				String[] splitLine = theLine.split(";");

				if(splitLine.length == 3){

					double sample;
					try{
						sample = Double.parseDouble(splitLine[2]);
						theSamples.add(sample);
					}catch(NumberFormatException e){
						System.out.println("Impossible to read win sample. Skipping.");
						e.printStackTrace();
					}
				}

				theLine = br.readLine();

			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading a file while aggregating the statistics of complete experiment.");
        	e.printStackTrace();
		}

		return theSamples;
	}

	private static String getGameKey(String statsFolderName) {

		String[] splitFolderName = statsFolderName.split("\\.");
		String gameKey;

		if(splitFolderName.length == 3){
			gameKey = splitFolderName[1];
		}else if(splitFolderName.length == 4){
			gameKey = splitFolderName[2];
		}else{
			gameKey = null;
		}

		return gameKey;
	}

}
