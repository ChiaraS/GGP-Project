/**
 *
 */
package org.ggp.base.util.statemachine;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * This state machine initializes the internal state machine in the given time limit.
 *
 * For now it is only useful to impose a limit on the initialization time of the
 * FwdInterrPropnetStateMachine so that we can use it in the tests when we want to
 * wrap it externally with the CachedStateMachine.
 *
 * @author C.Sironi
 *
 */
public class InitializationControlStateMachine extends StateMachine {

	/**
	 * This class calls the initialization method of the given state machine.
	 * @author C.Sironi
	 *
	 */
	class Initializer implements Callable<Boolean>{

		private List<Gdl> description;

		public Initializer(List<Gdl> description){
			this.description = description;
		}

		@Override
		public Boolean call() throws Exception {
			theRealMachine.initialize(description);
			return true;

		}
	}

	/**
	 * The state machine that this state machine is wrapping.
	 */
	private StateMachine theRealMachine;

	/**
	 * The time (in milliseconds) that this state machine has available to initialize the internal state machine.
	 */
	private long initTime;


	public InitializationControlStateMachine(StateMachine theRealMachine, long initTime) {
		this.theRealMachine = theRealMachine;
		this.initTime = initTime;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#initialize(java.util.List)
	 */
	@Override
	public void initialize(List<Gdl> description)
			throws StateMachineInitializationException {

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Initializer initializer = new Initializer(description);

		try {
			executor.invokeAny(Arrays.asList(initializer), initTime, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			GamerLogger.logError("StateMachine", "[INIT CONTROL] Impossible to initialize the state machine in the given amount of time.");
			throw new StateMachineInitializationException("Initialization of state machine failed!", e);
		}finally{
			// Reset executor and initializer
			executor.shutdownNow();

			// TODO: ACTUALLY WE COULD AVOID THE TERMINATION CHECK: if the initialization succeeded then the task will
			// be already terminated, if it failed this state machine cannot be used anyway, so if the task takes a
			// while to figure out it had been interrupted we don't care (except that it might still try to log something
			// and create a conflict with the rest of the code and some logs might get lost).
			if(!executor.isTerminated()){
				// If not all tasks terminated, wait for a minute and then check again
				try {
					executor.awaitTermination(1, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					GamerLogger.logError("StateMachine", "[INIT CONTROL] Interrupted while waiting for termination of executor.");
					GamerLogger.logStackTrace("StateMachine", e);
				}
			}

			executor = null;
			initializer = null;
		}

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getGoal(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public int getGoal(MachineState state, Role role)
			throws GoalDefinitionException, StateMachineException {
		return this.theRealMachine.getGoal(state, role);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#isTerminal(org.ggp.base.util.statemachine.MachineState)
	 */
	@Override
	public boolean isTerminal(MachineState state) throws StateMachineException {
		return this.theRealMachine.isTerminal(state);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getRoles()
	 */
	@Override
	public List<Role> getRoles() {
		return this.theRealMachine.getRoles();
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getInitialState()
	 */
	@Override
	public MachineState getInitialState() {
		return this.theRealMachine.getInitialState();
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getLegalMoves(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public List<Move> getLegalMoves(MachineState state, Role role)
			throws MoveDefinitionException, StateMachineException {
		return this.theRealMachine.getLegalMoves(state, role);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getNextState(org.ggp.base.util.statemachine.MachineState, java.util.List)
	 */
	@Override
	public MachineState getNextState(MachineState state, List<Move> moves)
			throws TransitionDefinitionException, StateMachineException {
		return this.theRealMachine.getNextState(state, moves);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#shutdown()
	 */
	@Override
	public void shutdown() {
		this.theRealMachine.shutdown();
	}

	@Override
	public String getName(){
		return "INIT_CONTROL(" + this.theRealMachine.getName() + ")";
	}

}
