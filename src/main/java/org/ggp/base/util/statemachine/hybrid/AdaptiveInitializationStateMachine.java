package org.ggp.base.util.statemachine.hybrid;

import java.util.List;

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
 * machine between the one based on the Propnet, the one based on YAP and the one based on the
 * GGP Base Prover and all of them are supposed never to fail answering a query by throwing the
 * StateMachineException.
 * On the other hand, if any of the state machines fails to answer to a query by throwing one
 * among the GoalDefinitionException, TransitionDefinitionException or MoveDefinitionException,
 * also all the other state machines will throw the same exception for the same query, since this
 * failure doesn't depend on the implementation of the state machine, but on the definition of
 * the GDL description or the inappropriate use of the state machine.
 * Moreover, if it turns out that the YAP state machine is faster than the GGP Base prover state
 * machine for a certain game, it's useless to ask to the GGP Base prover state machine to answer
 * to a query if the YAP state machine fails, because the YAP state machine is already backed up
 * by the GGP Base prover state machine internally.
 *
 * NOTE that this class can be used with an arbitrary number of state machines
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
		 * True if the result of the test (i.e. the speed in nodes/second of the state
		 * machine) has been computed correctly (e.g. the initialization didn't fail,
		 * the thread stopped correctly,...)
		 */
		//private boolean testSuccess;

		/**
		 * True if the initialization of the state machine succeeded and the state machine can be used.
		 */
		private boolean initSuccess;

		/**
		 * The number of nodes per second that this state machine can visit
		 */
		private double nodesPerSecond;

		public StateMachineTester(List<Gdl> description, StateMachine theMachine){
			this.description = description;
			this.theMachine = theMachine;
			//this.testSuccess = false;
			this.initSuccess = false;
			this.nodesPerSecond = 0;
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
				this.theMachine.initialize(description);

				this.initSuccess = true;

				int[] lastIterationVisitedNodes = new int[1];

				int totalVisitedNodes = 0;

				MachineState initialState = theMachine.getInitialState();

				long startTime = System.currentTimeMillis();

				while(!Thread.currentThread().isInterrupted()){

					try {
						theMachine.interruptiblePerformDepthCharge(initialState, lastIterationVisitedNodes);
						totalVisitedNodes += lastIterationVisitedNodes[0];
					} catch (TransitionDefinitionException
							| MoveDefinitionException | StateMachineException e) {
						GamerLogger.logError("StateMachineTester-" + this.theMachine.getName(), "Monte Carlo iteration failed. Discarding its results.");
						GamerLogger.logStackTrace("StateMachineTester-" + this.theMachine.getName(), e);
					}catch (Exception e) { // Keep all other exception separate from the typical exceptions of the state machine (even if now they are all dealt with in the same way)
						GamerLogger.logError("StateMachineTester-" + this.theMachine.getName(), "Monte Carlo iteration failed. Discarding its results.");
						GamerLogger.logStackTrace("StateMachineTester-" + this.theMachine.getName(), e);
					}catch (Error e) {
						GamerLogger.logError("StateMachineTester-" + this.theMachine.getName(), "Monte Carlo iteration failed. Discarding its results.");
						GamerLogger.logStackTrace("StateMachineTester-" + this.theMachine.getName(), e);
					}
				}

				long totalTime = System.currentTimeMillis() - startTime;
				if(totalTime > 0){
					this.nodesPerSecond = ((double) totalVisitedNodes * 1000)/((double) (System.currentTimeMillis() - startTime));
				}// else the nodes per second are left set to 0
				//this.testSuccess = true;

			} catch (StateMachineInitializationException e) {
				// Note that since probably there are other threads of the same type running, this thread must write logs
				// on its personal log file or it will create conflicts with other threads trying to log on the same file.
				// We assume that the file name is unique for this thread since it doesn't make sense to give to the
				// AdaptiveInitializationStateMachine the same state machine to check twice. If you want to give the same
				// state machine twice because the two instances have for example different settings remember to change the
				// name of the log file so that it will be unique for each thread.
				GamerLogger.logError("StateMachineTester-" + this.theMachine.getName(), "Initialization of the state machine failed. Cannot test its speed.");
				GamerLogger.logStackTrace("StateMachineTester-" + this.theMachine.getName(), e);
				//this.testSuccess = false;
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
	 * Time by when this state machine must have completed initialization
	 */
	private long initializeBy;

	public AdaptiveInitializationStateMachine(StateMachine[] allTheMachines, long initializeBy) {

		if(this.allTheMachines == null || this.allTheMachines.length < 2){
			throw new IllegalArgumentException("Expected at least two state machines to compare in this AdaptiveInitializationStateMachine.");
		}

		this.theFastestMachine = null;
		this.allTheMachines = allTheMachines;
		this.initializeBy = initializeBy;
	}

	@Override
	public void initialize(List<Gdl> description)
			throws StateMachineInitializationException {

		StateMachineTester[] testers = new StateMachineTester[allTheMachines.length];

		// TODO: If I only have one state machine I could avoid testing its speed, but if I do that I have no method
		// that checks for its initialization to be time limited --> separate initialization from speed check????????
		for(int i = 0; i < allTheMachines.length; i++){
			testers[i] = new StateMachineTester(description, this.allTheMachines[i]);
		}

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

}
