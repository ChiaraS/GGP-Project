package csironi.ggp.course.LoggerTest;

/**
 * This class runs a match of a game between a player that uses the GamerLogger and a player that uses Log4J2.
 * Both players will be given a copy of the same propnet to keep the comparison fair.
 *
 * @author C.Sironi
 *
 */
public class LoggersComparison {

	static{

		System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
		System.setProperty("isThreadContextMapInheritable", "true");

	}

	public LoggersComparison() {
		// TODO Auto-generated constructor stub
	}

}
