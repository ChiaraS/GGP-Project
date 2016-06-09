/**
 *
 */
package org.ggp.base.util.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author C.Sironi
 *
 */
public final class ManualUpdateLocalGameRepository extends GameRepository {

	/**
	 * The local directory where the games are stored.
	 */
	private final File theGamesDirectory;

	/**
	 * Creates the class that accesses the specified local games directory, if it exists.
	 * If it doesn't exist it will behave as if this game repository was empty.
	 *
	 * @param gamesDirectoryPath the path of the directory where all the games are stored.
	 *
	 */
	public ManualUpdateLocalGameRepository(String gamesDirectoryPath){

		System.out.println(gamesDirectoryPath);

		if(gamesDirectoryPath == null || gamesDirectoryPath == ""){
			this.theGamesDirectory = null;

			System.out.println("null1");

			return;
		}

		File theDirectory = new File(gamesDirectoryPath);

		// If the directory already exists use the games in it (if any).
		// Otherwise, set it null.
		if (!(theDirectory.exists())) {
			this.theGamesDirectory = null;
			System.out.println("null2");
        }else{
        	this.theGamesDirectory = theDirectory;
        	System.out.println(this.theGamesDirectory.getPath());
        }

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.game.GameRepository#getUncachedGame(java.lang.String)
	 */
	@Override
	protected Game getUncachedGame(String theKey) {

		Game cachedGame = loadGameFromCache(theKey);
        if (cachedGame != null) {
        	return cachedGame;
        }
        return null;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.game.GameRepository#getUncachedGameKeys()
	 */
	@Override
	protected Set<String> getUncachedGameKeys() {
        Set<String> theKeys = new HashSet<String>();
        for(File game : this.theGamesDirectory.listFiles()) {
            theKeys.add(game.getName().replace(".zip", ""));
        }
        return theKeys;
	}

    private synchronized Game loadGameFromCache(String theKey) {
        File theGameFile = new File(this.theGamesDirectory, theKey + ".zip");
        String theLine = null;
        try {
            FileInputStream fIn = new FileInputStream(theGameFile);
            GZIPInputStream gIn = new GZIPInputStream(fIn);
            InputStreamReader ir = new InputStreamReader(gIn);
            BufferedReader br = new BufferedReader(ir);
            theLine = br.readLine();
            br.close();
            ir.close();
            gIn.close();
            fIn.close();
        } catch (Exception e) {
            ;
        }

        if (theLine == null) return null;
        return Game.loadFromJSON(theLine);
    }

}
