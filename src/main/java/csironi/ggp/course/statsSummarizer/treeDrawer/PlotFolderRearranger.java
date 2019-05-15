package csironi.ggp.course.statsSummarizer.treeDrawer;

import java.io.File;

public class PlotFolderRearranger {

	public static void main(String args[]) {

		if(args.length != 1) {
			System.out.println("Specify the path of folder where the logs have to be re-aranged.");
			return;
		}

		File folder = new File(args[0]);

		if(!(folder.exists()) || !(folder.isDirectory())) {
			System.out.println("The specified forlder does not exist!");
			return;
		}

		File[] gameFolders = folder.listFiles();
		File[] roleFolders;
		File[] playerFolders;
		File[] matchFolders;
		File[] matchLogsAndFigures;

		for(File gameFolder : gameFolders) {
			if(gameFolder.isDirectory()) {
				roleFolders = gameFolder.listFiles();
				for(File roleFolder : roleFolders) {
					if(roleFolder.isDirectory()) {
						playerFolders = roleFolder.listFiles();
						for(File playerFolder : playerFolders) {
							if(playerFolder.isDirectory()) {
								matchFolders = playerFolder.listFiles();
								for(File matchFolder : matchFolders) {
									if(isMatchFolder(matchFolder)) {
										matchLogsAndFigures = matchFolder.listFiles();
										for(File matchLogOrFigure : matchLogsAndFigures) {
											moveUpByOneFolder(matchLogOrFigure);
										}
										matchFolder.deleteOnExit();
									}
								}
							}
						}
					}
				}
			}

		}
	}

	public static boolean isMatchFolder(File matchFolder) {
		try {
			Integer.parseInt(matchFolder.getName());
		}catch(NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static void moveUpByOneFolder(File matchLogOrFigure) {
		String destFolderName = matchLogOrFigure.getParentFile().getParent();
		matchLogOrFigure.renameTo(new File(destFolderName + "/" + matchLogOrFigure.getName()));
	}
}
