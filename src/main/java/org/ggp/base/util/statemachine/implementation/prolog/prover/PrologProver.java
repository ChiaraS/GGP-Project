package org.ggp.base.util.statemachine.implementation.prolog.prover;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ggp.base.util.logging.GamerLogger;

import com.declarativa.interprolog.PrologEngine;
import com.declarativa.interprolog.SWISubprocessEngine;
import com.declarativa.interprolog.XSBSubprocessEngine;
import com.declarativa.interprolog.YAPSubprocessEngine;
import com.xsb.interprolog.NativeEngine;

/** NOTE: THIS CLASS ONLY WORKS WITH YAP PROLOG ON LINUX - DON'T USE THIS CLASS BUT RATHER USE YapProver
 * This class acts like an interface between the state machine and Prolog.
 * The type of prolog to be used by this class is customizable: it is possible
 * to choose if to run this class over either YAP Prolog, SWI Prolog or XSB
 * Prolog (the last one can be set up as a subprocess but also as a native engine).
 * The state machine can call this class as if it was the real Prolog prover
 * without worrying about the details of setting it up and resetting it when a
 * query cannot be computed (i.e. if the real Prolog is not responding if it
 * is taking to long to respond, etc...).
 *
 * This class is a sort of fake Prolog prover that asks the real Prolog
 * prover to answer queries.
 *
 * NOTE: when done using this class, the shutdown method must be called to
 * "inform" this class that it will not be used anymore, so it can shutdown
 * also the real Prolog Prover.
 *
 * ATTENTION: this class is not thread safe when waitingTime is set with a positive
 * value. If more than one thread calls the same method of this class at the same
 * time, the results will be unreliable. This is because, when using threads to call
 * the queries, the same callable query instance is given to the executor after
 * resetting its goal. This doesn't create problems when the query thread manages to
 * finish before the timeout because the thread won't be running anymore when another
 * call will arrive and change the goal of the query instance. Moreover, there are no
 * problems also when the query thread exceeds the timeout, because before starting
 * another query with another thread the query object will be re-initialized causing
 * no conflicts.
 * NOTE that this class won't be thread safe in general since all queries are asked to
 * the same underlying prolog program.
 *
 * @author C.Sironi
 *
 */
public class PrologProver {

	/**
	 * Enumeration of all possible types of prolog that this class can mask.
	 *
	 * @author C.Sironi
	 *
	 */
	public enum PROLOG_TYPE{
    	YAP, SWI, XSB, NXSB
    }

	/**
	 * True if this class is being executed on linux, false if it is being executed on windows.
	 * Remember to change this when changing platform.
	 * TODO: this parameter needs to disappear/be changed so that it is not hardcoded.
	 */
	private static boolean LINUX = true;

	/**
	 * PAths to files and executables for windows and linux.
	 * TODO: also change these to not be hardcoded.
	 */

	private static String LINUX_DESCRIPTION_FILE_PATH = "/home/csironi/YAPplayer/prologFiles/description.pl";
	private static String LINUX_FUNCTIONS_FILE_PATH = "/home/csironi/YAPplayer/prologFiles/prologFunctions.pl";
	private static String LINUX_YAP_COMMAND = "/home/csironi/CadiaplayerInstallation/Yap/bin/yap";
	private static String LINUX_SWI_COMMAND = "/usr/bin/pl";
	private static String LINUX_XSB_COMMAND = "?";
	private static String LINUX_XSB_BIN = "?";

	private static String WINDOWS_DESCRIPTION_FILE_PATH = "C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\csironi\\windowsPrologFiles\\description.pl";
	private static String WINDOWS_FUNCTIONS_FILE_PATH = "C:\\Users\\c.sironi\\BITBUCKET REPOS\\GGP-Base\\csironi\\windowsPrologFiles\\prologFunctions.pl";
	private static String WINDOWS_YAP_COMMAND = "C:\\YapInstallation\\Yap64\\bin\\yap.exe";
	private static String WINDOWS_SWI_COMMAND = "C:\\SwiInstallation\\swipl\\bin\\swipl.exe";
	private static String WINDOWS_XSB_COMMAND = "C:\\XsbInstallation\\XSBWindows\\Xsb-3-2-7-Windows-Compiled\\config\\x86-pc-windows\\bin\\xsb";
	// If using XSB as native engine on windows, add this path also to the PATH environment variable.
	private static String WINDOWS_XSB_BIN = "C:\\XsbInstallation\\XSBWindows\\Xsb-3-2-7-Windows-Compiled\\config\\x86-pc-windows\\bin";

	// CLASSES REPRESENTING DIFFERENT TYPES OF QUERIES THAT CAN BE ASKED TO PROLOG

	/**
	 * Class that represents a query that returns some result in the form of a binding.
	 *
	 * @author C.Sironi
	 *
	 */
	public class CallableBindingsQuery implements Callable<Object[]>{

		/**
		 * String representing the goal of the query with Prolog format.
		 */
		private String goal;

		/**
		 * String representing the prolog name(s) of the variable(s) that
		 * Prolog must return to java in the binding(s).
		 */
		private String resVar;

		/**
		 * Constructor that initializes this query object as an empty query.
		 */
		public CallableBindingsQuery(){
			this.goal = null;
			this.resVar = null;
		}

		/**
		 * Method that sets goal and return variables names for this query.
		 *
		 * @param goal the prolog format goal to be asked to Prolog.
		 * @param resVar the prolog names of the variable that Prolog must return
		 * to java as a result of this query.
		 */
		public void setQuery(String goal, String resVar){
			this.goal = goal;
			this.resVar = resVar;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public Object[] call() throws Exception {
			return prologProver.deterministicGoal(goal,resVar);
		}

	}

	/**
	 * Class that represents a query that returns no results other than the query success or failure.
	 *
	 * @author C.Sironi
	 *
	 */
	public class CallableYesNoQuery implements Callable<Boolean>{

		/**
		 * String representing the goal of the query with Prolog format.
		 */
		private String goal;

		/**
		 * Constructor that initializes this query object as an empty query.
		 */
		public CallableYesNoQuery(){
			this.goal = null;
		}

		/**
		 * Method that sets the goal for this query.
		 *
		 * @param goal the prolog format goal to be asked to Prolog.
		 */
		public void setQuery(String goal){
			this.goal = goal;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public Boolean call() throws Exception {
			return prologProver.deterministicGoal(this.goal);
		}

	}

	// VARIABLES NEEDED TO INITIALIZE PROLOG AS A STATE MACHINE REPRESENTING THE CURRENT GAME
	// AND VARIABLES NEEDED TO START THE EXECUTION OF PROLOG

	private final PROLOG_TYPE prologType;

	/**
	 * Description of the game that this Prolog prover must reason on.
	 * The description must have the prolog syntax and will be written as
	 * is on a prolog (.pl) file.
	 */
	private String description;

	/**
	 *  Path of the file where to put the game description.
	 */
	private String descriptionFilePath;

	/**
	 *  Path of the file with all the predefined Prolog functions.
	 */
	private String functionsFilePath;

	/**
	 * Path of the command that this class must use to run Prolog.
	 * (if using XSB prolog with the NativeEngine, this will be the bin directory path)
	 */
	private String prologPath;

	/**
	 * True if this Prolog class is ready to answer queries, false otherwise.
	 * This parameter is needed to check if Prolog is ready or needs to be
	 * started up before asking a query.
	 */
	private boolean isReady;

	// THE OBJECT REPRESENTING THE PROLOG ENGINE TO BE CALLED TO ANSWER QUERIES

	/**
	 * Prolog engine that this class must use to answer queries.
	 */
	private PrologEngine prologProver;


	// VARIABLES NEEDED TO CONTROL THE EXECUTION TIME OF THE QUERIES

	/**
	 * This fake Prolog prover must wait for this amount of time for the underlying
	 * real Prolog prover to answer to a query.
	 * If time is negative or equal to 0 this fake Prolog prover will wait indefinitely.
	 */
	private long waitingTime;

	/**
	 * Executor that executes the queries to Prolog as threads, waiting for the
	 * answer only for the specified amount of time.
	 */
	private ExecutorService executor;

	// VARIABLES REPRESENTING THE QUERY OBJECTS USED TO ASK QUERIES TO PROLOG AS THREADS'TASKS

	/**
	 * Callable query that returns a binding to its result(s).
	 */
	private CallableBindingsQuery bindingsQuery;

	/**
	 * Callable query that returns true when it succeeds, false otherwise.
	 */
	private CallableYesNoQuery yesNoQuery;

	// CONSTRUCTORS
	// NOTE: if the constructor fails, this Prolog Prover cannot be used.

	/**
	 * Constructor that sets the game description to the given game description,
	 * the prolog type to YAP (i.e. this prolog prover will use YAP prolog to
	 * answer queries) and the waiting time for a query to a non-positive value
	 * (i.e. this prover will wait indefinitely for the answer of the query).
	 *
	 * @param description the game description with prolog syntax.
	 * @throws PrologProverException to signal that something went wrong during initialization
	 * of the Prolog Prover and thus it cannot be started and used.
	 */
	public PrologProver(String description) throws PrologProverException {
		this(description, PROLOG_TYPE.YAP, 0L);
	}

	/**
	 * Constructor that sets the game description and the waiting time for
	 * the queries to the given values and the prolog type to the default
	 * value, YAP.
	 *
	 * @param description the game description with prolog syntax.
	 * @param waitingTime the maximum time to wait for a query.
	 * @throws PrologProverException to signal that something went wrong during initialization
	 * of the Prolog Prover and thus it cannot be started and used.
	 */
	public PrologProver(String description, long waitingTime) throws PrologProverException {
		this(description, PROLOG_TYPE.YAP, waitingTime);
	}

	/**
	 * Constructor that sets the game description to the given game description,
	 * the prolog type to the given prolog type and the waiting time for a query
	 * to a non-positive value (i.e. this prover will wait indefinitely for the
	 * answer of the query) and all other parameters to the given values.
	 *
	 * @param description  the game description with prolog syntax.
	 * @param prologType the type of prolog to be used to answer queries.
	 * @throws PrologProverException to signal that something went wrong during initialization
	 * of the Prolog Prover and thus it cannot be started and used.
	 */
	public PrologProver(String description, PROLOG_TYPE prologType) throws PrologProverException {
		this(description, prologType, 0L);
	}

	/**
	 * Constructor that sets all the parameters to the given values.
	 *
	 * @param description the game description with prolog syntax.
	 * @param prologType the type of prolog to be used to answer queries.
	 * @param waitingTime the maximum time to wait for a query.
	 * @throws PrologProverException to signal that something went wrong during initialization
	 * of the Prolog Prover and thus it cannot be started and used.
	 */
	public PrologProver(String description, PROLOG_TYPE prologType, long waitingTime) throws PrologProverException{
		this.description = description;

		this.prologType = prologType;

		if(LINUX){ // LINUX
			this.descriptionFilePath = LINUX_DESCRIPTION_FILE_PATH;
			this.functionsFilePath = LINUX_FUNCTIONS_FILE_PATH;

			switch(this.prologType){
			case YAP:
				this.prologPath = LINUX_YAP_COMMAND;
				break;
			case SWI:
				this.prologPath = LINUX_SWI_COMMAND;
				break;
			case XSB:
				this.prologPath = LINUX_XSB_COMMAND;
				break;
			case NXSB:
				this.prologPath = LINUX_XSB_BIN;
				break;
			default:
				GamerLogger.logError("StateMachine", "[PrologProver] Exception during initialization of prolog prover. Unrecognized prolog type " + this.prologType + ".");
				throw new PrologProverException("Creation of Prolog prover failed. " + this.prologType + " is not a valid prolog type.");
			}
		}else{ // WINDOWS
			this.descriptionFilePath = WINDOWS_DESCRIPTION_FILE_PATH;
			this.functionsFilePath = WINDOWS_FUNCTIONS_FILE_PATH;

			switch(this.prologType){
			case YAP:
				this.prologPath = WINDOWS_YAP_COMMAND;
				break;
			case SWI:
				this.prologPath = WINDOWS_SWI_COMMAND;
				break;
			case XSB:
				this.prologPath = WINDOWS_XSB_COMMAND;
				break;
			case NXSB:
				this.prologPath = WINDOWS_XSB_BIN;
				break;
			default:
				GamerLogger.logError("StateMachine", "[PrologProver] Exception during initialization of prolog prover. Unrecognized prolog type " + this.prologType + ".");
				throw new PrologProverException("Creation of Prolog prover failed. " + this.prologType + " is not a valid prolog type.");
			}
		}

		this.waitingTime = waitingTime;
		this.executor = null;
		this.bindingsQuery = null;
		this.yesNoQuery = null;

		this.isReady = false;

		// Write game description to a file
		try {
			writeDescription(this.description);
			GamerLogger.log("StateMachine", "[PrologProver] Prolog game description saved, trying to start up real prover.");
		} catch (IOException e) {
			// Log the exception
			GamerLogger.logError("StateMachine", "[PrologProver] Exception during initialization of " + this.prologType + " prover. Impossible to write game description on file.");
			GamerLogger.logStackTrace("StateMachine", e);
			// Throw a new exception.
			throw new PrologProverException("Creation of " + this.prologType + " prover failed. Impossible to write game description on file.", e);
		}

		// Note: if the startup fails the first time, it's highly likely that there is something
		// wrong with the PROLOG execution command (this.prologPath), or something wrong with the
		// installation of Prolog or with the functions file being consulted (i.e. is because
		// of a programming/installing error, not an unpredictable cause). This is why we
		// included the first startup in the constructor of the class, and if it fails we assume
		// that this class won't be usable ever, so we shut it down.
		// On the contrary, if it succeeds, we assume it's highly likely to always succeed (or at
		// most it will fail for an unpredictable cause not likely to happen always), thus whenever
		// it fails before asking a query, we just throw an exception saying that the query couldn't
		// be computed and we'll try to start it up again next time we receive a query.
		// Feel free to remove this from here if you want to ignore this check and just try every
		// time to start up Prolog. The fact that startup failed the first time might also be
		// just a coincidence.
		this.startup();
	}

	// METHODS TO START, SHUTDOWN OR RESET THE PROLOG PROVER

	/**
	 * This method starts the Prolog Prover running an instance of the actual Prolog
	 * program of the selected type, setting it up to reason on the given game description.
	 * If a positive waiting time is set, this method also makes sure to prepare the
	 * executor to manage queries that might need to be interrupted.
	 *
	 * @throws PrologProverException if something goes wrong when starting up the real
	 * Prolog Prover and thus this java Prolog Prover cannot be used.
	 */
	private void startup() throws PrologProverException{

		try{

			switch(this.prologType){
			case YAP:
				// Create the bridge between Java and YAP Prolog, trying to start the Prolog program.
				this.prologProver = new YAPSubprocessEngine(this.prologPath);
				break;
			case SWI:
				// Create the bridge between Java and SWI Prolog, trying to start the Prolog program.
				this.prologProver = new SWISubprocessEngine(this.prologPath);
				break;
			case XSB:
				// Create the bridge between Java and XSB Prolog, trying to start the Prolog program.
				this.prologProver = new XSBSubprocessEngine(this.prologPath);
				break;
			case NXSB:
				// Create the bridge between Java and XSB Prolog, using the native engine.
				this.prologProver = new NativeEngine(this.prologPath);
				break;
			default:
				GamerLogger.logError("StateMachine", "[PrologProver] Exception during startup of " + this.prologType + " prover: unrecognized prolog path. Shutting down.");
				this.shutdown();
				// Throw an exception.
				throw new PrologProverException("Startup of " + this.prologType + " prover failed.");
			}

			GamerLogger.log("StateMachine", "Creation of PrologProver succeeded.");

			// Tell to Prolog to consult the file with the functions definitions
			// (the game description will also be consulted since it is referenced in
			// the functions file.)
			this.prologProver.consultAbsolute(new File(functionsFilePath));

			GamerLogger.log("StateMachine", "Prolog function file consulted.");

			// Not needed for now since the state machine is not calling
			// any query that uses random numbers.
			//this.randomizeProlog();

			// If a positive waiting time has been defined, also create the executor
			// that will take care of stopping the queries that are taking too long
			// and create the callable objects that represent the queries.
			if(this.waitingTime > 0){
				this.executor = Executors.newSingleThreadExecutor();
				this.bindingsQuery = new CallableBindingsQuery();
				this.yesNoQuery = new CallableYesNoQuery();
			}

			this.isReady = true;
		}catch(RuntimeException e){
			// Log the exception
			GamerLogger.logError("StateMachine", "[PrologProver] Exception during startup of " + this.prologType + " prover. Shutting down.");
			GamerLogger.logStackTrace("StateMachine", e);
			this.shutdown();
			// Throw an exception.
			throw new PrologProverException("Startup of " + this.prologType + " prover failed.", e);
		}

	}

	/**
	 * This method shuts down this Prolog Prover, making sure that the real Prolog Prover program
	 * is also stopped and that the query executor is shutdown as well and all its tasks are
	 * asked to interrupt (note that we can't be 100% sure that they will interrupt immediately
	 * or ever interrupt).
	 */
	public void shutdown(){
		if(this.executor != null){
			// Shutdown the executor and remove the reference to it.
			this.executor.shutdownNow();
			// If the executor gives any concurrency problem use this instruction
			// to wait for all threads to actually die and if they don't do something
			// to deal with it (i.e. throw exception or log this as it is probably a
			// programming fault since all the query threads are supposed to terminate
			// after interruption).
			//this.executor.awaitTermination(timeout, unit);
			this.executor = null;
		}

		if(this.prologProver != null){
			// Shutdown Prolog and remove the reference to it, as it is now unusable.
			this.prologProver.shutdown();
			this.prologProver = null;
		}

		// Remove the reference to the callable query objects (note that the last one
		// of them that has been called might still be running --> it should quit soon
		// anyway if it has been programmed properly).
		this.bindingsQuery = null;
		this.yesNoQuery = null;

		this.isReady = false;
	}

	/**
	 *	Write the game description on a prolog file.
	 *
	 * @param string the description of the game.
	 * @throws IOException if an I/O error occurs when writing.
	 */
	private void writeDescription(String string) throws IOException{

		BufferedWriter out = null;
		try{
			out = new BufferedWriter(new FileWriter(this.descriptionFilePath));
			out.write(string);
		}finally{
			if(out != null){
				out.close();
			}
		}
	}

	//private Random random = new Random();

	/**
	 * Change the Prolog random number generator
	 * using the Java random number generator
	 */
	/*
	private void randomizeProlog()
	{

		//int i = (int)Math.min(Math.random()*(30268), 30268)+1;
		//int j = (int)Math.min(Math.random()*(30307), 30307)+1;
		//int k = (int)Math.min(Math.random()*(30323), 30323)+1;

		int i = this.random.nextInt(30268)+1;
		int j = this.random.nextInt(30306)+1;
		int k = this.random.nextInt(30322)+1;
		this.prologProver.realCommand("setrand(rand("+i+", "+j+", "+k+"))");
	}*/

	/**
	 * This method performs a query on the real Prolog program, returning any result
	 * returned by the query.
	 *
	 * @param goal string representing the goal of the query with Prolog format.
	 * @param resVar String representing the prolog name(s) of the variable(s) that
	 * Prolog must return to java in the binding(s).
	 * @return the bindings containing the result(s) of the query to Prolog (NOTE
	 * that prolog might compute that the query has no results and so the bindings
	 * will be empty).
	 * @throws PrologProverException if something went wrong during the query and no answer
	 * from Prolog was received at all (either because of a timeout or because of
	 * another exception). In this case Prolog needs restarting to be able to answer
	 * another query correctly.
	 */
	public Object[] askQueryResults(String goal, String resVar) throws PrologProverException{

		// Check if the PrologEngine is ready to answer queries or if we have to start it again
		// because it was shutdown during the previous query because of an error.
		// If startup fails, just throw an exception to say that the computation of the query failed.
		// The startup method takes care of shutting everything down if it fails.
		if(! this.isReady){
			try{
				this.startup();
			}catch(PrologProverException e){
				GamerLogger.logError("StateMachine", "[PrologProver] Impossible to compute query result on " + this.prologType + " prolog side: prolog engine restart failed.");
				GamerLogger.logStackTrace("StateMachine", e);
				throw new PrologProverException("Computation of query \"" + goal + "\" with result variables \"" + resVar + "\" on " + this.prologType + " prolog side couldn't be completed: prolog engine restart failed.", e);
			}
		}

		Object[] bindings = null;

		// If a positive waiting time has been set, give a timeout to the query.
		if(this.waitingTime > 0){

			// Change the goal of the query task to be executed by the executor.
			this.bindingsQuery.setQuery(goal, resVar);

			try {
				// Try to query Prolog and wait for an answer till timeout has been reached.
				bindings = this.executor.invokeAny(Arrays.asList(this.bindingsQuery),this.waitingTime, TimeUnit.MILLISECONDS);
			} catch (ExecutionException
					| TimeoutException e) {
				// If something went wrong or timeout has been reached, then throw an exception.
				GamerLogger.logError("StateMachine", "[PrologProver] Impossible to compute query result on " + this.prologType + " prolog side: computation failed on prolog side.");
				GamerLogger.logStackTrace("StateMachine", e);
				this.shutdown();
				throw new PrologProverException("Computation of query \"" + goal + "\" with result variables \"" + resVar + "\" on " + this.prologType + " prolog side couldn't be completed: computation failed on prolog side.", e);
			} catch (InterruptedException e) {
				// If the thread using this Prolog Prover has been interrupted, still throw an exception
				// cause the query couldn't be computed, but also re-set the interrupted status of the
				// current thread to "true" so that also the callers of this method know that they have
				// to interrupt.
				GamerLogger.logError("StateMachine", "[PrologProver] Impossible to compute query result on " + this.prologType + " prolog: PrologProver has been interrupted before getting the result.");
				GamerLogger.logStackTrace("StateMachine", e);
				this.shutdown();
				Thread.currentThread().interrupt();
				throw new PrologProverException("Computation of query \"" + goal + "\" with result variables \"" + resVar + "\" on " + this.prologType + " prolog side couldn't be completed: PrologProver has been interrupted before getting the result.", e);
			}

		// If no positive waiting time has been set just wait indefinitely.
		}else{
			try{
				bindings = this.prologProver.deterministicGoal(goal, resVar);
			// Catch all possible exceptions of Interprolog and re-throw them as a PrologProverException
			// to signal that something went wrong and the query couldn't be answered.
			}catch(RuntimeException e){
				GamerLogger.logError("StateMachine", "[PrologProver] Impossible to compute query result on " + this.prologType + " prolog side: Interprolog exception occurred.");
				GamerLogger.logStackTrace("StateMachine", e);
				this.shutdown();
				throw new PrologProverException("Computation of query \"" + goal + "\" with result variables \"" + resVar + "\" on " + this.prologType + " prolog side couldn't be completed: Interprolog exception occurred.", e);
			}
		}

		return bindings;

	}

	/**
	 * This method performs a query on the real Prolog program, checking if it succeeded
	 * or failed.
	 *
	 * @param goal string representing the goal of the query with Prolog format.
	 * @return true if the goal succeeded, false otherwise.
	 * @throws PrologProverException if something went wrong during the query and no answer
	 * from Prolog was received at all (either because of a timeout or because of
	 * another exception). In this case Prolog needs restarting to be able to answer
	 * another query correctly.
	 */
	public boolean askQuerySuccess(String goal) throws PrologProverException{

		// Check if the PrologEngine is ready to answer queries or if we have to start it again
		// because it was shutdown during the previous query because of an error.
		// If startup fails, just throw an exception to say that the computation of the query failed.
		// The startup method takes care of shutting everything down if it fails.
		if(! this.isReady){
			try{
				this.startup();
			}catch(PrologProverException e){
				GamerLogger.logError("StateMachine", "[PrologProver] Impossible to compute yes/no query on " + this.prologType + " prolog side: prolog engine restart failed.");
				GamerLogger.logStackTrace("StateMachine", e);
				throw new PrologProverException("Computation of yes/no query \"" + goal + "\" on " + this.prologType + " prolog side couldn't be completed: prolog engine restart failed.", e);
			}
		}

		boolean success = false;

		// If a positive waiting time has been set, give a timeout to the query.
		if(this.waitingTime > 0){

			// Change the goal of the query task to be executed by the executor.
			this.yesNoQuery.setQuery(goal);

			try {
				// Try to query Prolog and wait for an answer till timeout has been reached.
				success = this.executor.invokeAny(Arrays.asList(this.yesNoQuery),this.waitingTime, TimeUnit.MILLISECONDS);
			} catch (ExecutionException
					| TimeoutException e) {
				// If something went wrong or timeout has been reached, then throw an exception.
				GamerLogger.logError("StateMachine", "[PrologProver] Impossible to compute yes/no query on " + this.prologType + " prolog side: computation failed on prolog side.");
				GamerLogger.logStackTrace("StateMachine", e);
				this.shutdown();
				throw new PrologProverException("Computation of yes/no query \"" + goal + "\" on " + this.prologType + " prolog side couldn't be completed: computation failed on prolog side.", e);
			} catch (InterruptedException e) {
				// If the thread using this Prolog Prover has been interrupted, still throw an exception
				// cause the query couldn't be computed, but also re-set the interrupted status of the
				// current thread to "true" so that also the callers of this method know that they have
				// to interrupt.
				GamerLogger.logError("StateMachine", "[PrologProver] Impossible to compute yes/no query on " + this.prologType + " prolog side: PrologProver has been interrupted before getting the result.");
				GamerLogger.logStackTrace("StateMachine", e);
				this.shutdown();
				Thread.currentThread().interrupt();
				throw new PrologProverException("Computation of yes/no query \"" + goal + "\" on " + this.prologType + " prolog side couldn't be completed: PrologProver has been interrupted before getting the result.", e);

			}

		// If no positive waiting time has been set just wait indefinitely.
		}else{
			try{
				success = this.prologProver.deterministicGoal(goal);
			// Catch all possible exceptions of Interprolog and re-throw them as a PrologProverException
			// to signal that something went wrong and the query couldn't be answered.
			}catch(RuntimeException e){
				GamerLogger.logError("StateMachine", "[PrologProver] Impossible to compute yes/no query on " + this.prologType + " prolog side: Interprolog exception occurred.");
				GamerLogger.logStackTrace("StateMachine", e);
				this.shutdown();
				throw new PrologProverException("Computation of yes/no query \"" + goal + "\" on " + this.prologType + " prolog side couldn't be completed: Interprolog exception occurred.", e);
			}
		}

		return success;

	}

	public boolean isReady(){
		return this.isReady();
	}

}