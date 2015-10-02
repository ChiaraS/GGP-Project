package org.ggp.base.util.statemachine.hybrid;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * This state machine gets as input a list of different state machines and chooses to use the
 * one that can initialize in time and seems the fastest (i.e. the one that visits more nodes
 * per second when performing Monte Carlo simulations) for the current game.
 *
 * REMARK: once the fastest machine has been identified, the other machines are discarded,
 * however it could be possible to keep them ordered from fastest to slowest so that when
 * the fastest state machine fails to answer to a query, the state machine immediately after
 * that one could be queried.
 * For now this has not been done because this class is used only to test the fastest state
 * machine between the one based on the Propnet, the one based on YAP backed by the GGP Base
 * Prover and the one based on the GGP Base Prover and all of them are supposed never to fail
 * answering a query by throwing the StateMachineException.
 * On the other hand, if any of the state machines fails to answer to a query by throwing one
 * among the GoalDefinitionException, TransitionDefinitionException or MoveDefinitionException,
 * also all the other state machines will throw the same exception for the same query, since this
 * failure doesn't depend on the implementation of the state machine, but on the definition of
 * the GDL description or the inappropriate use of the state machine.
 * Moreover, if it turns out that the backed YAP state machine is faster than the GGP Base prover
 * state machine for a certain game, it's useless to ask to the GGP Base prover state machine to
 * answer to a query if the YAP state machine fails, because the YAP state machine is already
 * backed up by the GGP Base prover state machine internally.
 *
 * NOTE that this class can be used with an arbitrary number of state machines.
 *
 * IMPORTANT NOTE: this class can be used only with sub-state machines that correctly handle thread
 * interruption. If even only one of them doesn't then this state machine might take an infinite
 * amount of time to initialize or not be able to return consistent results when queried.
 *
 * @author C.Sironi
 *
 */
public class AdaptiveInitializationStateMachine extends StateMachine {

	/**
	 * This class calls the initialization method of the given state machine
	 * and then computes the number of visited nodes when performing Monte
	 * Carlo simulations.
	 *
	 * @author C.Sironi
	 *
	 */
	class StateMachineTester extends Thread{

		/**
		 * The description of the game with which this StateMachineTester must try
		 * to initialize the given state machine.
		 */
		private List<Gdl> description;

		/**
		 * The state machine that this StateMachineTester must try to initialize and
		 * for which this StateMachineTester must compute the speed.
		 */
		private StateMachine theMachine;

		/**
		 * The time by when the state machine initialized by this StateMachineTester must
		 * finish initialization.
		 */
		private long timeout;

		/**
		 * True if the result of the test (i.e. the speed in nodes/second of the state
		 * machine) has been computed correctly (e.g. the initialization didn't fail,
		 * the thread stopped correctly,...).
		 */
		//private boolean testSuccess;

		/**
		 * True if the initialization of the state machine succeeded and the state machine can be used.
		 */
		private boolean initSuccess;

		/**
		 * The number of nodes per second that this state machine can visit.
		 */
		private double nodesPerSecond;

		public StateMachineTester(List<Gdl> description, StateMachine theMachine, long timeout){
			this.description = description;
			this.theMachine = theMachine;
			this.timeout = timeout;
			//this.testSuccess = false;
			this.initSuccess = false;
			this.nodesPerSecond = 0.0;
		}


		public double getNodesPerSecond(){
			return this.nodesPerSecond;
		}

		/*
		public boolean isSucceeded(){
			return this.testSuccess;
		}*/

		public boolean initSucceeded(){
			return this.initSuccess;
		}

		public StateMachine getTheMachine(){
			return this.theMachine;
		}

		/**
		 * This method tries to initialize the state machine and if initialization succeeded
		 * it computes how many nodes per second the state machine can visit running Monte
		 * Carlo simulations until it is interrupted.
		 */
		@Override
		public void run(){
			try {

				//System.out.println("[DEBUG-T] [" + getCurrentDate() + "] Initializing submachine.");

				this.theMachine.initialize(description, timeout);

				//System.out.println("[DEBUG-T] [" + getCurrentDate() + "] Done initializing submachine.");

				this.initSuccess = true;

				int[] lastIterationVisitedNodes = new int[1];

				int totalVisitedNodes = 0;

				MachineState initialState = theMachine.getInitialState();

				long startTime = System.currentTimeMillis();

				//System.out.println("[DEBUG-T] [" + getCurrentDate() + "] Starting speed tests.");

				while(!Thread.currentThread().isInterrupted()){

					theMachine.interruptiblePerformDepthCharge(initialState, lastIterationVisitedNodes);
					totalVisitedNodes += lastIterationVisitedNodes[0];

				}

				//System.out.println("[DEBUG-T] [" + getCurrentDate() + "] Speed test done.");

				long totalTime = System.currentTimeMillis() - startTime;
				if(totalTime > 0){
					this.nodesPerSecond = ((double) totalVisitedNodes * 1000)/((double) (System.currentTimeMillis() - startTime));
				}// else the nodes per second are left set to 0
				//this.testSuccess = true;

			} catch (StateMachineInitializationException e) {

				//System.out.println("[DEBUG-T] [" + getCurrentDate() + "] Failed initializing submachine.");

				// Note that since probably there are other threads of the same type running, this thread must write logs
				// on its personal log file or it will create conflicts with other threads trying to log on the same file.
				// We could assume that the file name is unique for this thread if we used in it the name of the state
				// machine being tested. However, it is possible that you want to give two instances of the same state
				// machine twice because the two instances have, for example, different settings. So to be safe the name
				// of the log file will also include the unique id of the thread testing the state machine --> the GamerLogger
				// will take care of this.
				GamerLogger.logError("StateMachineTester-" + this.theMachine.getName(), "Initialization of the state machine failed. Cannot test its speed.");
				GamerLogger.logStackTrace("StateMachineTester-" + this.theMachine.getName(), e);
				this.theMachine.shutdown();
				this.initSuccess = false;
			}
		}
	}

	/**
	 * Main state machine that this state machine must use to answer queries about the game.
	 */
	private StateMachine theFastestMachine;

	/**
	 *
	 */
	private StateMachine[] allTheMachines;

	/**
	 * Time (in millisecond) that this state machine should keep as a safety margin, meaning that this
	 * state machine should aim at finishing initialization by (timeout - safteyMargin), so that
	 * if it takes safetyMargin extra milliseconds to initialize, it will still be in time.
	 */
	private long safetyMargin;

	public AdaptiveInitializationStateMachine(StateMachine[] allTheMachines) {

		this(allTheMachines, 0L);

	}

	public AdaptiveInitializationStateMachine(StateMachine[] allTheMachines, long safetyMargin) {

		if(allTheMachines == null || allTheMachines.length < 2){
			throw new IllegalArgumentException("Expected at least two state machines to compare in this AdaptiveInitializationStateMachine.");
		}

		this.theFastestMachine = null;
		this.allTheMachines = allTheMachines;
		this.safetyMargin = safetyMargin;
	}

	@Override
	public void initialize(List<Gdl> description, long timeout)
			throws StateMachineInitializationException {

		//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] Starting initialization.");

		long initializeBy = timeout - safetyMargin;

		long currentTime = System.currentTimeMillis();

		//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] We have " + (timeout - currentTime) + "ms to initialize the state machine subtracting a safety margin of " + safetyMargin + "ms.");

		GamerLogger.log("StateMachine", "Available time for initialization: " + (timeout - currentTime) + "ms from which a safety margin of " + safetyMargin + "ms must be subtracted.");

		if(initializeBy <= currentTime){
			GamerLogger.logError("StateMachine", "Impossible to initialize the state machine in " + (timeout - currentTime) + "ms respecting the safety margin of " + safetyMargin + "ms.");
			throw new StateMachineInitializationException("Initialization failed. Impossible to initialize the state machine in " + (timeout - currentTime) + "ms respecting the safety margin of " + safetyMargin + "ms.");
		}

		// Create the executor as a pool with the number of threads that equals the number of state machines to test.
		ExecutorService executor = Executors.newFixedThreadPool(this.allTheMachines.length);

		StateMachineTester[] testers = new StateMachineTester[allTheMachines.length];

		for(int i = 0; i < allTheMachines.length; i++){

			//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] Creating tester for state machine " + this.allTheMachines[i].getName() + ".");

			// If the state machines being tested also have a check for the timeout during initialization,
			// give them the same timeout this state machine is using, i.e. the one with the safety margin.
			testers[i] = new StateMachineTester(description, this.allTheMachines[i], initializeBy);

			//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] Starting tester.");

			executor.execute(testers[i]);
		}

		//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] Closing executor.");

		// Shutdown executor to tell it not to accept any more task to execute.
		// Note that this doesn't interrupt previously started tasks.
		executor.shutdown();

		// Tell the executor to wait until all currently running tasks have completed execution or the timeout has elapsed.
		// We should wait until the initializeBy timeout elapsed and then interrupt the tasks that are checking the speed.
		// However, since it is possible that all of them already failed initializing the state machines way earlier than
		// initializeBy time is reached, it is useless to keep waiting, that is why we use the awaitTermination method with
		// the timeout so that we wait for the given amount of time before interrupting the tasks unless they all terminated
		// and we can already proceed with the execution.
		boolean allTasksTerminated = false;
		try {

			//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] Waiting for timeout to elapse.");

			allTasksTerminated = executor.awaitTermination(initializeBy - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

			//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] All task terminated? " + allTasksTerminated);

		} catch (InterruptedException e) {
			executor.shutdownNow(); // Interrupt everything
			GamerLogger.logError("StateMachine", "[ADAPTIVE_INIT] Initialization interrupted before completion.");
			GamerLogger.logStackTrace("StateMachine", e);
			Thread.currentThread().interrupt();
			throw new StateMachineInitializationException("State machine initialization failed. Initialization interrupted before completion (while running speed tests).");
		}

		//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] Interrupting executor's tasks.");

		// Here the available time has elapsed, so we must interrupt the threads that are still running (or they will go on
		// checking the speed forever).
		executor.shutdownNow(); // This instruction interrupts all threads.

		// Wait for all threads to actually terminate
		while(!executor.isTerminated()){

			//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] Waiting for termination.");

			// If not all tasks terminated, wait for a minute and then check again
			try {
				executor.awaitTermination(1, TimeUnit.MINUTES);
			} catch (InterruptedException e) {// If this exception is thrown it means the thread that is executing the initialization
				// of the InitializationSafeStateMachine has been interrupted. If we do nothing this state machine will be stuck in the
				// while loop anyway until all tasks in the executor have terminated, thus we break out of the loop throwing a new exception.
				// What happens to the still running tasks in the executor? Who will make sure they terminate?
				GamerLogger.logError("StateMachine", "[ADAPTIVE_INIT] Initialization interrupted before completion (while waiting for all tasks to interrupt).");
				GamerLogger.logStackTrace("StateMachine", e);
				Thread.currentThread().interrupt();
				throw new StateMachineInitializationException("State machine initialization failed. Impossible to initialize the AdaptiveInitializationStateMachine!");
			}
		}

		if(executor.isTerminated()){
			//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] All tasks terminated. Looking for faster machine.");
		}else{
			//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] Some tasks still running. Looking for faster machine anyway.");
		}

		// Check which state machine has the maximum nodes/second value.
		double maxSpeed = -1.0;
		for(StateMachineTester tester : testers){

			//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] State machine: " + tester.getTheMachine().getName() +".");

			//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] Success: " + tester.initSucceeded() +".");

			// If initialization failed, don't even check
			if(tester.initSucceeded()){
				double currentMachineSpeed = tester.getNodesPerSecond();
				//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] Nodes per second: " + tester.getNodesPerSecond() +".");
				if(currentMachineSpeed > maxSpeed){
					maxSpeed = currentMachineSpeed;
					this.theFastestMachine = tester.getTheMachine();
				}
			}
		}

		//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] Shutting down other machines.");

		this.shutdownOtherMachines();

		this.allTheMachines = null; // Maybe this will free some memory

		// If no state machine is available, throw an exception to say it's impossible to complete initialization
		// (this happens only if all of the given state machines failed initialization, if there is at least a state
		// machine for which initialization succeeded then that one will be selected even if its speed is 0 or very slow)
		if(this.theFastestMachine == null){
			GamerLogger.logError("StateMachine", "[ADAPTIVE_INIT] Impossible to initialize the state machine. Initialization of all given sub-machines failed.");
			throw new StateMachineInitializationException("State machine initialization failed. Impossible to initialize any of the given sub-machines.");
		}

		//System.out.println("[DEBUG] [" + this.getCurrentDate() + "] Fastest machine: " + (this.theFastestMachine == null ? "null" : this.theFastestMachine.getName()) + ".");
	}

	@Override
	public int getGoal(MachineState state, Role role)
			throws GoalDefinitionException, StateMachineException {
		return this.theFastestMachine.getGoal(state, role);
	}

	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException {
		return this.theFastestMachine.isTerminal(state);
	}

	@Override
	public List<Role> getRoles() {
		return this.theFastestMachine.getRoles();
	}

	@Override
	public MachineState getInitialState() {
		return this.theFastestMachine.getInitialState();
	}

	@Override
	public List<Move> getLegalMoves(MachineState state, Role role)
			throws MoveDefinitionException, StateMachineException {
		return this.theFastestMachine.getLegalMoves(state, role);
	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves)
			throws TransitionDefinitionException, StateMachineException {
		return this.theFastestMachine.getNextState(state, moves);
	}

	@Override
	public void shutdown() {

		if(this.theFastestMachine != null){
			this.theFastestMachine.shutdown();
		}

		if(this.allTheMachines != null){
			shutdownOtherMachines();
		}
	}

	private void shutdownOtherMachines(){
		for(StateMachine machine : this.allTheMachines){
			if(machine != null && machine != this.theFastestMachine){
				machine.shutdown();
			}
		}
	}


	@Override
	public String getName(){
		// Note that if initialization succeeded this never happens
		if(this.theFastestMachine != null){
			return "ADAPTIVE_INIT(" + this.theFastestMachine.getName() + ")";
		}else{
			return "ADAPTIVE_INIT(null)";
		}
	}

	public StateMachine getFastestMachine(){
		return this.theFastestMachine;
	}


	private String getCurrentDate(){
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    return strDate;
	}

}
