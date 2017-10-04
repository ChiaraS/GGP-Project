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

import csironi.ggp.course.utils.MyPair;

/**
 * This class aggregates in a double entry table (formatted in Latex) the results of
 * multiple experiments that involve a certain number of players matched against each
 * other.
 *
 * I.e. if we have the following results files:
 * - PlayerA vs PlayerB
 * - PlayerA vs PlayerC
 * - PlayerB vs PlayerC
 *
 * each of which contains results for the following games: Game1, Game2, Game3,
 * then this class will output a latex representation of the following table:
 * _________________________________________
 * Game1   | PlayerA  | PlayerB  | PlayerC  |
 * -----------------------------------------
 * PlayerA | 		  | win%AvsB | win%AvsC |
 * PlayerB | win%BvsA |			 | win%BvsC |
 * PlayerC | win%CvsA | win%CvsB |			|
 * _________________________________________
 * Game2   | PlayerA  | PlayerB  | PlayerC  |
 * -----------------------------------------
 * PlayerA | 		  | win%AvsB | win%AvsC |
 * PlayerB | win%BvsA |			 | win%BvsC |
 * PlayerC | win%CvsA | win%CvsB |			|
 * _________________________________________
 * Game3   | PlayerA  | PlayerB  | PlayerC  |
 * -----------------------------------------
 * PlayerA | 		  | win%AvsB | win%AvsC |
 * PlayerB | win%BvsA |			 | win%BvsC |
 * PlayerC | win%CvsA | win%CvsB |			|
 *
 * For each gameKey this class will represent the name as returned by the gameKeyMap.
 * The games in the tables are ordered according to the gamesOrder.
 * The class expects as input the path of the folder that contains a file of win
 * statistics for each experiment we want to consider.
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
 */
public class MultiplayerLatexTabCreator {

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
			System.out.println("Expecting at least 1 argument: the folder containing the *WinStatistics.csv files.");
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

		File[] allFiles = folder.listFiles();

		// Maps each game to the maps with the player names for the first player (row player).
		Map<String,Map<String,Map<String,MyPair<Double,Double>>>> mapsPerGame = new HashMap<String,Map<String,Map<String,MyPair<Double,Double>>>> ();

		String[] splitLine;
		String playerA;
		String playerB;
		String aliasPlayerA;
		String aliasPlayerB;
		String playerName;

		for(File f : allFiles){

			if(f.isFile() && f.getName().endsWith("WinsStatistics.csv")){

				System.out.println("File = " + f.getName());

				BufferedReader br;

				try{
					br = new BufferedReader(new FileReader(f));

					// Read and skip header
					String aLine = br.readLine();
					// Read and skip empty line
					aLine = br.readLine();

					// Read line for playerA
					aLine = br.readLine();
					// Read line for playerB
					String anotherLine = br.readLine();

					// Find name of playerA
					splitLine = aLine.split(";");
					if(splitLine.length != 11){
						System.out.println("Line with wrong number of entries. Error in the format of the file!");
						System.out.println("Line that caused error: " + aLine);
						System.out.println("Continuing parsing anyway.");
					}
					playerA = splitLine[1];
					aliasPlayerA = aliases.get(playerA); // If no alias is specified, then this will be null;
					if(aliasPlayerA == null){
						aliasPlayerA = playerA;
					}

					// Find name of playerB
					splitLine = anotherLine.split(";");
					if(splitLine.length != 11){
						System.out.println("Line with wrong number of entries. Error in the format of the file!");
						System.out.println("Line that caused error: " + anotherLine);
						System.out.println("Continuing parsing anyway.");
					}
					playerB = splitLine[1];
					aliasPlayerB = aliases.get(playerB);
					if(aliasPlayerB == null){
						aliasPlayerB = playerB;
					}

					allPlayerNames.add(aliasPlayerA);
					allPlayerNames.add(aliasPlayerB);

					// Process line of playerA
					processLine(aLine.split(";"), mapsPerGame, aliasPlayerA, aliasPlayerB);

					// Process line of playerB
					processLine(anotherLine.split(";"), mapsPerGame, aliasPlayerB, aliasPlayerA);

					// Process the rest of the lines
					aLine = br.readLine();

					while(aLine != null){

						splitLine = aLine.split(";");

						if(splitLine.length == 11){
							playerName = splitLine[1];

							if(playerName.equals(playerA)){
								processLine(splitLine, mapsPerGame, aliasPlayerA, aliasPlayerB);
							}else if(playerName.equals(playerB)){
								processLine(splitLine, mapsPerGame, aliasPlayerB, aliasPlayerA);
							}else{
								System.out.println("Unrecognized player type: " + playerName);
								System.out.println("Skipping line.");
							}
						}

						aLine = br.readLine();
					}

					br.close();
				}catch (IOException e) {
					System.out.println("Exception when reading a file while creating latex table.");
		        	e.printStackTrace();
				}

			}

		}

		Map<String,Map<String,MyPair<Double,Double>>> gameMap;
		Map<String,MyPair<Double,Double>> firstPlayerMap;
		MyPair<Double,Double> firstAndSecondPlayerStat;

		// Add to the playerNamesOrder the players that weren't specified
		for(String name : allPlayerNames){
			if(!playerNamesOrder.contains(name)){
				playerNamesOrder.add(name);
			}
		}

		String latexRow;

		// Iterate over the games in order and create their row in the table
		for(String game : gamesOrder){

			gameMap = mapsPerGame.get(game);

			if(gameMap != null){

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
					latexRow = firstGamerName;
					firstPlayerMap = gameMap.get(firstGamerName);
					if(firstPlayerMap != null){
						for(String secondGamerName : playerNamesOrder){
							firstAndSecondPlayerStat = firstPlayerMap.get(secondGamerName);
							if(firstAndSecondPlayerStat != null){
								latexRow += (" & $" + firstAndSecondPlayerStat.getFirst() + "(\\pm" + firstAndSecondPlayerStat.getSecond() + ")$");
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

	private static void processLine(String[] splitLine, Map<String,Map<String,Map<String,MyPair<Double,Double>>>> mapsPerGame, String firstPlayerName, String secondPlayerName){

		String gameKey = splitLine[0];

		Map<String,Map<String,MyPair<Double,Double>>> gameMap = mapsPerGame.get(gameKey);
		if(gameMap == null){
			gameMap = new HashMap<String,Map<String,MyPair<Double,Double>>>();
			mapsPerGame.put(gameKey, gameMap);
		}

		Map<String,MyPair<Double,Double>> firstPlayerMap = gameMap.get(firstPlayerName);
		if(firstPlayerMap == null){
			firstPlayerMap = new HashMap<String,MyPair<Double,Double>>();
			gameMap.put(firstPlayerName, firstPlayerMap);
		}

		MyPair<Double,Double> firstAndSecondPlayerStat = firstPlayerMap.get(secondPlayerName);
		if(firstAndSecondPlayerStat == null){
			firstAndSecondPlayerStat = new MyPair<Double,Double>(round(Double.parseDouble(splitLine[7]), 1), round(Double.parseDouble(splitLine[8]),2));
			firstPlayerMap.put(secondPlayerName, firstAndSecondPlayerStat);
		}else{
			System.out.println("Duplicate statistic for the following combination of players: [" + firstPlayerName + " vs " + secondPlayerName + "]");
			System.out.println("Statistic excluded from summary!");
		}

	}

}
