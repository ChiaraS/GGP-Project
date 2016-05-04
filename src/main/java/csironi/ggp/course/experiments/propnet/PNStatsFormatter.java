package csironi.ggp.course.experiments.propnet;

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
import java.util.Map.Entry;

import csironi.ggp.course.utils.Pair;

public class PNStatsFormatter {

	public static final Map<String, String> gameKeyMap;

	public static final List<String> gamesOrder;

	public static final List<String> optOrder;

	static{
		gameKeyMap = new HashMap<String, String>();

		// Maps the game keys into the corresponding game name we want to have in the paper
		gameKeyMap.put("tictactoe_3d_2player","3DTicTacToe");
		gameKeyMap.put("breakthrough","breakthr.");
		gameKeyMap.put("knightThrough","Knightthrough");
		gameKeyMap.put("skirmish","skirmish");
		gameKeyMap.put("battle","battle");
		gameKeyMap.put("chinook","chinook");
		gameKeyMap.put("chineseCheckers1","c-check.1");
		gameKeyMap.put("chineseCheckers2","c-check.2");
		gameKeyMap.put("chineseCheckers3","c-check.3");
		gameKeyMap.put("chineseCheckers4","c-check.4");
		gameKeyMap.put("chineseCheckers6","c-check.6");
		gameKeyMap.put("checkers","Checkers");
		gameKeyMap.put("connect5","Connect 5");
		gameKeyMap.put("connect4","connect4");
		gameKeyMap.put("othello-comp2007","othello");
		gameKeyMap.put("quad_7x7","Quad");
		gameKeyMap.put("sheepAndWolf","SheepAndWolf");
		gameKeyMap.put("ttcc4_2player","TTCC4 2P");
		gameKeyMap.put("zhadu","Zhadu");
		gameKeyMap.put("ttcc4","TTCC4 3P");
		gameKeyMap.put("amazons","amazons");
		gameKeyMap.put("pentago","pentago");
		gameKeyMap.put("ticTacToe","ticTacToe");

		gameKeyMap.put("OverallAvg","Avg Win\\%");

		gamesOrder = new ArrayList<String>();

		// Gives the order in which games statistics must be inserted in the table
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

		optOrder = new ArrayList<String>();

		optOrder.add("Prover");
		optOrder.add("CacheProver");
		optOrder.add("60CacheProver");
		optOrder.add("BasePN");
		optOrder.add("Opt0PN");
		optOrder.add("Opt1PN");
		optOrder.add("Opt2PN");
		optOrder.add("Opt3PN");
		optOrder.add("Opt02PN");
		optOrder.add("Opt12PN");
		optOrder.add("Opt102PN");
		optOrder.add("Opt13PN");
		optOrder.add("Opt23PN");
		optOrder.add("Opt323PN");
		optOrder.add("Opt1023PN");
		optOrder.add("Opt10323PN");
		optOrder.add("Cache1023PN");
		optOrder.add("Cache10323PN");
		optOrder.add("60Cache1023PN");
	}

	public static void main(String[] args){

		if(args.length != 1){
			System.out.println("Specify the folder where to find the statistics for each optimization.");
			return;
		}

		String theFolderPath = args[0];

		File theFolder = new File(theFolderPath);

		if(!theFolder.isDirectory()){
			System.out.println("Cannot find the specified folder.");
			return;
		}

		System.out.println("Starting...");

		File[] statFiles = theFolder.listFiles();

		Map<String, Map<String, Map<String, Pair<Double, Double>>>> statsMap = new HashMap<String, Map<String, Map<String, Pair<Double, Double>>>>();

		for(File f : statFiles){
			if(f.isFile() && f.getName().endsWith(".csv")){

				String filename = f.getName();

				System.out.println("Parsing file " + filename);

				String optType = filename.split("-")[0];

				BufferedReader br;
				String theLine;
				try {
					br = new BufferedReader(new FileReader(f));
					// Read header
					theLine = br.readLine();

					String[] statTypes = theLine.split(";");

					theLine = br.readLine();

					while(theLine != null){
						if(theLine.equals("") || theLine.split(";").length == 0 || theLine.split(";")[0].equals("")){
							theLine = br.readLine();
							continue;
						}

						String[] splitAvgLine = theLine.split(";");

						if(splitAvgLine[1].equals("AVG")){
							String gameKey = splitAvgLine[0];

							String ciLine = br.readLine();

							String[] splitCiLine = ciLine.split(";");

							for(int i = 2; i < statTypes.length; i++){
								Map<String, Map<String, Pair<Double, Double>>> optMap = statsMap.get(statTypes[i]);

								if(optMap == null){
									optMap = new HashMap<String, Map<String, Pair<Double, Double>>>();
									statsMap.put(statTypes[i], optMap);
								}

								Map<String, Pair<Double, Double>> gamesMap = optMap.get(optType);

								if(gamesMap == null){
									gamesMap = new  HashMap<String, Pair<Double, Double>>();
									optMap.put(optType, gamesMap);
								}

								Pair<Double, Double> statValues = gamesMap.get(gameKey);

								if(statValues != null){
									System.out.println("Found statistic with more than one value! Interrupting!");
									return;
								}

								double avg = Double.parseDouble(splitAvgLine[i]);
								double ci = Double.parseDouble(splitCiLine[i]);

								statValues = new Pair<Double, Double>(avg, ci);

								gamesMap.put(gameKey, statValues);
							}
						}

						theLine = br.readLine();
					}

					br.close();
				} catch (IOException | NumberFormatException e) {
					System.out.println("Exception when reading one line of the .csv file " + f.getName() + ".");
					System.out.println("Excluding " + optType + " from summarization.");
		        	e.printStackTrace();

		        	for(Entry<String,  Map<String, Map<String, Pair<Double, Double>>>> entry : statsMap.entrySet()){
		        		entry.getValue().remove(optType);
		        	}
		        	continue;
				}
			}
		}

		String theStatsFolderPath = theFolderPath + "/Stats";
		File theStatsFolder = new File(theStatsFolderPath);

		if(!theStatsFolder.isDirectory()){
			theStatsFolder.mkdirs();
		}else{
			System.out.println("The directory with the statistics already exists. If you want to recompute them, delete the folder first.");
			return;
		}

		for(Entry<String, Map<String, Map<String, Pair<Double, Double>>>> entry : statsMap.entrySet()){

			String theStatsFilePath = theStatsFolderPath + "/" + entry.getKey() + "-Stats.csv";
			String theLatexFilePath = theStatsFolderPath + "/" + entry.getKey() + "-Latex.txt";
			String theCSVLatexFilePath = theStatsFolderPath + "/" + entry.getKey() + "-CSVLatex.csv";
			String theCSVLatexFilePathNoCi = theStatsFolderPath + "/" + entry.getKey() + "-CSVNoCiLatex.csv";

			String statsLine = "Game;";
			String latexLine = "Game";
			String csvLatexLine = "Game";
			String csvLatexLineNoCi = "Game";

			boolean foundOne = false;

			for(String opt : optOrder){
				if(entry.getValue().get(opt) != null){
					statsLine += opt + "_AVG;" + opt + "_CI;";
					latexLine += " & " + opt;
					csvLatexLine += ";" + " & " + opt;
					csvLatexLineNoCi += ";" + " & " + opt;

					foundOne = true;
				}
			}

			latexLine += "\\\\";
			csvLatexLine += ";\\\\;";
			csvLatexLineNoCi += ";\\\\;";

			if(!foundOne){
				continue;
			}

			writeToFile(theStatsFilePath, statsLine);
			writeToFile(theLatexFilePath, latexLine);
			writeToFile(theCSVLatexFilePath, csvLatexLine);
			writeToFile(theCSVLatexFilePathNoCi, csvLatexLineNoCi);

			for(String game : gamesOrder){

				String gameKey = gameKeyMap.get(game);
				if(gameKey == null){
					gameKey = game;
				}
				statsLine = gameKey + ";";
				latexLine = gameKey;
				csvLatexLine = gameKey;
				csvLatexLineNoCi = gameKey;

				boolean atLeastOneValue = false;

				for(String opt : optOrder){

					Map<String, Pair<Double, Double>> gamesMap = entry.getValue().get(opt);

					if(gamesMap != null){

						Pair<Double, Double> statsValues = gamesMap.get(game);

						if(statsValues != null){

							atLeastOneValue = true;

							statsLine += statsValues.getFirst() + ";" + statsValues.getSecond() + ";";
							if(entry.getKey().contains("PerSecond")){
								latexLine += " & $" + adaptiveRound(statsValues.getFirst()) + "(\\pm " + round(statsValues.getSecond(),2) + ")$";
								csvLatexLine += "; & $" + adaptiveRound(statsValues.getFirst()) + "(\\pm " + round(statsValues.getSecond(),2) + ")$";
								csvLatexLineNoCi += "; & $" + adaptiveRound(statsValues.getFirst()) + "$";
							}else{
								//latexLine += " & $" + round(statsValues.getFirst(),2) + "(\\pm " + round(statsValues.getSecond(),2) + ")$";
								//csvLatexLine += "; & $" + round(statsValues.getFirst(),2) + "(\\pm " + round(statsValues.getSecond(),2) + ")$";
								//csvLatexLineNoCi += "; & $" + round(statsValues.getFirst(),2) + "$";
								latexLine += " & $" + ((int)round(statsValues.getFirst(),0)) + "(\\pm " + round(statsValues.getSecond(),2) + ")$";
								csvLatexLine += "; & $" + ((int)round(statsValues.getFirst(),0)) + "(\\pm " + round(statsValues.getSecond(),2) + ")$";
								csvLatexLineNoCi += "; & $" + ((int)round(statsValues.getFirst(),0)) + "$";
							}

						}else{
							statsLine += ";;";
							latexLine += " & ";
							csvLatexLine += "; & ";
							csvLatexLineNoCi += "; & ";
						}
					}
				}

				latexLine += "\\\\";
				csvLatexLine +=  ";\\\\;";
				csvLatexLineNoCi +=  ";\\\\;";

				if(atLeastOneValue){
					writeToFile(theStatsFilePath, statsLine);
					writeToFile(theLatexFilePath, latexLine);
					writeToFile(theCSVLatexFilePath, csvLatexLine);
					writeToFile(theCSVLatexFilePathNoCi, csvLatexLineNoCi);
				}
			}
		}
	}

	private static double adaptiveRound(double value){

		if(value < 100.0){
			return round(value, 3);
		}else{
			return round(value, 0);
		}
	}

	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
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


























