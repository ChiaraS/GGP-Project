package org.ggp.base.util.game;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.ggp.base.util.files.FileUtils;

public class LocalFolderGameRepository extends GameRepository {

	/**
	 * The local directory where the games are stored.
	 */
	private final File theLocalFolder;

	public LocalFolderGameRepository(String theLocalFolderPath) {

		System.out.println(theLocalFolderPath);

		if(theLocalFolderPath == null || theLocalFolderPath == ""){
			this.theLocalFolder = null;

			System.out.println("null1");

			return;
		}

		File theDirectory = new File(theLocalFolderPath);

		// If the directory already exists use the games in it (if any).
		// Otherwise, set it null.
		if (!(theDirectory.exists())) {
			this.theLocalFolder = null;
			System.out.println("null2");
        }else{
        	this.theLocalFolder = theDirectory;
        	System.out.println(this.theLocalFolder.getPath());
        }
	}

	@Override
	protected Game getUncachedGame(String theKey) {
		Game cachedGame = loadGameFromCache(theKey);
        if (cachedGame != null) {
        	return cachedGame;
        }
        return null;
	}

	@Override
	protected Set<String> getUncachedGameKeys() {
        Set<String> theKeys = new HashSet<String>();
        for(File game : this.theLocalFolder.listFiles()) {
            theKeys.add(game.getName().replace(".txt", ""));
        }
        return theKeys;
	}

    private synchronized Game loadGameFromCache(String theKey) {
    	/*
        File theGameFile = new File(this.theLocalFolder, theKey + ".txt");
        String theLine = null;
        String gameDescription = "";
        try {

        	FileReader fr = new FileReader(theGameFile);
            BufferedReader br = new BufferedReader(fr);

            theLine = br.readLine();

            while(theLine != null){
            	gameDescription += (theLine + " ");
            	theLine = br.readLine();
            }

            fr.close();
            br.close();

        } catch (Exception e) {
            gameDescription = null;
        }

        if (gameDescription == null) return null;

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println(gameDescription);
        System.out.println();
        System.out.println();
        System.out.println();
        */

        return Game.createEphemeralGame(Game.preprocessRulesheet(FileUtils.readFileAsString(new File(this.theLocalFolder, theKey + ".txt"))));
    }

}
