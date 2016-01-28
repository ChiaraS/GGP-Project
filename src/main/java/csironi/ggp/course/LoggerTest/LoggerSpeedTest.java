package csironi.ggp.course.LoggerTest;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.match.Match;

/**
 * This class tests the speed of the GamerLogger checking the time
 * that the logger takes to log the given amount of messages on the
 * given number of log files.
 *
 * @author C.Sironi
 *
 */
public class LoggerSpeedTest {

	public static void main(String[] args) {

		if(args.length != 2){
			System.out.println("Specify the number of messages to log and the number of log files.");
		}else{
			int numMex = Integer.parseInt(args[0]);
			int numFiles = Integer.parseInt(args[1]);

			System.out.println("Logging " + numMex + " messages on " + numFiles + " files.");
			String gameKey = "ticTacToe";
			GameRepository theRepository = GameRepository.getDefaultRepository();
			Match fakeMatch = new Match(gameKey + "." + System.currentTimeMillis(), -1, -1, -1,theRepository.getGame(gameKey));

			long start = System.currentTimeMillis();

			GamerLogger.startFileLogging(fakeMatch, "LoggerSpeedTest");

			System.out.println("Starting GamerLogger speed test.");

			for(int j = 0; j < numFiles; j++){
				for(int i = 0; i < numMex; i++){
					GamerLogger.log("LoggerSpeedTest"+j, "Message" + i);
				}
			}

			long end = System.currentTimeMillis();

			System.out.println(end-start);
		}

	}

}
