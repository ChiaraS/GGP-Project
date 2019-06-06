package csironi.ggp.course.statsSummarizer.treeDrawer.chart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IndependentTreePlotter {

	public static void main(String args[]) {

		if(args.length != 3 && args.length != 7) {
			System.out.println("Expecting 3 compulsory arguments and 4 optional: (i) the folder that contains the compressed tree plot logs (without duplicate edges) for each game, role, agent and match, (ii) the number of plots that can be generated in parallel, and (iii) the color scale type (COLOR|EXTENDED_COLOR|GRAY_SMALL|GRAY_BIG|REPEATED_COLOR|REPEATED_DARK_COLOR). Optionally, the extreme values for the x and y coordinates of the plot can be specified.");
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

		if(args.length == 7) {
			// TODO add here check for sorrect format of coordinates extremes
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

		if(args.length == 7) {
			theSettings.add(args[3]);
			theSettings.add(args[4]);
			theSettings.add(args[5]);
			theSettings.add(args[6]);
		}


		File[] gamesDirs = sourceFolder.listFiles();

		File[] statsDirs;

		File[] playerDirs;

		File[] roleDirs;

		File[] comboDirs;

		File[] treePlotFiles;

		for(int i = 0; i < gamesDirs.length; i++){

			if(gamesDirs[i].isDirectory()){

				statsDirs = gamesDirs[i].listFiles();

				for(int j = 0; j < statsDirs.length; j++){

					if(statsDirs[j].isDirectory() && statsDirs[j].getName().equals("TreePlotLogs")){

						playerDirs = statsDirs[j].listFiles();

						for(int k = 0; k < playerDirs.length; k++){

							if(playerDirs[k].isDirectory()){

								roleDirs = playerDirs[k].listFiles();

								for(int l = 0; l < roleDirs.length; l++){

									if(roleDirs[l].isDirectory()){

										comboDirs = roleDirs[l].listFiles();

										for(int m = 0; m < comboDirs.length; m++){

											if(comboDirs[m].isDirectory()){

												treePlotFiles = comboDirs[m].listFiles();

												for(int n = 0; n < treePlotFiles.length; n++){
													if(treePlotFiles[n].isFile() && treePlotFiles[n].getName().startsWith("C-")) {
														theSettings.set(3, treePlotFiles[n].getPath());
														executor.execute(new TreePlotRunner(new ArrayList<String>(theSettings), treePlotFiles[n].getParent(), getNameWithoutExtension(treePlotFiles[n].getName())));
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
