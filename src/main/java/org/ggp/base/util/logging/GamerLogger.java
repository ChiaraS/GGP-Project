package org.ggp.base.util.logging;

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

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.util.match.Match;


/**
 * GamerLogger is a customized logger designed for long-running game players.
 * Logs are written to directories on a per-game basis. Each logfile represents
 * a single logical component of the game playing program, identified whenever
 * the logger is called.
 *
 * TODO: More details about specific use examples.
 *
 * @author Sam Schreiber
 */
public class GamerLogger {
    // Public Interface
    public static void emitToConsole(String s) {
        // TODO: fix this hack!
        if(!writeLogsToFile && !suppressLoggerOutput) {
            System.out.print(s);
        }
    }

    public static void stopFileLogging() {
       // log("Logger", "Stopped logging to files at: " + new Date());
       // log("Logger", "LOG SEALED");
       // writeLogsToFile = false;
    }

    public static void startFileLogging() {
    	if(!writeLogsToFile){
    		writeLogsToFile = true;

    		log("Logger", "Started logging to files at: " + new Date());

    	}
    }

    public static void setSpilloverLogfile(String spilloverFilename) {
    	spilloverLogfile = spilloverFilename;
    }

    public static void startFileLogging(Match m, String roleName) {
        writeLogsToFile = true;
        myDirectory = "logs/" + m.getMatchId() + "-" + roleName;

        new File(myDirectory).mkdirs();

        log("Logger", "Started logging to files at: " + new Date());
        log("Logger", "Game rules: " + m.getGame().getRules());
        log("Logger", "Start clock: " + m.getStartClock());
        log("Logger", "Play clock: " + m.getPlayClock());
    }

    /**
     * Starts file logging only once setting a logging directory.
     * If someone else calls this method before the stopFileLogging method
     * has been called nothing will happen. This method is meant to replace
     * the startFileLoggingMethod soon.
     *
     * @param m
     * @param roleName
     */
    public static void singleStartFileLogging(Match m, String roleName) {
    	if(!writeLogsToFile){
	        writeLogsToFile = true;
	        myDirectory = "logs/" + m.getMatchId() + "-" + roleName;

	        new File(myDirectory).mkdirs();

	        log("Logger", "Started logging to files at: " + new Date());
	        log("Logger", "Game rules: " + m.getGame().getRules());
	        log("Logger", "Start clock: " + m.getStartClock());
	        log("Logger", "Play clock: " + m.getPlayClock());
    	}
    }

    public static void setFileToDisplay(String toFile) {
        filesToDisplay.add(toFile);
    }

    public static void setMinimumLevelToDisplay(int nLevel) {
        minLevelToDisplay = nLevel;
    }

    public static void setSuppressLoggerOutput(boolean bSuppress) {
        suppressLoggerOutput = bSuppress;
    }

    public static final int LOG_LEVEL_DATA_DUMP = 0;
    public static final int LOG_LEVEL_ORDINARY = 3;
    public static final int LOG_LEVEL_IMPORTANT = 6;
    public static final int LOG_LEVEL_CRITICAL = 9;

    public enum FORMAT{
    	STANDARD_FORMAT, CSV_FORMAT, PLAIN_FORMAT
    }

    public static void logError(String toFile, String message) {
        logError(FORMAT.STANDARD_FORMAT, toFile, message);
    }

    public static void logError(FORMAT formatType, String toFile, String message) {
        logEntry(formatType, System.err, toFile, message, LOG_LEVEL_CRITICAL);
        if(writeLogsToFile) {
            logEntry(formatType, System.err, "Errors", "(in " + toFile + ") " + message, LOG_LEVEL_CRITICAL);
        }
    }

    public static void log(String toFile, String message) {
    	log(FORMAT.STANDARD_FORMAT, toFile, message);
    }

    public static void log(FORMAT formatType, String toFile, String message) {
        log(formatType, toFile, message, LOG_LEVEL_ORDINARY);
    }

    public static void log(String toFile, String message, int nLevel) {
        log(FORMAT.STANDARD_FORMAT, toFile, message, nLevel);
    }

    public static void log(FORMAT formatType, String toFile, String message, int nLevel) {
        logEntry(formatType, System.out, toFile, message, nLevel);
    }

    public static void logStackTrace(String toFile, Exception ex) {
        logStackTrace(FORMAT.STANDARD_FORMAT, toFile, ex);
    }

    public static void logStackTrace(FORMAT formatType, String toFile, Exception ex) {
        StringWriter s = new StringWriter();
        ex.printStackTrace(new PrintWriter(s));
        logError(formatType, toFile, s.toString());
    }

    public static void logStackTrace(String toFile, Error ex) {
        logStackTrace(FORMAT.STANDARD_FORMAT, toFile, ex);
    }

    public static void logStackTrace(FORMAT formatType, String toFile, Error ex) {
        StringWriter s = new StringWriter();
        ex.printStackTrace(new PrintWriter(s));
        logError(formatType, toFile, s.toString());
    }

    // Private Implementation
    private static boolean writeLogsToFile = false;

    private static final Random theRandom = new Random();
    private static final Set<String> filesToSkip = new HashSet<String>();
    private static final long maximumLogfileSize = 25 * 1024 * 1024;


    private static void logEntry(FORMAT formatType, PrintStream ordinaryOutput, String toFile, String message, int logLevel) {
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
            String logMessage = logFormat(formatType, logLevel, ordinaryOutput == System.err, message);

            // If we are also displaying this file, write it to the standard output.
            if(filesToDisplay.contains(toFile) || logLevel >= minLevelToDisplay) {
                ordinaryOutput.println("[" + toFile + "] " + message);
            }

            // When constructing filename, if we are not writing to a particular directory,
            // go directly to the spillover file if one exists.
            String completeFilePath = ThreadContext.get("LOG_FOLDER") + "/" + toFile;
            if(!writeLogsToFile && spilloverLogfile != null) {
            	completeFilePath = spilloverLogfile;
            }

            // Periodically check to make sure we're not writing TOO MUCH to this file.
            if(filesToSkip.size() != 0 && filesToSkip.contains(completeFilePath)) {
                return;
            }
            if(theRandom.nextInt(1000) == 0) {
                // Verify that the file is not too large.
                if(new File(completeFilePath).length() > maximumLogfileSize) {
                    System.err.println("Adding " + completeFilePath + " to filesToSkip.");
                    filesToSkip.add(completeFilePath);
                    logLevel = 9;
                    logMessage = logFormat(formatType, logLevel, ordinaryOutput == System.err, "File too long; stopping all writes to this file.");
                }
            }

            switch(formatType){
        	case STANDARD_FORMAT: case PLAIN_FORMAT:
        		completeFilePath += ".log";
        		break;
        	case CSV_FORMAT:
        		completeFilePath += ".csv";
        		break;
            }

            // Finally, write the log message to the file.
            BufferedWriter out = new BufferedWriter(new FileWriter(completeFilePath, true));
            out.write(logMessage);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


   /* private static void logEntry(FORMAT formatType, PrintStream ordinaryOutput, String toFile, String message, int logLevel) {
        if(suppressLoggerOutput)
            return;

        toFile += Thread.currentThread().getId();

        // When we're not writing to a particular directory, and we're not spilling over into
        // a general logfile, write directly to the standard output unless it is really unimportant.
        if(!writeLogsToFile && spilloverLogfile == null) {
            if (logLevel >= LOG_LEVEL_ORDINARY) {
                ordinaryOutput.println("[" + toFile + "] " + message);
            }
            return;
        }

        try {
            String logMessage = logFormat(formatType, logLevel, ordinaryOutput == System.err, message);

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
                    logMessage = logFormat(formatType, logLevel, ordinaryOutput == System.err, "File too long; stopping all writes to this file.");
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
    */

    private static String logFormat(FORMAT formatType, int logLevel, boolean isError, String message) {

        String logMessage = "";

        switch(formatType){
        	case STANDARD_FORMAT:
        		logMessage = "LOG " + System.currentTimeMillis() + " [L" + logLevel + "]: " + (isError ? "<ERR> " : "") + message;
        		break;
        	case CSV_FORMAT: case PLAIN_FORMAT:
        		logMessage = message;
        		break;
        }

        int length = logMessage.length();
        if(length <= 0 || logMessage.charAt(length - 1) != '\n'){
            logMessage += '\n';     // All log lines must end with a newline.
        }
        return logMessage;
    }

    private static String myDirectory;
    private static HashSet<String> filesToDisplay = new HashSet<String>();
    private static int minLevelToDisplay = Integer.MAX_VALUE;
    private static boolean suppressLoggerOutput;
    private static String spilloverLogfile;


    /************************ ADDITIONAL PARAMETERS AND METHODS FOR LOGGING DURIGN TESTS ************************
     * This logger is normally meant to be used for a single player at a time.
     * To run experiments and testing, however, multiple players are running at the same time in the same program
     * instance. The following parameters and methods can be used to manage logging in such cases so that the logs
     * of multiple players won't interfere with each other.
     */

    //private boolean concurrentLogging;

    /**
     * This method starts file logging in a directory for the game and match on which an experiment/test is being
     * performed. The directory of the game tested will contain a directory for each match run in the experiment.
     * Each directory for a match will contain the log files written during the match by each player involved in
     * the experiment.
     *
     * NOTE: each log file already contains in its name an ID (the thread ID) that distinguishes it from the same
     * 		 file of another player. However, to be able to really distinguish which player a log file belongs to
     * 		 it is advisable to include a more meaningful ID (e.g. the player's role) to the file name (e.g.
     * 		 "xplayerStats" instead of just "Stats", xplayerStateMachine)
     * @param gameKey
     * @param m
     */
    /*
    public static void startConcurrentFileLogging(String testId, Match m) {
        writeLogsToFile = true;
        myDirectory = "testlogs/" + testId + "/" + m.getMatchId();

        new File(myDirectory).mkdirs();

        log("Logger", "Started logging to files at: " + new Date());
        log("Logger", "Game rules: " + m.getGame().getRules());
        log("Logger", "Start clock: " + m.getStartClock());
        log("Logger", "Play clock: " + m.getPlayClock());
    }*/

}