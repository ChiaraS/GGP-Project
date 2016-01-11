/**
 *
 */
package csironi.ggp.course.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.ggp.base.util.match.Match;

/**
 * !!!!!!!!!!!!!!!!!!!! ADD A METHOD TO SET MANUALLY THE FILES TO SKIP (i.e. if i want to skip writing on file the
 * logs of the category Error or StateMachine I should be able to do that!)
 *
 *
 *
 * This logger is an adaptation of org.ggp.base.util.logging.GamerLogger as a non-static class.
 * This logger can be instantiated for each of the players that are running at the same time and
 * allows to save separate log files for each of them.
 *
 * Each log message can be associated to a different category of logs. This category is represented by
 * the parameter 'toFile'. Any category can be used. Some examples of categories that can be defined
 * are 'GamePlayer' for logs about network messages received/sent by the game player, 'Logger' for logs
 * about the logging procedure (e.g. when it started/ended) for this player, 'Statistics' for logs about
 * the playing statistics of this player in a certain match (e.g. thinking time, number of iterations of
 * the decision procedure,...) and so on. This logger, for each log, will also record the category in which
 * it is classified.
 * For long run game players, this logger can be used to create different log directories for each match
 * played by the corresponding player, containing separate files for each defined category.
 *
 * Each log message is also associated to a level of importance. This level of importance can be used to
 * decide which logs to show and which to hide because they are not relevant.
 *
 * DETAILS: This class saves logs for the player that instantiates it.
 * There are 3 different main setting for the way this class saves logs:
 * 1. By default it writes all logs on the standard output (System.err for error logs and System.out for
 * all other logs.). It is possible to set the importance level for each log. This setting only prints
 * logs with level higher than or equal to LOG_LEVEL_ORDINARY.
 * 2. If a spill-over file name is set and the logger has not been set to write logs to file (i.e.
 * 'writeLogsToFile == false') then this class will write all logs on this file (also writing the category
 * of the log).
 * 3. If the logger has been set to write logs to file, then for each match it will create a different
 * directory and in this directory create a different log file for each log category. Then it will write each
 * log in the directory of the corresponding match, in the file corresponding to its category.
 * (NOTE: in this case, if the logged message is an Error log (i.e. the logError method has been called), the
 * message is written both in the corresponding file category and in the file with "Error" category => never
 * use "Error" as a custom category name to avoid messy logs in the corresponding file!
 * Moreover, when an Exception/Error object is logged with the method logStackTrace, the effect is the same as
 * using the logError method, where the message is the stack trace of the Exception/Error object). *
 *
 * NOTE 1: with configuration 2. and 3., it is also possible to state which categories of logs we want to
 * display on the standard output/standard error besides writing them on a file. Whenever a message is logged
 * with configuration 2. or 3., if its category is set to be displayed and its importance level is higher than
 * the 'minLevelToDisplay', then the log will also be printed on standard output/standard error.
 *
 * NOTE 2: this class, when writing logs on a file (either the spill-over file or a file corresponding to a log
 * category), from time to time checks if the file is becoming too long, and if so it stops writing on that file
 * and closes it (the file name is memorized in a list of files to skip when writing logs).
 *
 * @author C.Sironi
 */
public class Logger {

	/******************************************** PARAMETERS ********************************************/

	//////////////////////////////////////////// STATIC FIELDS ///////////////////////////////////////////

	/**
	 * Level of importance of the message: DUMP
	 */
    public static final int LOG_LEVEL_DATA_DUMP = 0;

    /**
     * Level of importance of the message: ORDINARY
     */
    public static final int LOG_LEVEL_ORDINARY = 3;

    /**
     * Level of importance of the message: IMPORTANT
     */
    public static final int LOG_LEVEL_IMPORTANT = 6;

    /**
     * Level of importance of the message: CRITICAL
     */
    public static final int LOG_LEVEL_CRITICAL = 9;

    /**
     * Random number generator.
     */
    private static final Random theRandom = new Random();

    /**
     * Maximum size that a log file can reach before being closed and not written on anymore.
     */
    private static final long maximumLogfileSize = 25 * 1024 * 1024;


    //////////////////////////////////////// NON-STATIC FIELDS ////////////////////////////////////////

    /**
     * Name of the directory where this logger must save log files.
     * This directory name changes for every match.
     */
    private String myDirectory;

    /**
     * List of log categories that we want also to be displayed on standard output/standard error when logging
     * them on a file.
     */
    private HashSet<String> filesToDisplay = new HashSet<String>();

    /**
     * Minimum importance level that a log message must have to be also written on standard output/standard error
     * whilst being written on a log file.
     */
    private int minLevelToDisplay = Integer.MAX_VALUE;

    /**
     * List of the names of the files that are too long and must not be written anymore with logs.
     */
    private final Set<String> filesToSkip = new HashSet<String>();

    /**
     * Name of a general log file on which to save the logs for this player when we are not saving on specific
     * files and we don't have a specific directory for the current match (i.e. the parameter 'writeLogsToFile'
     * is false).
     * This name should be specified if we want the logs for this player to be saved on a generic file (that might
     * contain also other logs) and not in specific files in different directories for each match.
     */
    private String spilloverLogfile;

    /**
     * True if the logger must write logs for the current specific match on specific files in the specific
     * directory that corresponds to this match, false otherwise.
     */
    private boolean writeLogsToFile = false;

    /**
     * True if ANY output of this logger should be discarded (not written on any file or on the console).
     */
    private boolean suppressLoggerOutput;



	/**
	 *
	 */
	public Logger() {
		// TODO Auto-generated constructor stub
	}

	/*************************************** METHODS **************************************/

	/**
	 * This method starts file logging for a particular match of the player in the specific directory
	 * corresponding to the match.
	 *
	 * @param m the match for which to save logs.
	 * @param roleName the role of the player in this match (used to create a unique directory for the match).
	 */
	public void startFileLogging(Match m, String roleName) {
        this.writeLogsToFile = true;
        this.myDirectory = "logs/" + m.getMatchId() + "-" + roleName;

        new File(myDirectory).mkdirs();

        log("Logger", "Started logging to files at: " + new Date());
        log("Logger", "Game rules: " + m.getGame().getRules());
        log("Logger", "Start clock: " + m.getStartClock());
        log("Logger", "Play clock: " + m.getPlayClock());
    }

	/**
	 * This method stops file logging for the corresponding player for the current match being logged.
	 */
	public void stopFileLogging() {
        log("Logger", "Stopped logging to files at: " + new Date());
        log("Logger", "LOG SEALED");
        this.writeLogsToFile = false;
    }

	/**
	 * This method sets the spill-over file name.
	 *
	 * @param spilloverFilename the name (can be null) of the file where we want to spill logs over
	 * when no specific file and directory are specified for the current match being logged.
	 */
	public void setSpilloverLogfile(String spilloverFilename) {
    	this.spilloverLogfile = spilloverFilename;
    }

	/**
	 * This method sets the 'suppressLoggerOutput' flag.
	 *
	 * @param bSuppress true if we want to suppress ANY output from this logger, false otherwise.
	 */
	public void setSuppressLoggerOutput(boolean bSuppress) {
        this.suppressLoggerOutput = bSuppress;
    }



	//we can specify different files in the directory of the match on which to save the logs (e.g. if it's statistics we want to log we can put them is a separate log file for this match rather than in the same log file that logs all the received and sent messages for this player during this match)
	public void log(String toFile, String message) {
        log(toFile, message, LOG_LEVEL_ORDINARY);
    }

    public void log(String toFile, String message, int nLevel) {
        logEntry(System.out, toFile, message, nLevel);
    }

    private void logEntry(PrintStream ordinaryOutput, String toFile, String message, int logLevel) {
        if(suppressLoggerOutput)
            return;

        // When we're not writing to a particular directory, and we're not spilling over into
        // a general logfile, write directly to the standard output unless it is really unimportant.
        if(!writeLogsToFile && spilloverLogfile == null) {
            if (logLevel >= LOG_LEVEL_ORDINARY) {
                ordinaryOutput.println("[" + toFile + "] " + message);
            }
            return;
        }

        try {

            // If we are also displaying this file, write it to the standard output.
            if(filesToDisplay.contains(toFile) || logLevel >= minLevelToDisplay) {
                ordinaryOutput.println("[" + toFile + "] " + message);
            }

            // When constructing filename, if we are not writing to a particular directory,
            // go directly to the spillover file if one exists.
            String myFilename = myDirectory + "/" + toFile;
            if(!writeLogsToFile && spilloverLogfile != null) {
            	myFilename = spilloverLogfile;
            	// Since we will not write the log in the file corresponding to its category, add the category
            	// to the message.
            	message = "[" + toFile + "]" + message;
            }

            String logMessage = logFormat(logLevel, ordinaryOutput == System.err, message);

            // Periodically check to make sure we're not writing TOO MUCH to this file.
            if(filesToSkip.size() != 0 && filesToSkip.contains(myFilename)) {
                return;
            }
            if(theRandom.nextInt(1000) == 0) {
                // Verify that the file is not too large.
                if(new File(myFilename).length() > maximumLogfileSize) {
                    System.err.println("Adding " + myFilename + " to filesToSkip.");
                    filesToSkip.add(myFilename);
                    logLevel = 9;
                    logMessage = logFormat(logLevel, ordinaryOutput == System.err, "File too long; stopping all writes to this file.");
                }
            }

            // Finally, write the log message to the file.
            BufferedWriter out = new BufferedWriter(new FileWriter(myFilename, true));
            out.write(logMessage);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }











    // Public Interface
    public void emitToConsole(String s) {
        // TODO: fix this hack!
        if(!writeLogsToFile && !suppressLoggerOutput) {
            System.out.print(s);
        }
    }







    public void setFileToDisplay(String toFile) {
        filesToDisplay.add(toFile);
    }

    public void setMinimumLevelToDisplay(int nLevel) {
        minLevelToDisplay = nLevel;
    }





    public void logError(String toFile, String message) {
        logEntry(System.err, toFile, message, LOG_LEVEL_CRITICAL);
        if(writeLogsToFile) {
            logEntry(System.err, "Errors", "(in " + toFile + ") " + message, LOG_LEVEL_CRITICAL);
        }
    }



    public void logStackTrace(String toFile, Exception ex) {
        StringWriter s = new StringWriter();
        ex.printStackTrace(new PrintWriter(s));
        logError(toFile, s.toString());
    }

    public void logStackTrace(String toFile, Error ex) {
        StringWriter s = new StringWriter();
        ex.printStackTrace(new PrintWriter(s));
        logError(toFile, s.toString());
    }





    private static String logFormat(int logLevel, boolean isError, String message) {
        String logMessage = "LOG " + System.currentTimeMillis() + " [L" + logLevel + "]: " + (isError ? "<ERR> " : "") + message;
        if(logMessage.charAt(logMessage.length() - 1) != '\n') {
            logMessage += '\n';     // All log lines must end with a newline.
        }
        return logMessage;
    }



}
