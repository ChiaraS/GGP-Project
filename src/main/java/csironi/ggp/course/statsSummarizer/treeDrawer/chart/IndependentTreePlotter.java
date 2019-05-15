package csironi.ggp.course.statsSummarizer.treeDrawer.chart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IndependentTreePlotter {

	public static void main(String args[]) {

		if(args.length != 3) {
			System.out.println("Expecting 3 arguments: (i) the folder that contains the compressed tree plot logs without duplicate edges for each game, role, agent and match, (ii) the number of plots that can be generated in parallel, and (iii) the color scale type (COLOR|EXTENDED_COLOR|GRAY_SMALL|GRAY_BIG|REPEATED_COLOR|REPEATED_DARK_COLOR).");
			return;
		}

		String sourceFolderPath = args[0];
		File sourceFolder = new File(sourceFolderPath);
		if(!(sourceFolder.exists() && sourceFolder.isDirectory())) {
			System.out.println("Impossible to plot the trees! Cannot find the given folder " + sourceFolderPath + "!");
			return;
		}
		int numParallelPlots;
		try{
			numParallelPlots = Integer.parseInt(args[1]);
		}catch(NumberFormatException nfe) {
			System.out.println("Impossible to plot the trees! Wrong format for the number of plots that can be generated in parallel: " + args[1] + "!");
			nfe.printStackTrace();
			return;
		}
		String colorScaleType = args[2];
		if(!(args[2].equals("COLOR") || args[2].equals("EXTENDED_COLOR") || args[2].equals("GRAY_SMALL") || args[2].equals("GRAY_BIG") || args[2].equals("REPEATED_COLOR") || args[2].equals("REPEATED_DARK_COLOR"))) {
			System.out.println("Impossible to plot the trees! Unrecognized color scale type " + args[2] + "! Accepted types are (COLOR|EXTENDED_COLOR|GRAY_SMALL|GRAY_BIG|REPEATED_COLOR|REPEATED_DARK_COLOR).");
			return;
		}

		// Create the executor as a pool with the desired number of threads
		// (corresponding to the number of trees we want to plot in parallel).
		ExecutorService executor = Executors.newFixedThreadPool(numParallelPlots);

		// Create the settings for the process
		List<String> theSettings = new ArrayList<String>();

		theSettings.add("java");
		//theSettings.add("/usr/java/jdk1.8.0_131/bin/java"); // To use old java version on go4nature
		//theSettings.add("-Xmx:25g");
		theSettings.add("-jar");
		theSettings.add("JFCTree.jar");
		theSettings.add(""); // Path of the .csv file with the compressed log of the coordinates without duplicates
		theSettings.add(colorScaleType);

		File[] gamesDirs = sourceFolder.listFiles();

		File[] roleDirs;

		File[] playerDirs;

		File[] opponentDirs;

		File[] treePlotFiles;

		for(int i = 0; i < gamesDirs.length; i++){

			if(gamesDirs[i].isDirectory()){

				roleDirs = gamesDirs[i].listFiles();

				for(int j = 0; j < roleDirs.length; j++){

					if(roleDirs[j].isDirectory()){

						playerDirs = roleDirs[j].listFiles();

						for(int k = 0; k < playerDirs.length; k++){

							if(playerDirs[k].isDirectory()){

								opponentDirs = playerDirs[k].listFiles();

								for(int l = 0; l < opponentDirs.length; l++){

									if(opponentDirs[l].isDirectory()){

										treePlotFiles = opponentDirs[l].listFiles();

										for(int m = 0; m < treePlotFiles.length; m++){

											if(treePlotFiles[m].isFile()) {
												theSettings.set(3, treePlotFiles[m].getPath());
												executor.execute(new TreePlotRunner(new ArrayList<String>(theSettings), treePlotFiles[m].getParent(), getNameWithoutExtension(treePlotFiles[m].getName())));
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

		// Shutdown executor to tell it not to accept any more task to execute.
		// Note that this doesn't interrupt previously started tasks nor avoids executing previously submitted tasks.
		executor.shutdown();

		/*
		while(!(executor.isTerminated())){
			System.out.println("Threads ALL: " + ManagementFactory.getThreadMXBean().getThreadCount());
			System.out.println("Threads ACTIVE: " + Thread.activeCount());
			try {
				executor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/

		// Tell the executor to wait until all currently running tasks have completed execution.
		try {

			executor.awaitTermination(10, TimeUnit.DAYS);

		} catch (InterruptedException e) {
			executor.shutdownNow(); // Interrupt everything
			System.out.println("Tree plotting interrupted before completion!");
			Thread.currentThread().interrupt();
			return;
		}

		if(!executor.isTerminated()){
			System.out.println("Tree plotting is taking too long. Interrupting it.");
			executor.shutdownNow(); // This instruction interrupts all threads.
			return;
		}else{
			System.out.println("Tree plotting completed.");
		}
	}

	public static String getNameWithoutExtension(String filename) {

		String[] splitFileName = filename.split("\\.");

		String filenameWithoutExtension = "";
		for(int i = 0; i < splitFileName.length-1; i++) {
			filenameWithoutExtension += splitFileName[i];
		}

		return filenameWithoutExtension;
	}

}
