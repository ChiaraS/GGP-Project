package csironi.ggp.course.statsSummarizer.paramVisits;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import csironi.ggp.course.statsSummarizer.StatsUtils;

public class ParamVisitsPercentageComputer {

	public static void main(String[] args) {

		if(args.length != 1) {
			System.out.println("Wrong input! Expecting as input the folder of the tournament that contains the -Stats folder of each game in the tourney.");
			return;
		}

		File tourneyFolder = new File(args[0]);

		if(!(tourneyFolder.exists() && tourneyFolder.isDirectory())){
			System.out.println("The specified tourney folder does not exist or is not a folder!");
			return;
		}

		String gameKey;

		File[] gameFolders = tourneyFolder.listFiles();
		for(File gameFolder : gameFolders) {

			//System.out.println(gameFolder.getName());

			if(gameFolder.isDirectory() && gameFolder.getName().endsWith("-Stats")){

				String[] splitString = gameFolder.getName().split("\\.");
				gameKey = splitString[2];

				//System.out.println(gameKey);

				File[] statsFolders = gameFolder.listFiles();
				for(File statsFolder : statsFolders) {
					if(statsFolder.isDirectory() && statsFolder.getName().equals("ParamsStatistics")){

						//System.out.println(statsFolder.getName());

						File[] statsFiles = statsFolder.listFiles();
						for(File statsFile : statsFiles) {
							// For now the percentage is computed only for Local and Global stats for the files of AllRoles
							if(statsFile.isFile() && statsFile.getName().contains("AllRoles") &&
									(statsFile.getName().contains("GlobalParamTuner") || statsFile.getName().contains("LocalParamTuner"))){

								//System.out.println(statsFile.getName());

								Map<String,Map<String,List<TupleVisitsStats>>> visitsForRolesAndTuples = extractVisitsFromFile(statsFile);
								computePercentagesAndOrder(visitsForRolesAndTuples);
								String filename = statsFile.getName().substring(0, (statsFile.getName().length()-13)) + "Percent.csv";
								logPercentages(statsFolder.getPath(), filename, gameKey, visitsForRolesAndTuples);

							}
						}
					}
				}
			}
		}

	}

	/**
	 * This method gets a file with statistics for a tuples of parameters of a fixed length
	 * (i.e. length 1 for the LocalParamTuner-AggrStats.csv file and length "d" for the
	 * GlobalParamTuner-AggrStats.csv file) and extracts for each role, for each tuple with
	 * the given length the actual values (VALUE) that were assigned to the tuple and the
	 * corresponding sum of all the times these values were visited over all the runs of the
	 * game (i.e. TOTAL_VISITS).
	 *
	 * @param theFile
	 * @return
	 */
	public static Map<String,Map<String,List<TupleVisitsStats>>> extractVisitsFromFile(File theFile){

		BufferedReader br = null;

		String theLine;
		String[] splitLine;
		String[] splitSplitLine;

		Map<String,Map<String,List<TupleVisitsStats>>> theMap = new  HashMap<String,Map<String,List<TupleVisitsStats>>>();
		Map<String,List<TupleVisitsStats>> roleMap = null;
		List<TupleVisitsStats> tupleStats = null;

		try {
			br = new BufferedReader(new FileReader(theFile));

			theLine = br.readLine();

			while(theLine != null){

				//System.out.println(theLine);

				if(theLine.equals("")){
					continue;
				}

				splitLine = theLine.split(";");

				// Line of legth 1 could be the role or the type of tuple
				if(splitLine.length == 1) {
					splitSplitLine = splitLine[0].split(" = ");

					if(splitSplitLine[0].equals("ROLE")) {
						String role = splitSplitLine[1];
						// Get map for role
						roleMap = theMap.get(role);
						if(roleMap == null) {
							roleMap = new HashMap<String,List<TupleVisitsStats>>();
							theMap.put(role, roleMap);
						}
					}else if(splitSplitLine[0].equals("PARAM")) {
						String tuple = splitSplitLine[1];
						// Get stats for tuple
						tupleStats = roleMap.get(tuple);
						if(tupleStats == null) {
							tupleStats = new ArrayList<TupleVisitsStats>();
							roleMap.put(tuple, tupleStats);
						}
					}else {
						System.out.println("Unexpected line format - no ROLE nor PARAM specified: " + theLine + ". Skipping line.");
					}

				}else if(splitLine.length == 7) {
					if(!splitLine[0].equals("VALUE")) {

						if(roleMap == null || tupleStats == null) {
							System.out.println("Line with no associated role or tuple: " + theLine + ". Skipping line.");
						}else {

							String value = splitLine[0];
							try {
								int visits = Integer.parseInt(splitLine[1]);

								tupleStats.add(new TupleVisitsStats(value, visits));
							}catch(NumberFormatException nfe) {
								System.out.println("Line with wrong format for the TOTAL_VISITS: " + theLine + ". Skipping line.");
								continue;
							}

						}

					}
				}else {
					System.out.println("Unexpected line format - wrong length: " + theLine + ". Skipping line.");
				}

				theLine = br.readLine();

			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + theFile.getPath() + ".");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + theFile.getPath() + ".");
					ioe.printStackTrace();
				}
        	}
		}

		return theMap;

	}

	/**
	 * Given a map with the extracted visits for all the tuples of all the roles, computes the percentages,
	 * checking that the sum of visits is the same for all the tuples of all the roles. Finally, it orders
	 * the tuple values from the one with highest to the one with lowest visits percentage.
	 *
	 * @param theMap
	 */
	public static void computePercentagesAndOrder(Map<String,Map<String,List<TupleVisitsStats>>> theMap){

		int totVisits = -1;

		for(Entry<String,Map<String,List<TupleVisitsStats>>> roleEntry : theMap.entrySet()) {

			for(Entry<String,List<TupleVisitsStats>> tupleEntry : roleEntry.getValue().entrySet()) {
				// Compute total visits, checking if they are the same computed for other roles and tuples
				int totVisitsForTuple = 0;
				for(TupleVisitsStats tupleValueStats : tupleEntry.getValue()) {
					totVisitsForTuple += tupleValueStats.getVisits();
				}
				if(totVisits == -1) {
					totVisits = totVisitsForTuple;
				}else if(totVisitsForTuple != totVisits) { // Check
					System.out.println("The total visits must be the same for each role-tuple combination, but different values were detected for ROLE=" +
							roleEntry.getKey() + " and TUPLE=" + tupleEntry.getKey() + ". Results might be computed with the wrong number of total visits!");
				}
				for(TupleVisitsStats tupleValueStats : tupleEntry.getValue()) {
					tupleValueStats.computeVisitsPercentage(totVisitsForTuple);
				}
				Collections.sort(tupleEntry.getValue(),
						new Comparator<TupleVisitsStats>() {

					@Override
					public int compare(TupleVisitsStats o1, TupleVisitsStats o2) {
						// Sort from largest to smallest
						if(o1.getVisitsPercentage() > o2.getVisitsPercentage()){
							return -1;
						}else if(o1.getVisitsPercentage() < o2.getVisitsPercentage()){
							return 1;
						}else{
							return 0;
						}
					}

				});
			}
		}

	}

	public static void logPercentages(String destinationFolderPath, String filename, String gameKey,
			Map<String,Map<String,List<TupleVisitsStats>>> visitsForRolesAndTuples) {

		// List the roles
		List<String> orderedRoles = new ArrayList<String>();
		for(String role : visitsForRolesAndTuples.keySet()) {
			orderedRoles.add(role);
		}
		// List all tuples
		Set<String> tuples = new HashSet<String>();
		for(Entry<String,Map<String,List<TupleVisitsStats>>> roleEntry : visitsForRolesAndTuples.entrySet()) {
			for(String tuple : roleEntry.getValue().keySet()) {
				tuples.add(tuple);
			}
		}
		List<String> orderedTuples = new ArrayList<String>();
		for(String tuple : tuples) {
			orderedTuples.add(tuple);
		}

		String newline = "\n";
		// Log game name
		String toLog = gameKey + ";" + newline;
		// Log the roles
		for(String role : orderedRoles) {
			toLog += (role + ";");
		}
		toLog += newline;
		// Log the tuples for each role
		for(String role : orderedRoles) {
			for(String tuple : orderedTuples) {
				toLog += (tuple + ";");
			}
		}
		toLog += newline;
		// Oder lists of tuple values stats according to the order of roles and tuples,
		// keeping track of the maximum length of all the lists
		int maxLength = 0;
		Map<String,List<TupleVisitsStats>> roleMap;
		List<TupleVisitsStats> tupleStats;
		List<List<TupleVisitsStats>> orderedStats = new ArrayList<List<TupleVisitsStats>>();
		for(String role : orderedRoles) {
			roleMap = visitsForRolesAndTuples.get(role);
			if(roleMap == null) {
				for(String tuple : orderedTuples) {
					orderedStats.add(new ArrayList<TupleVisitsStats>());
				}
			}else {
				for(String tuple : orderedTuples) {
					tupleStats = roleMap.get(tuple);
					if(tupleStats == null) {
						orderedStats.add(new ArrayList<TupleVisitsStats>());
					}else {
						orderedStats.add(tupleStats);
						if(tupleStats.size() > maxLength) {
							maxLength = tupleStats.size();
						}
					}
				}
			}
		}
		// Log the headers with "n-tuple value" and "percentage" for each combination of role and n-tuple.
		for(int i = 0; i < orderedStats.size(); i++) {
			toLog += ("Value;Percentage;");
		}
		toLog += newline;
		// Log tuple value and percentage per row, therefore getting the first stats of each stats list in the first row,
		// the second stats of each stats list in the second row, the third stats of each stats list in the third row, etc.
		int index = 0;
		while(index < maxLength) {
			for(List<TupleVisitsStats> statsList : orderedStats) {
				if(index < statsList.size()) {
					TupleVisitsStats stats = statsList.get(index);
					toLog += stats.getTuple() + ";" + stats.getVisitsPercentage() + ";";
				}else {
					toLog += ";;";
				}
			}
			toLog += newline;
			index++;
		}

		StatsUtils.writeToFile(destinationFolderPath + "/" + filename, toLog);

	}


}
