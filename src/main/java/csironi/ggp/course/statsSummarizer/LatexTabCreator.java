package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class creates a file containing the table latex formatting of the given stats.
 * Each row in the table corresponds to a game, each column to a playing algorithm.
 * For each gameKey this class will represent the name as returned by the gameKeyMap.
 * The games in the rows are ordered according to the gamesOrder.
 * The class expects as input the folder that contains a file of statistics for each
 * algorithm and the name of the destination file where to save the latex representation
 * of the aggragated statistics. If the files with the stats of each algorithm start with
 * a number, their content will be ordered in the columns according to that number (from
 * the smallest to the largest number).
 *
 * @author C.Sironi
 *
 */
public class LatexTabCreator {

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


		/*********** GAMES KEY MAP FOR PN PAPER *************
		gameKeyMap.put("amazons","amazons");
		gameKeyMap.put("battle","battle");
		gameKeyMap.put("breakthrough","breakthr.");
		gameKeyMap.put("chineseCheckers1","c-check.1");
		gameKeyMap.put("chineseCheckers2","c-check.2");
		gameKeyMap.put("chineseCheckers3","c-check.3");
		gameKeyMap.put("chineseCheckers4","c-check.4");
		gameKeyMap.put("chineseCheckers6","c-check.6");
		gameKeyMap.put("connect4","connect4");
		gameKeyMap.put("othello-comp2007","othello");
		gameKeyMap.put("pentago","pentago");
		gameKeyMap.put("skirmish","skirmish");
		gameKeyMap.put("ticTacToe","ticTacToe");
		*********** GAMES KEY MAP FOR PN PAPER - END *************/

		gameKeyMap.put("OverallAvg","Avg Win\\%");

		gamesOrder = new ArrayList<String>();

		// Gives the order in which games results must be inserted in the table

		/*********** GAMES ORDER FOR PN PAPER ************
		gamesOrder.add("amazons");
		gamesOrder.add("battle");
		gamesOrder.add("breakthrough");
		gamesOrder.add("chineseCheckers1");
		gamesOrder.add("chineseCheckers2");
		gamesOrder.add("chineseCheckers3");
		gamesOrder.add("chineseCheckers4");
		gamesOrder.add("chineseCheckers6");
		gamesOrder.add("connect4");
		gamesOrder.add("othello-comp2007");
		gamesOrder.add("pentago");
		gamesOrder.add("skirmish");
		gamesOrder.add("ticTacToe");
		*********** GAMES ORDER FOR PN PAPER - END *************/

		/*********** GAMES ORDER FOR GRAVE PAPER *************/
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
		/*********** GAMES ORDER FOR GRAVE PAPER -END *************/

		gamesOrder.add("OverallAvg");

	}

	public static void main(String[] args) {

		if(args.length != 2){
			System.out.println("Give as arguments the folder containing the *Latex.csv files and the name of the file that will contain the latex representation of the table rows.");
			return;
		}

		String folderPath = args[0];

		File folder = new File(folderPath);

		if(!folder.isDirectory()){
			System.out.println("Cannot find the specified folder.");
		}

		String filePath = folderPath + "/" + args[1] + "Table";

		File[] allFiles = folder.listFiles();

		List<Integer> filePriorities = new ArrayList<Integer>();
		List<Map<String, String>> orderedFileMaps = new ArrayList<Map<String, String>>();

		for(File f : allFiles){

			if(f.isFile() && f.getName().endsWith("-Latex.csv")){

				System.out.println("File = " + f.getName());

				// 1. parse the file and get the map (latexgame-latexscore)
				Map<String, String> fileMap = getFileMap(f);

				String[] splitFileName = f.getName().split("\\.");

				// 1. Order files depending on their priority. No priority means lowest priority.
				int priority = -1;
				try{
					priority = Integer.parseInt(splitFileName[0]);

					System.out.println("Priority = " + priority);

				}catch(NumberFormatException e){

				}

				if(priority == -1){
					filePriorities.add(new Integer(-1));
					orderedFileMaps.add(fileMap);

				}else if(orderedFileMaps.isEmpty()){
					filePriorities.add(new Integer(priority));
					orderedFileMaps.add(fileMap);
				}else{

					int i = 0;

					while(i < filePriorities.size() && filePriorities.get(i).intValue() != -1 && filePriorities.get(i).intValue() < priority){
						i++;
					}

					if(i == filePriorities.size()){
						filePriorities.add(new Integer(priority));
						orderedFileMaps.add(fileMap);
					}else{
						filePriorities.add(i, new Integer(priority));
						orderedFileMaps.add(i, fileMap);
					}
				}
			}
		}

		System.out.println(filePriorities);

		// 2. Iterate over the games in order and create their row in the table
		for(String gameKey : gamesOrder){

			String latexRow = "";

			// First add the name of the game
			String latexGameKey = gameKeyMap.get(gameKey);
			if(latexGameKey ==  null){
				latexGameKey = gameKey;
			}

			latexRow += latexGameKey;

			boolean emptyLine = true;

			// Get the latex cell for each file in the order specified by the priorities
			for(Map<String, String> fileMap : orderedFileMaps){
				String latexCell = fileMap.get(gameKey);

				if(latexCell != null){
					latexRow += " & " + latexCell;
					emptyLine = false;
				}else{
					latexRow += " &";
				}
			}

			if(!emptyLine){
				latexRow += " \\\\";
				writeToFile(filePath, latexRow);
			}

		}


	}

	private static Map<String, String> getFileMap(File f){

		Map<String, String> theMap = new HashMap<String, String>();

		BufferedReader br;

		try{
			br = new BufferedReader(new FileReader(f));
			String theLine = br.readLine();

			while(theLine != null && !(theLine.equals(""))){

				String[] splitLine = theLine.split(";");

				if(splitLine.length == 2){
					theMap.put(splitLine[0], splitLine[1]);
				}

				theLine = br.readLine();
			}

			theLine = br.readLine(); // Check if there is also the overall avg

			if(theLine != null && !(theLine.equals(""))){
				String[] splitLine = theLine.split(";");

				if(splitLine.length == 2){
					theMap.put(splitLine[0], splitLine[1]);
				}
			}

			br.close();
		}catch (IOException e) {
			System.out.println("Exception when reading a file while creating latex table.");
        	e.printStackTrace();
        	theMap = null;
		}

		return theMap;
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

}
