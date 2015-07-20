/**
 *
 */
package csironi.ggp.course.utils;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * @author C.Sironi
 *
 */
public class CSVFormatter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int matchNumber = 1;
		String globalFileNameOut = "C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\Formatted_stats\\TicTacToe\\Global_stats.csv";
		String globalHeader = "Match number;Step;Visited nodes;Iterations;Move selection time;Move processing time;\n";
		FileWriter globalWriter = null;

		try{
			globalWriter = new FileWriter(globalFileNameOut, true);
			globalWriter.append(globalHeader);
			globalWriter.close();
		}catch(Exception e){
			System.out.println("Something went wrong! No idea what to do!");
			e.printStackTrace();
		}

		String fileNameIn = "C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\SERVERL4J2logs\\1437144616085PlayerRunner\\1437144617030.SampleMonteCarloGamer.9148\\ticTacToeResults.ticTacToe.1437144862364\\Stats.log";
		String personalFileNameOut = "C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\Formatted_stats\\TicTacToe\\ticTacToe.1437144862364.csv";

		CSVFormatter.formatCSV(fileNameIn, personalFileNameOut, globalFileNameOut, matchNumber);

	}

	public static void formatCSV(String fileNameIn, String personalFileNameOut, String globalFileNameOut, int matchNumber){

		Scanner sc = new Scanner(System.in);
		FileWriter personalWriter = null;
		FileWriter globalWriter = null;

		try{
			sc = new Scanner(new File(fileNameIn));

			personalWriter = new FileWriter(personalFileNameOut);
			globalWriter = new FileWriter(globalFileNameOut, true);

			personalWriter.append("Step;Visited nodes;Iterations;Move selection time;Move processing time;\n");

			String record = "";
			int step = 0;

			while(sc.hasNext()){
				String currentToken = sc.next();
				if(currentToken.equals("VISITED_NODES")){
					// =
					String next = sc.next();
					// Value
					record = sc.next();
				}else if(currentToken.equals("ITERATIONS")){
					String next = sc.next();
					record += ";" + sc.next();
				}else if(currentToken.equals("MOVE_SELECTION_TIME")){
					String next = sc.next();
					record += ";" + sc.next();
				}else if(currentToken.equals("MOVE_PROCESSING_TIME")){
					String next = sc.next();
					record = (++step) + ";" + record + ";" + sc.next() + ";\n";

					//Write on both files
					personalWriter.append(record);
					globalWriter.append("" + matchNumber + ";" + record);
				}
			}

			//Close scanner and writers
			sc.close();
			personalWriter.close();
			globalWriter.close();

		}catch(Exception e){
			sc.close();
		}

	}

}
