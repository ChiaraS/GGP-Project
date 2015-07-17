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

	}

	public void formatCSV(String fileNameIn, String personalFileNameOut, String globalFileNameOut){

		Scanner sc = new Scanner(System.in);
		FileWriter personalWriter = null;
		FileWriter globalWriter = null;

		try{
			sc = new Scanner(new File(fileNameIn));

			personalWriter = new FileWriter(personalFileNameOut);
			globalWriter = new FileWriter(globalFileNameOut, true);

			personalWriter.append("Step;Visited nodes;Move selection time;Move processing time");

			String record = "";
			int step = 0;

			while(sc.hasNext()){
				String currentToken = sc.next();
				if(currentToken.equals("VISITED_NODES")){
					// =
					String next = sc.next();
					// Value
					record = sc.next();
				}else if(currentToken.equals("MOVE_SELECTION_TIME")){
					String next = sc.next();
					record += ";" + sc.next();
				}else if(currentToken.equals("MOVE_PROCESSING_TIME")){
					String next = sc.next();
					record += ";" + sc.next() + "\n";

					//scrivi su entrambi i file
					personalWriter.append((++step) + ";" + record);
					globalWriter.append(record);
				}
			}

			sc.close();
		}catch(Exception e){
			sc.close();
		}

	}

}
