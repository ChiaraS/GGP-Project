package csironi.ggp.course.LoggerTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

public class L4J2SpeedTest {

	public static void main(String[] args) {

		if(args.length != 2){
			System.out.println("Specify the number of messages to log and the number of log files.");
		}else{
			int numMex = Integer.parseInt(args[0]);
			int numFiles = Integer.parseInt(args[1]);

			System.out.println("Logging " + numMex + " messages on " + numFiles + " files.");

			System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
			System.setProperty("isThreadContextMapInheritable", "true");

			Logger LOGGER = LogManager.getRootLogger();
			//Logger CSV_LOGGER = LogManager.getLogger("CSVLogger");

			long start = System.currentTimeMillis();

			ThreadContext.put("LOG_FOLDER", "L4J2LogsSpeedTest");

			System.out.println("Starting GamerLogger speed test.");

			for(int j = 0; j < numFiles; j++){

				ThreadContext.put("LOG_FILE", "L4J2LogsSpeedTest"+j);

				for(int i = 0; i < numMex; i++){
					LOGGER.info("Message" + i);
				}
			}

			long end = System.currentTimeMillis();

			System.out.println("Total duration: " + (end-start) + "ms.");
		}

	}

}
