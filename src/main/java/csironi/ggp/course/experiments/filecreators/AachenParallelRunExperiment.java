package csironi.ggp.course.experiments.filecreators;

import java.util.HashMap;
import java.util.Map;

import csironi.ggp.course.statsSummarizer.StatsUtils;

public class AachenParallelRunExperiment {

	public static final Map<String,String> gameKeyMap;

	static{
		gameKeyMap = new HashMap<String,String>();

		gameKeyMap.put("breakthrough", "BT");
		gameKeyMap.put("knightThrough", "KT");
		gameKeyMap.put("connect4", "C4");
		gameKeyMap.put("connect5", "C5");
		gameKeyMap.put("sheepAndWolf", "SW");
		gameKeyMap.put("checkers", "CH");
		gameKeyMap.put("chinook", "CHI");
		gameKeyMap.put("chineseCheckers3", "CC3");
		gameKeyMap.put("pentago", "PE");
		gameKeyMap.put("quad_7x7", "QU");
		gameKeyMap.put("reversi", "RE");
		gameKeyMap.put("tictactoe_3d_2player", "3D");
		gameKeyMap.put("ttcc4_2player", "TC2");
		gameKeyMap.put("ttcc4", "TC3");

	}

	public static void main(String[] args){

		String path = args[0];

		/*
		String[] gameKeys = {"breakthrough", "chineseCheckers3", "connect4",
				"knightThrough", "chinook", "tictactoe_3d_2player", "checkers",
				"connect5", "pentago", "reversi", "ttcc4", "ttcc4_2player",
				"sheepAndWolf", "quad_7x7"};
		*/

		/*String[] gameKeys = {"chineseCheckers3", "connect4", "pentago", "reversi",
				"ttcc4", "ttcc4_2player", "sheepAndWolf"};*/

		String[] gameKeys = {"checkers"};

		String runnerName = "032NPPAvsDUCT";

		String tourneyName = "20000sim0321DGSNPPAvsDUCT";

		String gamerType1 = "20000sim032A1DGSNPPADuct";

		String gamerType2 = "20000simDuct";

		//String timeID = "" + System.currentTimeMillis();
		String timeID = "1591376419231";



		int gameRuns = 125;

		for(String gameKey : gameKeys){

			String gameAcronym = gameKeyMap.get(gameKey);

			StatsUtils.writeToFileMkParentDir(path + "/" + gameAcronym + "/" + gameAcronym + runnerName + "Runner.sh", "#!/bin/bash\n");

			for(int runNumber = 0; runNumber < gameRuns; runNumber++){

				// Batch file
				String content = "#!/usr/local_rwth/bin/zsh\n"+
								 "# Name the job\n" +
								 "#SBATCH -J " + gameAcronym + runnerName + "Runner\n" +
								 "# Declare the STDOUT file\n" +
								 "#SBATCH -o " + gameAcronym + runnerName + "Runner_%J.out\n" +
								 "# Declare the STDERR file\n" +
								 "#SBATCH -e " + gameAcronym + runnerName + "Runner_%J.err\n" +
								 "# Ask for 40 GB memory\n" +
								 "#SBATCH --mem-per-cpu=10G\n" +
								 "# Set time limit in minutes\n" +
								 "#SBATCH -t 10000\n" +
								 "# Mention DKE as project\n" +
								 "#SBATCH -A um_dke\n" +
								 "\n" +
								 "java -jar IndependentTourneyRunner.jar " + runNumber + gameAcronym + tourneyName + ".properties </dev/null >" + runNumber + gameAcronym + tourneyName + ".log 2>&1\n";

				StatsUtils.writeToFileMkParentDir(path + "/" + gameAcronym + "/" + runNumber + gameAcronym + runnerName + "Runner.sh", content);

				// Batch file that contains
				StatsUtils.writeToFileMkParentDir(path + "/" + gameAcronym + "/" + gameAcronym + runnerName + "Runner.sh", "sbatch ./" + runNumber + gameAcronym + runnerName + "Runner.sh\n");


				//System.out.println(path + "/" + gameAcronym + "/" + runNumber + gameAcronym + runnerName + "Runner.sh");
				//System.out.println(content);

				// .properties file

				content = "tourneyName=" + tourneyName + "\n" +
						"gameKeys=" + gameKey + ";\n" +
						"startClock=1000000\n" +
						"playClock=1000000\n" +
						"pnCreationTime=1200000\n" +
						"numParallelPlayers=2\n" +
						"matchesPerGamerType=4\n" +
						"numSequentialMatches=1\n" +
						"theGamersTypes=MctsGamer-" + gamerType1 + ".properties;MctsGamer-" + gamerType2 + ".properties;\n" +
						"runNumber=" + runNumber + "\n";

				//if(runNumber > 0){
					content += "continueOldExperiment=true\n";
					content += "timeID=" + timeID + "\n";
					content += "finishInterruptedExperiment=true";
				//}

				StatsUtils.writeToFileMkParentDir(path + "/" + gameAcronym + "/" + runNumber + gameAcronym + tourneyName + ".properties", content);

				//System.out.println(path + "/" + gameAcronym + "/" + runNumber + gameAcronym + tourneyName + ".properties");
				//System.out.println(content);


			}

		}


	}

}
