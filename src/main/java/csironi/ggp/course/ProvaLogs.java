package csironi.ggp.course;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProvaLogs {

	public static void main(String[] args){
		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
		System.setProperty("isThreadContextMapInheritable", "true");

		Logger ROOT_LOGGER = LogManager.getRootLogger();
		Logger CSV_LOGGER = LogManager.getLogger("CSVLogger");

		ROOT_LOGGER.info("Logging text message in the default file in the default folder.");
		CSV_LOGGER.info("Logging CSV message in the default file in the default folder.");

		/*
		ThreadContext.put("LOG_FOLDER", "SpecificLogFolder");

		ROOT_LOGGER.info("Logging text message in the default file in a specific folder.");
		CSV_LOGGER.info("Logging CSV message in the default file in a specific folder.");


		ThreadContext.put("LOG_FILE", "SpecificLogFile");

		ROOT_LOGGER.info("Logging text message in a specific file in a specific folder.");
		CSV_LOGGER.info("Logging CSV message in a specific file in a specific folder.");

		ThreadContext.remove("LOG_FOLDER");

		ROOT_LOGGER.info("Logging text message in a specific file in the default folder.");
		CSV_LOGGER.info("Logging CSV message in a specific file in the default folder.");
		*/
	}

}
