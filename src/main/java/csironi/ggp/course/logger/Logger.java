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
 * This logger is an adaptation of org.ggp.base.util.logging.GamerLogger as a non-static class.
 * This logger can be instantiated for each of the players that are running at the same time and
 * allows to save separate log files for each of them.
 * For long run game players, this logger can be used to create different log directories
 * for each match played by the corresponding player.
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

    //////////////////////////////////////// NON-STATIC FIELDS ////////////////////////////////////////

    /**
     * Name of the directory where this logger must save log files.
     * This directory name changes for every match.
     */
    private String myDirectory;


    private static HashSet<String> filesToDisplay = new HashSet<String>();
    private static int minLevelToDisplay = Integer.MAX_VALUE;

    /**
     * True if ANY output of this logger should be discarded (not written on any file or on the console).
     */
    private boolean suppressLoggerOutput;

    /**
     * True if the logger must write logs for the current specific match on specific files in the specific
     * directory that corresponds to this match, false otherwise.
     */
    private boolean writeLogsToFile = false;

    /**
     * Name of a general log file on which to save the logs for this player when we are not saving on specific
     * files and we don't have a specific directory for the current match (i.e. the parameter 'writeLogsToFile'
     * is false).
     * This name should be specified if we want the logs for this player to be saved on a generic file (that might
     * contain also other logs) and not in specific files in different directories for each match.
     */
    private String spilloverLogfile;



    private static final Set<String> filesToSkip = new HashSet<String>();
    private static final long maximumLogfileSize = 25 * 1024 * 1024;

	/**
	 *
	 */
	public Logger() {
		// TODO Auto-generated constructor stub
	}

	/*************************************** METHODS **************************************/

	/**
	 * This method starts file logging for a particular match of the player.
	 *
	 * @param m the match for which to save logs.
	 * @param roleName the role of the player in this match.
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
	 * This method stops file logging for the corresponding player for the current match.
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
            String logMessage = logFormat(logLevel, ordinaryOutput == System.err, message);

            // If we are also displaying this file, write it to the standard output.
            if(filesToDisplay.contains(toFile) || logLevel >= minLevelToDisplay) {
                ordinaryOutput.println("[" + toFile + "] " + message);
            }

            // When constructing filename, if we are not writing to a particular directory,
            // go directly to the spillover file if one exists.
            String myFilename = myDirectory + "/" + toFile;
            if(!writeLogsToFile && spilloverLogfile != null) {
            	myFilename = spilloverLogfile;
            }

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
    public static void emitToConsole(String s) {
        // TODO: fix this hack!
        if(!writeLogsToFile && !suppressLoggerOutput) {
            System.out.print(s);
        }
    }







    public static void setFileToDisplay(String toFile) {
        filesToDisplay.add(toFile);
    }

    public static void setMinimumLevelToDisplay(int nLevel) {
        minLevelToDisplay = nLevel;
    }





    public static void logError(String toFile, String message) {
        logEntry(System.err, toFile, message, LOG_LEVEL_CRITICAL);
        if(writeLogsToFile) {
            logEntry(System.err, "Errors", "(in " + toFile + ") " + message, LOG_LEVEL_CRITICAL);
        }
    }



    public static void logStackTrace(String toFile, Exception ex) {
        StringWriter s = new StringWriter();
        ex.printStackTrace(new PrintWriter(s));
        logError(toFile, s.toString());
    }

    public static void logStackTrace(String toFile, Error ex) {
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
