package csironi.ggp.course.statsSummarizer.treeDrawer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ggp.base.util.Interval;

import com.google.common.io.Files;

public class TreePlotLogsExtractor {

	/**
	 * OLD EXTRACTOR: this class extracts logs to plot the tree outside of the Stats folder.
	 *
	 * Given the path of a folder with the folders containing the statistics of a set of games
	 * (i.e. the folders ending with -Stats), extracts the TreeSizeLog.csv files for each game,
	 * for each player type, for each role played by the type, for each possible assignments of
	 * other roles. The TreeSizeLog.csv file is extracted for all the matches with the match
	 * number in the specified interval.
	 * The default now is match 0 of each combination.
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
	 * E.g. input
	 *
	 * [folderPathWithStatsFolders] [resultFolderName] [matchNumberInterval] [(optional) listOfAliases]
	 *
	 * C:\Users\c.sironi\RES\GGP\!!!!!BranchingFactorPlot\4PNTBEAvs4PRND 4PNTBEAvs4PRND 0-5 4PRND=Print4PRandomTunerCGMDMctsGamer 4PNTBEA=Print4PNTBEATunerCGMDMctsGamer
	 *
	 * @param args
	 */
	public static void main(String args[]) {

		/************************************ Prepare the folders *********************************/

		if(args.length < 3){
			System.out.println("Impossible to collect logs to create tree plots. Specify the absolute path of the folder containing statistics, the name of the folder that will contain the logs and the plots, and the interval for the match numbers to be extracted.");
			return;
		}

		String sourceFolderPath = args[0];
		String resultFolderPath = sourceFolderPath + "/" + args[1];
		String[] interval = args[2].split("-");
		Interval matchInterval = new Interval(Integer.parseInt(interval[0]), Integer.parseInt(interval[1]), true, true);

		// Create a map that maps each player to its alias
		Map<String,String> aliases = new HashMap<String,String>();

		// Fill the map
		String[] aliasSpecification;
		String alias;
		String[] playersWithSameAlias;
		for(int i = 3; i < args.length; i++){
			aliasSpecification = args[i].split("=");

			//System.out.println(aliasSpecification);

			alias = aliasSpecification[0];

			//System.out.println();

			playersWithSameAlias = aliasSpecification[1].split(";");
			for(int j = 0; j < playersWithSameAlias.length; j++){

				//System.out.println(playersWithSameAlias[j]);

				aliases.put(playersWithSameAlias[j], alias);
			}
		}

		System.out.println(sourceFolderPath);
		System.out.println(resultFolderPath);

		System.out.println("ALIASES:");
		if(aliases.isEmpty()){
			System.out.println("none");
		}else{
			for(Entry<String,String> entry : aliases.entrySet()){
				System.out.println(entry.getKey() + " = " + entry.getValue());
			}
		}

		File sourceFolder = new File(sourceFolderPath);

		if(!sourceFolder.isDirectory()){
			System.out.println("Impossible to find the directory with the statistics to move.");
			return;
		}

		File resultFolder = new File(resultFolderPath);

		if(resultFolder.isDirectory()){
			System.out.println("The folder where to move the tree log files already exists! Only the logs that are not already in this folder will be moved!");
			//return;
		}

		File[] gamesDirs = sourceFolder.listFiles();

		String gameKey;

		File[] statsDirs;

		File[] playerDirs;

		String playerType;

		File[] roleDirs;

		String roleName;

		File[] treePlotFiles;

		// For the folder of each game...
		for(int i = 0; i < gamesDirs.length; i++){

			if(gamesDirs[i].isDirectory() && gamesDirs[i].getName().endsWith("-Stats")){

				String[] splitGameFolder = gamesDirs[i].getName().split("\\.");
				if(splitGameFolder.length == 4) {
					gameKey = splitGameFolder[2];
				}else {
					gameKey = splitGameFolder[1];
				}

				//System.out.println(gameKey);

				// ...scan the content until you find the "TreePlotLogs" folder of the game.
				statsDirs = gamesDirs[i].listFiles();

				for(int j = 0; j < statsDirs.length; j++){

					if(statsDirs[j].isDirectory() && statsDirs[j].getName() != null && statsDirs[j].getName().equals("TreePlotLogs")){

						playerDirs = statsDirs[j].listFiles();

						for(int k = 0; k < playerDirs.length; k++){

							if(aliases.containsKey(playerDirs[k].getName())) {
								playerType = aliases.get(playerDirs[k].getName());
							}else {
								playerType = playerDirs[k].getName();
							}

							//System.out.println(playerType);

							roleDirs = playerDirs[k].listFiles();

							for(int l = 0; l < roleDirs.length; l++){

								roleName = roleDirs[l].getName();

								//System.out.println(roleName);

								treePlotFiles = roleDirs[l].listFiles();

								for(int m = 0; m < treePlotFiles.length; m++){

									String[] splitFileName = treePlotFiles[m].getName().split("\\.");

									int matchNumebr = Integer.parseInt(splitFileName[0]);

									if(matchInterval.contains(matchNumebr)) {

										String destinationPath = resultFolderPath + "/" + gameKey + "/" + playerType + "/" + roleName;

										File destinationFolder = new File(destinationPath);
										destinationFolder.mkdirs();

										File destinationFile = new File(destinationPath + "/" + treePlotFiles[m].getName());

										if(!destinationFile.exists() || !destinationFile.isFile()) {

											try {
												Files.copy(treePlotFiles[m], destinationFile);
											} catch (IOException e) {
												System.out.println("Error copying file! Trying to find another file for match 0 in the folder.");
												e.printStackTrace();
											}

										}

									}

								}

							}

						}

					}

				}

			}

		}

	}

}
