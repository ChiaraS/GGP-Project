package org.ggp.base.util.statemachine.implementation.yapProlog.prover;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.declarativa.interprolog.YAPSubprocessEngine;

/**
 * This class acts like an interface between the state machine and Yap Prolog.
 * The state machine can call this class as if it was the real Yap Prolog prover
 * without worrying about the details of setting it up and resetting it when a
 * query cannot be computed (i.e. if the real Yap Prolog is not responding if it
 * is taking to long to respond, etc...).
 *
 * This class is a sort of fake Yap Prolog prover that asks the real Yap Prolog
 * prover to answer queries.
 *
 * NOTE: when done using this class, the shutdown method must be called to
 * "inform" this class that it will not be used anymore, so it can shutdown
 * also the real Yap Prover.
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
public class YapProver {

	/**
	 * Static reference to the logger
	 */
	private static final Logger LOGGER;

	static{

		LOGGER = LogManager.getRootLogger();

	}

	private static String LINUX_DESCRIPTION_FILE_PATH = "/home/csironi/PrologPlayer/prologFiles/description.pl";
	private static String LINUX_FUNCTIONS_FILE_PATH = "/home/csironi/PrologPlayer/prologFiles/prologFunctions.pl";
	private static String LINUX_YAP_COMMAND = "/home/csironi/CadiaplayerInstallation/Yap/bin/yap";

	// CLASSES REPRESENTING DIFFERENT TYPES OF QUERIES THAT CAN BE ASKED TO YAP PROLOG

	/**
	 * Class that represents a query that returns some result in the form of a binding.
	 *
	 * @author C.Sironi
	 *
	 */
	public class CallableBindingsQuery implements Callable<Object[]>{

		/**
		 * String representing the goal of the query with Yap Prolog format.
		 */
		private String goal;

		/**
		 * String representing the prolog name(s) of the variable(s) that
		 * Yap Prolog must return to java in the binding(s).
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
		 * @param goal the prolog format goal to be asked to Yap.
		 * @param resVar the prolog names of the variable that Yap must return
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
			return yapProver.deterministicGoal(goal,resVar);
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
		 * String representing the goal of the query with Yap Prolog format.
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
		 * @param goal the prolog format goal to be asked to Yap.
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
			return yapProver.deterministicGoal(this.goal);
		}

	}

	// VARIABLES NEEDED TO INITIALIZE YAP PROLOG AS A STATE MACHINE REPRESENTING THE CURRENT GAME
	// AND VARIABLES NEEDED TO START THE EXECUTION OF PROLOG

	/**
	 * Description of the game that this Yap Prolog prover must reason on.
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
	 * Command that this class must use to run Yap.
	 */
	private String yapCommand;

	/**
	 * True if this Yap Prolog class is ready to answer queries, false otherwise.
	 * This parameter is needed to check if Yap prolog is ready or needs to be
	 * started up before asking a query.
	 */
	private boolean isReady;

	// THE OBJECT REPRESENTING THE YAP ENGINE TO BE CALLED TO ANSWER QUERIES

	/**
	 * Yap engine that this class must use to answer queries.
	 */
	private YAPSubprocessEngine yapProver;


	// VARIABLES NEEDED TO CONTROL THE EXECUTION TIME OF THE QUERIES

	/**
	 * This fake Yap Prolog prover must wait for this amount of time for the underlying
	 * real Yap Prolog prover to answer to a query.
	 * If time is negative or equal to 0 this fake Yap Prolog prover will wait indefinitely.
	 */
	private long waitingTime;

	/**
	 * Executor that executes the queries to Yap Prolog as threads, waiting for the
	 * answer only for the specified amount of time.
	 */
	private ExecutorService executor;

	// VARIABLES REPRESENTING THE QUERY OBJECTS USED TO ASK QUERIES TO YAP AS THREADS'TASKS

	/**
	 * Callable query that returns a binding to its result(s).
	 */
	private CallableBindingsQuery bindingsQuery;

	/**
	 * Callable query that returns true when it succeeds, false otherwise.
	 */
	private CallableYesNoQuery yesNoQuery;

	// CONSTRUCTORS
	// NOTE: if the constructor fails, this Yap Prover cannot be used.

	/**
	 * Constructor that sets the game description to the given game description,
	 * the waiting time for a query to a non-positive value (i.e. this class
	 * will wait indefinitely for the answer of the query) and all other parameters
	 * to default values (the default values for the file paths and the prolog
	 * execution command are defined for linux GoGeneral server).
	 *
	 * @param description the game description with prolog syntax.
	 * @throws YapProverException to signal that something went wrong during initialization
	 * of the Yap Prover and thus it cannot be started and used.
	 */
	public YapProver(String description) throws YapProverException {
		this(description, 0L);
	}

	/**
	 * Constructor that sets the game description and the waiting time for
	 * the queries to the given values and all other parameters to the default
	 * values for the linux GoGeneral server.
	 *
	 * @param description the game description with prolog syntax.
	 * @param waitingTime the maximum time to wait for a query.
	 * @throws YapProverException to signal that something went wrong during initialization
	 * of the Yap Prover and thus it cannot be started and used.
	 */
	public YapProver(String description, long waitingTime) throws YapProverException {
		this(description, LINUX_DESCRIPTION_FILE_PATH, LINUX_FUNCTIONS_FILE_PATH, LINUX_YAP_COMMAND, waitingTime);
	}

	/**
	 * Constructor that sets the game description to the given game description,
	 * the waiting time for a query to a non-positive value (i.e. this class
	 * will wait indefinitely for the answer of the query) and all other parameters
	 * to the given values.
	 *
	 * @param description  the game description with prolog syntax.
	 * @param descriptionFilePath path of the file where to put the game description.
	 * @param functionsFilePath path of the file with all the predefined Prolog functions.
	 * @param yapCommand command that this class must use to run Yap.
	 * @throws YapProverException to signal that something went wrong during initialization
	 * of the Yap Prover and thus it cannot be started and used.
	 */
	public YapProver(String description, String descriptionFilePath, String functionsFilePath, String yapCommand) throws YapProverException {
		this(description, descriptionFilePath, functionsFilePath, yapCommand, 0L);
	}

	/**
	 * Constructor that sets all the parameters to the given values.
	 *
	 * @param description the game description with prolog syntax.
	 * @param descriptionFilePath path of the file where to put the game description.
	 * @param functionsFilePath path of the file with all the predefined Prolog functions.
	 * @param yapCommand command that this class must use to run Yap.
	 * @param waitingTime the maximum time to wait for a query.
	 * @throws YapProverException to signal that something went wrong during initialization
	 * of the Yap Prover and thus it cannot be started and used.
	 */
	public YapProver(String description, String descriptionFilePath, String functionsFilePath, String yapCommand, long waitingTime) throws YapProverException{
		this.description = description;
		this.descriptionFilePath = descriptionFilePath;
		this.functionsFilePath = functionsFilePath;
		this.yapCommand = yapCommand;
		this.waitingTime = waitingTime;
		this.executor = null;
		this.bindingsQuery = null;
		this.yesNoQuery = null;

		this.isReady = false;

		// Write game description to a file
		try {
			writeDescription(this.description);
		} catch (IOException e) {
			// Log the exception
			LOGGER.error("[StateMachine] [YAP] [YapProver] Exception during initialization of Yap Prover.", e);
			// Throw a new exception.
			throw new YapProverException("Creation of Yap Prover failed.", e);
		}

		// Note: if the startup fails the first time, it's highly likely that there is something
		// wrong with the YAP execution command (this.yapCommand), or something wrong with the
		// installation of Yap Prolog or with the functions file being consulted (i.e. is because
		// of a programming/installing error, not an unpredictable cause). This is why we
		// included the first startup in the constructor of the class, and if it fails we assume
		// that this class won't be usable ever, so we shut it down.
		// On the contrary, if it succeeds, we assume it's highly likely to always succeed (or at
		// most it will fail for an unpredictable cause not likely to happen always), thus whenever
		// it fails before asking a query, we just throw an exception saying that the query couldn't
		// be computed and we'll try to start it up again next time we receive a query.
		// Feel free to remove this from here if you want to ignore this check and just try every
		// time to start up Yap Prolog. The fact that startup failed the first time might also be
		// just a coincidence.
		this.startup();
	}

	// METHODS TO START, SHUTDOWN OR RESET THE YAP PROVER

	/**
	 * This method starts the Yap Prover running an instance of the actual Yap Prolog
	 * program, setting it up to reason on the given game description. If a positive
	 * waiting time is set, this method also makes sure to prepare the executor to
	 * manage queries that might need to be interrupted.
	 *
	 * @throws YapProverException if something goes wrong when starting up the real
	 * Yap Prover and thus this java Yap Prover cannot be used.
	 */
	private void startup() throws YapProverException{

		try{

			// Create the bridge between Java and YAP Prolog, trying to start the YAP Prolog program.
			this.yapProver = new YAPSubprocessEngine(this.yapCommand);

			// Tell to Yap Prolog to consult the file with the functions definitions
			// (the game description will also be consulted since it is referenced in
			// the functions file.)
			this.yapProver.consultAbsolute(new File(functionsFilePath));

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
			LOGGER.error("[StateMachine] [YAP] [YapProver] Exception during startup of Yap Prover. Shutting down.", e);
			this.shutdown();
			// Throw an exception.
			throw new YapProverException("Startup of Yap Prover failed.", e);
		}

	}

	/**
	 * This method shuts down this Yap Prover, making sure that the real Yap Prover program
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

		if(this.yapProver != null){
			// Shutdown Yap Prolog and remove the reference to it, as it is now unusable.
			this.yapProver.shutdown();
			this.yapProver = null;
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
		this.yapProver.realCommand("setrand(rand("+i+", "+j+", "+k+"))");
	}*/

	/**
	 * This method performs a query on the real Yap Prolog program, returning any result
	 * returned by the query.
	 *
	 * @param goal string representing the goal of the query with Yap Prolog format.
	 * @param resVar String representing the prolog name(s) of the variable(s) that
	 * Yap Prolog must return to java in the binding(s).
	 * @return the bindings containing the result(s) of the query to Yap Prolog (NOTE
	 * that prolog might compute that the query has no results and so the bindings
	 * will be empty).
	 * @throws YapProverException if something went wrong during the query and no answer
	 * from Yap Prolog was received at all (either because of a timeout or because of
	 * another exception). In this case Yap Prolog needs restarting to be able to answer
	 * another query correctly.
	 */
	public Object[] askQueryResults(String goal, String resVar) throws YapProverException{

		// Check if the YapSubprocessEngine is ready to answer queries or if we have to start it again
		// because it was shutdown during the previous query because of an error.
		// If startup fails, just throw an exception to say that the computation of the query failed.
		// The startup method takes care of shutting everything down if it fails.
		if(! this.isReady){
			try{
				this.startup();
			}catch(YapProverException e){
				LOGGER.error("[StateMachine] [YAP] [YapProver] Impossible to complete the computation of query result on Yap Prolog side.", e);
				throw new YapProverException("Computation of query \"" + goal + "\" with result variables \"" + resVar + "\" on Yap Prolog side couldn't be completed.", e);
			}
		}

		Object[] bindings = null;

		// If a positive waiting time has been set, give a timeout to the query.
		if(this.waitingTime > 0){

			// Change the goal of the query task to be executed by the executor.
			this.bindingsQuery.setQuery(goal, resVar);

			try {
				// Try to query Yap Prolog and wait for an answer till timeout has been reached.
				bindings = this.executor.invokeAny(Arrays.asList(this.bindingsQuery),this.waitingTime, TimeUnit.MILLISECONDS);
			} catch (ExecutionException
					| TimeoutException e) {
				// If something went wrong or timeout has been reached, then throw an exception.
				LOGGER.error("[StateMachine] [YAP] [YapProver] Impossible to complete the computation of query result on Yap Prolog side.", e);
				this.shutdown();
				throw new YapProverException("Computation of query \"" + goal + "\" with result variables \"" + resVar + "\" on Yap Prolog side couldn't be completed.", e);
			} catch (InterruptedException e) {
				// If the thread using this Yap Prover has been interrupted, still throw an exception
				// cause the query couldn't be computed, but also re-set the interrupted status of the
				// current thread to "true" so that also the callers of this method know that they have
				// to interrupt.
				LOGGER.error("[StateMachine] [YAP] [YapProver] Impossible to complete the computation of query result on Yap Prolog side.", e);
				this.shutdown();
				Thread.currentThread().interrupt();
				throw new YapProverException("Computation of query \"" + goal + "\" with result variables \"" + resVar + "\" on Yap Prolog side couldn't be completed.", e);
			}

		// If no positive waiting time has been set just wait indefinitely.
		}else{
			try{
				bindings = this.yapProver.deterministicGoal(goal, resVar);
			// Catch all possible exceptions of Interprolog and re-throw them as a YapPrologException
			// to signal that something went wrong and the query couldn't be answered.
			}catch(RuntimeException e){
				LOGGER.error("[StateMachine] [YAP] [YapProver] Impossible to complete the computation of query result on Yap Prolog side.", e);
				this.shutdown();
				throw new YapProverException("Computation of query \"" + goal + "\" with result variables \"" + resVar + "\" on Yap Prolog side couldn't be completed.", e);
			}
		}

		return bindings;

	}

	/**
	 * This method performs a query on the real Yap Prolog program, checking if it succeeded
	 * or failed.
	 *
	 * @param goal string representing the goal of the query with Yap Prolog format.
	 * @return true if the goal succeeded, false otherwise.
	 * @throws YapProverException if something went wrong during the query and no answer
	 * from Yap Prolog was received at all (either because of a timeout or because of
	 * another exception). In this case Yap Prolog needs restarting to be able to answer
	 * another query correctly.
	 */
	public boolean askQuerySuccess(String goal) throws YapProverException{

		// Check if the YapSubprocessEngine is ready to answer queries or if we have to start it again
		// because it was shutdown during the previous query because of an error.
		// If startup fails, just throw an exception to say that the computation of the query failed.
		// The startup method takes care of shutting everything down if it fails.
		if(! this.isReady){
			try{
				this.startup();
			}catch(YapProverException e){
				LOGGER.error("[StateMachine] [YAP] [YapProver] Impossible to complete the computation of yes/no query on Yap Prolog side.", e);
				throw new YapProverException("Computation of yes/no query \"" + goal + "\" on Yap Prolog side couldn't be completed.", e);
			}
		}

		boolean success = false;

		// If a positive waiting time has been set, give a timeout to the query.
		if(this.waitingTime > 0){

			// Change the goal of the query task to be executed by the executor.
			this.yesNoQuery.setQuery(goal);

			try {
				// Try to query Yap Prolog and wait for an answer till timeout has been reached.
				success = this.executor.invokeAny(Arrays.asList(this.yesNoQuery),this.waitingTime, TimeUnit.MILLISECONDS);
			} catch (ExecutionException
					| TimeoutException e) {
				// If something went wrong or timeout has been reached, then throw an exception.
				LOGGER.error("[StateMachine] [YAP] [YapProver] Impossible to complete the computation of yes/no query on Yap Prolog side.", e);
				this.shutdown();
				throw new YapProverException("Computation of yes/no query \"" + goal + "\" on Yap Prolog side couldn't be completed.", e);
			} catch (InterruptedException e) {
				// If the thread using this Yap Prover has been interrupted, still throw an exception
				// cause the query couldn't be computed, but also re-set the interrupted status of the
				// current thread to "true" so that also the callers of this method know that they have
				// to interrupt.
				LOGGER.error("[StateMachine] [YAP] [YapProver] Impossible to complete the computation of yes/no query on Yap Prolog side.", e);
				this.shutdown();
				Thread.currentThread().interrupt();
				throw new YapProverException("Computation of yes/no query \"" + goal + "\" on Yap Prolog side couldn't be completed.", e);

			}

		// If no positive waiting time has been set just wait indefinitely.
		}else{
			try{
				success = this.yapProver.deterministicGoal(goal);
			// Catch all possible exceptions of Interprolog and re-throw them as a YapPrologException
			// to signal that something went wrong and the query couldn't be answered.
			}catch(RuntimeException e){
				LOGGER.error("[StateMachine] [YAP] [YapProver] Impossible to complete the computation of yes/no query on Yap Prolog side.", e);
				this.shutdown();
				throw new YapProverException("Computation of yes/no query \"" + goal + "\" on Yap Prolog side couldn't be completed.", e);
			}
		}

		return success;

	}

	public boolean isReady(){
		return this.isReady();
	}

}