package csironi.ggp.course.LoggerTest;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

public class L4J2Experiments {

	public static void main(String[] args) {

		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
		System.setProperty("isThreadContextMapInheritable", "true");

		Logger LOGGER = LogManager.getRootLogger();
		//Logger CSV_LOGGER = LogManager.getLogger("CSVLogger");

		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);

		ctx.
	    final Configuration config = ctx.getConfiguration();
	    Map<String, Appender> appenders = config.getAppenders();
	    System.out.println("Num of appenders: " + appenders.size());
	    System.out.println("Keys of appenders:");
	    for(String s: appenders.keySet()){
	    	System.out.println(s);
	    }

	    appenders.remove("LogRouter");

	    appenders = config.getAppenders();
	    System.out.println("Num of appenders: " + appenders.size());
	    System.out.println("Keys of appenders:");
	    for(String s: appenders.keySet()){
	    	System.out.println(s);
	    }

	    ctx.updateLoggers();

	    appenders = config.getAppenders();
	    System.out.println("Num of appenders: " + appenders.size());
	    System.out.println("Keys of appenders:");
	    for(String s: appenders.keySet()){
	    	System.out.println(s);
	    }

	    LOGGER.info("CIAOAOAOAO");

		//ThreadContext.put("LOG_FOLDER", "L4J2LogsSpeedTest");

		//System.out.println("Starting GamerLogger speed test.");

		//for(int j = 0; j < numFiles; j++){

		//	ThreadContext.put("LOG_FILE", "L4J2LogsSpeedTest"+j);

		//	for(int i = 0; i < numMex; i++){
		//		LOGGER.info("Message" + i);
		//	}
		//}

		//long end = System.currentTimeMillis();

	    //System.out.println("Total duration: " + (end-start) + "ms.");

	}
}

