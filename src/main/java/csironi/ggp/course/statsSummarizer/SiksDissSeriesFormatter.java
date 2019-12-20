package csironi.ggp.course.statsSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SiksDissSeriesFormatter {

	private static Map<String,String> universityAliases;

	private static Set<String> universities;

	private static Map<String,String> universitiesFullNames;

	static{
		universityAliases = new HashMap<String,String>();

		// Maps the game keys into the corresponding game name we want to have in the paper
		universityAliases.put("TU/E","TUE");
		universityAliases.put("TIU","UVT");
		universityAliases.put("OUN","OU");
		universityAliases.put("VUA","VU");

		universities = new HashSet<String>();

		universitiesFullNames = new HashMap<String,String>();
		universitiesFullNames.put("CWI","Centrum voor Wiskunde en Informatica, Amsterdam");
		universitiesFullNames.put("EUR","Erasmus Universiteit, Rotterdam");
		universitiesFullNames.put("OU","Open Universiteit");
		universitiesFullNames.put("RUN","Radboud Universiteit Nij\\-me\\-gen");
		universitiesFullNames.put("TUD","Technische Universiteit Delft");
		universitiesFullNames.put("TUE","Technische Universiteit Eindhoven");
		universitiesFullNames.put("UL","Universiteit Leiden");
		universitiesFullNames.put("UM","Universiteit Maastricht");
		universitiesFullNames.put("UT","Universiteit Twente");
		universitiesFullNames.put("UU","Universiteit Utrecht");
		universitiesFullNames.put("UVA","Universiteit van Amsterdam");
		universitiesFullNames.put("UVT","Universiteit van Tilburg");
		universitiesFullNames.put("VU","Vrije Universiteit, Amsterdam");
		//universitiesFullNames.put("CWI","");
	}


	/**
	 * This class gets the file with the SIKS dissertation series as published on the SIKS website and formats it
	 * according to the
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length != 1){
			System.out.println("Give as input the path of the file containing the SIKS dissertation series to be formatted!");
			return;
		}

		String theFilePath = args[0];

		File theFile = new File(theFilePath);

		if(!(theFile.exists() && theFile.isFile())){
			System.out.println("Couldn't find specified file containing the SIKS dissertation series to be formatted.");
			return;
		}

		String theOutputFilePath = theFile.getParentFile().getPath() + "/Formatted" +  theFile.getName();

		BufferedReader br = null;
		String theLine;
		String[] splitLine;

		int year;
		boolean firstYear = true;

		try {
			br = new BufferedReader(new FileReader(theFile));

			// Read header
			theLine = br.readLine();

			while(theLine != null){

				splitLine = splitLine(theLine);

				if(splitLine.length == 3) {
					// Check if the first item is a year and we have to open the sikslist environment.
					if(!splitLine[0].equals("")) {

						//System.out.println(splitLine[0] + "ciao");

						try {
							year = Integer.parseInt(splitLine[0]);
							if(year >= 1996 && year <= 9999) {
								if(!firstYear) {
									// Close the siklist environment
									StatsUtils.writeToFileMkParentDir(theOutputFilePath, "\n\\end{sikslist}\n\n");
								}else {
									firstYear = false;
								}
								StatsUtils.writeToFileMkParentDir(theOutputFilePath, "\\begin{sikslist}{" + year + "}\n");
							}else {
								System.out.println("Unexpected year number in line: \"" + theLine + "\". Ignoring line!");
							}
						}catch(NumberFormatException nfe) {
							System.out.println("Expected year in line: \"" + theLine + "\". Ignoring line!");
						}
					}

					StatsUtils.writeToFileMkParentDir(theOutputFilePath, parseThesisData(splitLine[2]));

				}else {
					System.out.println("Ignoring useless line: \"" + theLine + "\"");
				}

				theLine = br.readLine();
			}

			// Close the last siklist environment
			StatsUtils.writeToFileMkParentDir(theOutputFilePath, "\n\\end{sikslist}\n\n");

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the .csv file " + theFilePath + ".");
        	e.printStackTrace();
        	if(br != null){
	        	try {
					br.close();
				} catch (IOException ioe) {
					System.out.println("Exception when closing the .csv file " + theFilePath + ".");
					ioe.printStackTrace();
				}
        	}
		}


		// Add to the file all the universities acronyms with their full name. Note that this should go as
		// a footnote of the first occurrence of a university acronym, but this has to be moved manually
		// because now it is just put at the end of the file.
		// Note that only universities encountered when parsing the original file are added, and
		// universities for which the full name is not specified in the fullName map are added as
		// well with blank full name that has to be manually entered.
		// Also note that this code does not detect when a person has multiple affiliations, there-
		// fore when the university is more than one it will be detected as a signle different university.
		// This also has to be fixed manually.
		List<String> acronyms = new ArrayList<String>(universities);
		Collections.sort(acronyms);
		String abbreviations = "\\footnote{Abbreviations: SIKS -- Dutch Research School for Information and Knowledge Systems";
		String fullName;
		for(String acronym : acronyms) {

			fullName = universitiesFullNames.get(acronym);
			if(fullName == null) {
				fullName = "???";
			}

			abbreviations += ("; " + acronym + " -- " + fullName);
		}
		abbreviations += ".}";

		StatsUtils.writeToFileMkParentDir(theOutputFilePath, "\n\n\n" + abbreviations);

	}

	/**
	 * This method splits the line using "&" as separator. Note that it does not check
	 * whether it is the right format for the line and simply uses all the occurrences
	 * of "&" that are not preceded by "\".
	 *
	 * @return
	 */
	public static String[] splitLine(String line) {

		// Find indices of all occurrences of "&" that are not preceeded by "\"
		List<Integer> indices = new ArrayList<Integer>();

		int index = line.indexOf("&", 0);

		while(index != -1) {

			if(index == 0 || line.charAt(index-1) != '\\') {
				indices.add(new Integer(index));
			}

			index = line.indexOf("&", index+1);

		}
		indices.add(new Integer(line.length()));

		String[] splitLine = new String[indices.size()]; // Minimum size is 1
		index = 0;
		for(int i = 0; i < splitLine.length; i++) {
			splitLine[i] = line.substring(index, indices.get(i)).replace("\t", " ").trim();
			index = indices.get(i) + 1;
		}

		return splitLine;
	}


	/**
	 * Given a line like the following:
	 *
	 * 		Mathijs de Weerdt (TUD), Plan Merging in Multi-Agent Systems
	 *
	 * formats it as a \siksitem as follows
	 *
	 * 		\siksitem{Mathijs de Weerdt} {TUD} {Plan Merging in Multi-Agent Systems}
	 *
	 * Note that if the line is weirdly formatted or is missing any of the entries among author, university or title,
	 * all the content of the line is considered as being "university". Note that this decision is because there can
	 * be entries of withdrawn students, for which the entire content of the line consists of "withdrawn" and should
	 * be formatted between brackets like the "university". Note that whenever a line with different formatting is
	 * detected a warning is printed on screen.
	 *
	 * @param data
	 * @return
	 */
	public static String parseThesisData(String data) {

		String author = null;
		String university = null;
		String title = null;

		boolean unusualFormat = false;

		int startIndex;
		int endIndex;

		// Extract author
		endIndex = data.indexOf("(", 0);

		if(endIndex > -1) {
			author = data.substring(0, endIndex).trim();

			// Extract university
			startIndex = endIndex + 1;
			endIndex = data.indexOf(")", startIndex);

			if(endIndex > -1) {
				university = data.substring(startIndex, endIndex).trim().toUpperCase();
				// Check if university is formatted wrongly and has to be substituted by the correct alias.
				if(universityAliases.containsKey(university)) {
					university = universityAliases.get(university);
				}

				universities.add(university);

				// Extract title
				// Remove comma
				if(data.charAt(endIndex+1) == ',') {
					endIndex++;
				}
				startIndex = endIndex + 1;
				endIndex = data.indexOf("\\\\", startIndex);

				if(endIndex > -1) {
					title = data.substring(startIndex, endIndex).trim();
				}else {
					unusualFormat = true;
				}
			}else {
				unusualFormat = true;
			}
		}else {
			unusualFormat = true;
		}

		if(unusualFormat) {

			System.out.println("Warning! Line with unusual format: \"" + data + "\"");

			// Extract whole line as university, removing final characters "\\"
			endIndex = data.indexOf("\\\\", 0);

			if(endIndex > -1) {
				university = data.substring(0, endIndex).trim();
			}else {
				university = "";
			}

			author = "";
			title = "";
		}

		return "\\siksitem{" + author + "}{" + university + "}{" + title + "}";

	}

}
