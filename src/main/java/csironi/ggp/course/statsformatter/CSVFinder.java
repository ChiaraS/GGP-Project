package csironi.ggp.course.statsformatter;

import java.io.File;
import java.util.HashMap;

public class CSVFinder {

	public static void main(String args[]){

		String tourneyFolder = args[0];

		String playerType = args[1];

		File mainDirectory = new File(tourneyFolder);

		HashMap<String, Integer> matchNumbers = new HashMap<String, Integer>();

		if(mainDirectory.isDirectory()){
			//System.out.println("Is directory");
			File[] children = mainDirectory.listFiles();
			//System.out.println("Num of children: " + children.length);
			for(int i=0; i < children.length; i++){
				if(children[i].isDirectory()){
					//System.out.println("Children is directory");
					String fileName = children[i].getName();
					//System.out.println("Name: " + fileName);
					String[] tokens = fileName.split("\\.");

					//System.out.print("[");
					//for(int k = 0; k < tokens.length; k++){
					//	System.out.print(" " + tokens[k]);
					//}
					//System.out.println(" ]");

					if(tokens.length >= 3){
						String folder = tokens[1];
						String personalFileNameOut = tokens[1] + "." + tokens[2] + ".csv";
						File[] subChildren = children[i].listFiles();
						for(int j = 0; j < subChildren.length; j++){
							if(subChildren[j].isFile() && (subChildren[j].getName().equals("Stats.log")||subChildren[j].getName().equals("Stats"))){
								//String personalPathNameOut = outDirectoryPath + "\\" + folder + "\\" + personalFileNameOut;
								//String globalPathNameOut = outDirectoryPath + "\\" + folder + "\\" + "Global_stats.csv";
								int matchNumber = 1;
								if(matchNumbers.containsKey(folder)){
									matchNumber = matchNumbers.get(folder);
								}
								//formatCSV(subChildren[j].getAbsolutePath(), personalPathNameOut, globalPathNameOut, matchNumber);
								matchNumber++;
								matchNumbers.put(folder, matchNumber);
							}
						}
					}


				}
			}
		}


























	}

}
