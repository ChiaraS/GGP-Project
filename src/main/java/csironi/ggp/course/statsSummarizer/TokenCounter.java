package csironi.ggp.course.statsSummarizer;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.util.game.CloudGameRepository;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.logging.GamerLogger;

public class TokenCounter {

	/**
	 * Counts the token in the GDL game descriptions by adding a space in place of each parenthesis
	 * and then splitting the description in tokens using the spaces as separators.
	 * Parenthesis are not part of the tokens.
	 *
	 * Inputs this program gets:
	 *
	 * 		[mainLogFolder] = the outer folder containing all the logs and files produced by this program and its subprograms.
	 * 		[gameKey] = the keys of the games to test separated by ";" (use the string "ALL" to test all games in the repository at once).
	 *  	[repositoryLocation] = URL of the remote repository
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length != 3) {
			System.out.println("Specify main log folder, game key and repository location");
			return;
		}

		String mainLogFolder = args[0];
    	String gameKey = args[1];
    	String repositoryLocation = args[2];

    	ThreadContext.put("LOG_FOLDER", mainLogFolder);

    	GamerLogger.startFileLogging();

    	GameRepository gameRepo = new CloudGameRepository(repositoryLocation);

	    GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Tokens", "Game;Tokens;");

    	if(gameKey.equals("ALL")){

    	    for(String aGameKey : gameRepo.getGameKeys()) {
    	        if(aGameKey.contains("laikLee")) continue;

    	        Game game = gameRepo.getGame(aGameKey);

    	        String preprocessed = preprocess(game.getRulesheet());

    	        //System.out.println(preprocessed);

    	        List<String> tokens = lex(preprocessed);

    	        GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Tokens", aGameKey + ";" + tokens.size() + ";");

    	    }

    	}else{

    		String[] gameKeysArray = gameKey.split(";");

    		if(gameKeysArray.length == 0) {
    			System.out.println("No valid game keys specified. Stopping token counter.");
				return;
    		}

    		for(String aGameKey : gameKeysArray) {

    			Game game = gameRepo.getGame(aGameKey);

    			if(game == null) {
    				System.out.println("Could not find game " + aGameKey + ". Continuing counting tokens for other games.");
    				continue;
    			}

    			//System.out.println(game.getRulesheet());

    			String preprocessed = preprocess(game.getRulesheet());

    			//System.out.println(preprocessed);

    	        List<String> tokens = lex(preprocessed);

       	        GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "Tokens", aGameKey + ";" + tokens.size() + ";");

    		}
    	}

	}


	public static String preprocess(String string)
	{
		string = string.replaceAll("\\(", " ");
		string = string.replaceAll("\\)", " ");

		string = string.replaceAll("\\s+", " ");
		string = string.trim();

		return string;
	}

	public static List<String> lex(String string)
	{
		List<String> tokens = new ArrayList<String>();
		for (String token : string.split(" "))
		{
			tokens.add(token);
		}

		return tokens;
	}

}
