package org.ggp.base.util.statemachine.implementation.yapProlog.prover;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.implementation.yapProlog.transform.YapEngineSupport;

import com.declarativa.interprolog.YAPSubprocessEngine;

/**
 * This class acts like an interface between the state machine and Yap Prolog.
 * When the state machine uses this class it's like it is using Yap Prolog
 * without having to take care of starting it or dealing with it when it is
 * not responding.
 * The state machine can call this class as if it was the real Yap Prolog prover
 * without worrying about the details of setting it up.
 *
 * This class is a sort of fake Yap Prolog prover that asks the real Yap Prolog
 * prover to answer queries.
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
 *
 * @author C.Sironi
 *
 */
public class YapProver {

	/**
	 * Class that represents a query that returns some result in the form of a binding.
	 *
	 * @author C.Sironi
	 *
	 */
	public class CallableBindingsQuery implements Callable<Object[]>{

		private String goal;
		private String resVar;

		public CallableBindingsQuery(String goal, String resVar){
			this.goal = goal;
			this.resVar = resVar;
		}

		public void setQuery(String goal, String resVar){
			this.goal = goal;
			this.resVar = resVar;
		}

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

		private String goal;

		public CallableYesNoQuery(String goal){
			this.goal = goal;
		}

		public void setQuery(String goal){
			this.goal = goal;
		}

		@Override
		public Boolean call() throws Exception {
			return yapProver.deterministicGoal(this.goal);
		}

	}


	/**
	 * Command that this class must use to run Yap.
	 */
	private String yapCommand;

	/**
	 * Yap engine that this class must use to answer queries.
	 */
	private YAPSubprocessEngine yapProver;

	// Path of the file where to put the game description
	private String descriptionFilePath;

	// File with all the predefined Prolog functions
	private String functionsFilePath;

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

	private CallableBindingsQuery bindingsQuery;

	private CallableYesNoQuery yesNoQuery;


	public YapProver() {
		// TODO Auto-generated constructor stub
		flushAndWrite(support.toProlog(description));
	}

	public void start(){

		//try{
			// Create the bridge between Java and YAP Prolog, trying to start the YAP Prolog program.
			this.yapProver = new YAPSubprocessEngine(this.yapCommand);

			this.executor = Executors.newSingleThreadExecutor();

			this.yapProver.consultAbsolute(new File(functionsFilePath));

			randomizeProlog();

		}catch(RuntimeException re){
			throw re;
		}catch(Exception e){
			// Log the exception
			GamerLogger.logError("StateMachine", "[YAP] Exception during state machine initialization. Shutting down.");
			GamerLogger.logStackTrace("StateMachine", e);

			// Reset all the variables of the state machine to null to leave the state machine in a consistent
			// state, since initialization failed.
			this.roles = null;
			this.fakeRoles = null;
			this.currentYapState = null;
			this.initialState = null;
			// Shutdown Yap Prolog and remove the reference to it, as it is now unusable.
			this.yapProver.shutdown();
			this.yapProver = null;
			// Shutdown the executor
			this.executor.shutdownNow();
			this.executor = null;

			// Throw an exception.
			throw new StateMachineException("State machine initialization failure.", e);
		}

	}

	public void shutdown(){
		this.yapProver.shutdown();
		this.executor.shutdownNow();
		// If the executor gives any concurrency problem use this instruction
		// to wait for all threads to actually die and if they don't do something
		// to deal with it (i.e. throw exception or log this as it is probably a
		// programming fault since all the query threads are supposed to terminate
		// after interruption).
		//this.executor.awaitTermination(timeout, unit);
		this.yapProver = null;
		this.executor = null;
		this.bindingsQuery = null;
		this.yesNoQuery = null;
	}

	public void restart(){
		this.shutdown();
		this.start();
	}


	public Object[] askQueryResults(String goal, String resVar) throws YapProverException{

		Object[] bindings = null;

		// If a positive waiting time has been set, give a timeout to the query.
		if(this.waitingTime > 0){

			// Change the goal of the query task to be executed by the executor.
			this.bindingsQuery.setQuery(goal, resVar);

			try {
				// Try to query Yap Prolog and wait for an answer till timeout has been reached.
				bindings = this.executor.invokeAny(Arrays.asList(this.bindingsQuery),this.waitingTime, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException
					| TimeoutException e) {
				// If something went wrong or timeout has been reached, then throw an exception.
				GamerLogger.logError("StateMachine", "[YapProver] Computation of query result on Yap Prolog side failed.");
				GamerLogger.logStackTrace("StateMachine", e);
				this.restart();
				throw new YapProverException("Computation of query \"" + goal + "\" with result variables \"" + resVar + "\" on Yap Prolog side failed.", e);
			}
		// If no positive waiting time has been set just wait indefinitely.
		}else{
			// TODO: also catch runtime exceptions of interprolog here and rethrow yapProverException???
			bindings = this.yapProver.deterministicGoal(goal, resVar);
		}

		return bindings;

	}

	public boolean askQuerySuccess(String goal) throws YapProverException{

		boolean success = false;

		// If a positive waiting time has been set, give a timeout to the query.
		if(this.waitingTime > 0){

			// Change the goal of the query task to be executed by the executor.
			this.yesNoQuery.setQuery(goal);

			try {
				// Try to query Yap Prolog and wait for an answer till timeout has been reached.
				success = this.executor.invokeAny(Arrays.asList(this.yesNoQuery),this.waitingTime, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException
					| TimeoutException e) {
				// If something went wrong or timeout has been reached, then throw an exception.
				GamerLogger.logError("StateMachine", "[YapProver] Computation of yes/no query on Yap Prolog side failed.");
				GamerLogger.logStackTrace("StateMachine", e);
				this.restart();
				throw new YapProverException("Computation of yes/no query \"" + goal + "\" on Yap Prolog side failed.", e);
			}
		// If no positive waiting time has been set just wait indefinitely.
		}else{
			// TODO: also catch runtime exceptions of interprolog here and rethrow yapProverException???
			// Direi che ha senso visto che questa classe fa da filtro tra la state machine e yap: la state
			// machine nn dovrebbe sapere neanche dellésistenza dle vero yap
			success = this.yapProver.deterministicGoal(goal);
		}

		return success;

	}

}