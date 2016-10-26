/**
 *
 */
package org.ggp.base.util.statemachine.safe;

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
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

/**
 * This state machine initializes the internal state machine in the given time limit.
 * Makes sense to use this state machine to initialize other state machine whose
 * initialization method ignores the timeout parameter. If you use it for state
 * machines that already check the timeout limit then this state machine will be
 * redundant.
 *
 * For now it is only useful to impose a limit on the initialization time of the
 * FwdInterrPropnetStateMachine so that we can use it in the tests or when we want
 * to wrap it externally with the CachedStateMachine.
 *
 * @author C.Sironi
 *
 */
public class InitializationSafeStateMachine extends StateMachine {

	/**
	 * This class calls the initialization method of the given state machine.
	 * @author C.Sironi
	 *
	 */
	class Initializer implements Callable<Boolean>{

		private List<Gdl> description;

		private long timeout;

		public Initializer(List<Gdl> description, long timeout){
			this.description = description;
			this.timeout = timeout;
		}

		@Override
		public Boolean call() throws Exception {
			theRealMachine.initialize(description, timeout);
			return true;

		}
	}

	/**
	 * The state machine that this state machine is wrapping.
	 */
	private StateMachine theRealMachine;

	public InitializationSafeStateMachine(StateMachine theRealMachine) {
		this.theRealMachine = theRealMachine;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#initialize(java.util.List)
	 */
	@Override
	public void initialize(List<Gdl> description, long timeout)
			throws StateMachineInitializationException {

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Initializer initializer = new Initializer(description, timeout);

		long initTime = timeout - System.currentTimeMillis();

		if(initTime <= 0){
			throw new StateMachineInitializationException("Initialization of state machine failed! No time available to complete initialization!");
		}

		try {
			executor.invokeAny(Arrays.asList(initializer), initTime, TimeUnit.MILLISECONDS);
		} catch (ExecutionException | TimeoutException e) {
			GamerLogger.logError("StateMachine", "[INIT SAFE] Impossible to initialize the state machine in the given amount of time.");
			if(this.theRealMachine != null){
				this.theRealMachine.shutdown();
			}
			throw new StateMachineInitializationException("Initialization of state machine failed!", e);
		} catch (InterruptedException e) {
			GamerLogger.logError("StateMachine", "[INIT SAFE] Impossible to initialize the state machine in the given amount of time.");
			if(this.theRealMachine != null){
				this.theRealMachine.shutdown();
			}
			Thread.currentThread().interrupt();
			throw new StateMachineInitializationException("Initialization of state machine failed!", e);
		}finally{
			// Reset executor and initializer
			executor.shutdownNow();

			// TODO: ACTUALLY WE COULD AVOID THE TERMINATION CHECK: if the initialization succeeded then the task will
			// be already terminated, if it failed this state machine cannot be used anyway, so if the task takes a
			// while to figure out it had been interrupted we don't care (except that it might still try to log something
			// and create a conflict with the rest of the code and some logs might get lost).
			while(!executor.isTerminated()){
				// If not all tasks terminated, wait for a minute and then check again
				try {
					executor.awaitTermination(1, TimeUnit.MINUTES);
				} catch (InterruptedException e) { // If this exception is thrown it means the thread that is executing the initialization
					// of the InitializationSafeStateMachine has been interrupted. If we do nothing this state machine will be stuck in the
					// while loop anyway until all tasks in the executor have terminated, thus we break out of the loop throwing a new exception.
					// What happens to the still running tasks in the executor? Who will make sure they terminate?
					GamerLogger.logError("StateMachine", "[INIT SAFE] Interrupted while waiting for termination of executor.");
					GamerLogger.logStackTrace("StateMachine", e);
					Thread.currentThread().interrupt();
					throw new StateMachineInitializationException("Initialization of state machine failed!", e);
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
	public List<Integer> getOneRoleGoals(ExplicitMachineState state, ExplicitRole role)
			throws StateMachineException {
		// We do not check if the real state machine is null, because if initialization succeeded this
		// cannot happen and if initialization failed this state machine is not supposed to be used.
		return this.theRealMachine.getOneRoleGoals(state, role);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#isTerminal(org.ggp.base.util.statemachine.MachineState)
	 */
	@Override
	public boolean isTerminal(ExplicitMachineState state) throws StateMachineException {
		return this.theRealMachine.isTerminal(state);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getRoles()
	 */
	@Override
	public List<ExplicitRole> getRoles() {
		return this.theRealMachine.getRoles();
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getInitialState()
	 */
	@Override
	public ExplicitMachineState getInitialState() {
		return this.theRealMachine.getInitialState();
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getLegalMoves(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public List<ExplicitMove> getLegalMoves(ExplicitMachineState state, ExplicitRole role)
			throws MoveDefinitionException, StateMachineException {
		return this.theRealMachine.getLegalMoves(state, role);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#getNextState(org.ggp.base.util.statemachine.MachineState, java.util.List)
	 */
	@Override
	public ExplicitMachineState getNextState(ExplicitMachineState state, List<ExplicitMove> moves)
			throws TransitionDefinitionException, StateMachineException {
		return this.theRealMachine.getNextState(state, moves);
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.util.statemachine.StateMachine#shutdown()
	 */
	@Override
	public void shutdown() {
		if(this.theRealMachine != null){
			this.theRealMachine.shutdown();
		}
	}

	public StateMachine getTheRealMachine() {
		return this.theRealMachine;
    }

	@Override
    public String getName() {
        if(this.theRealMachine != null) {
            return "InitializationSafe(" + this.theRealMachine.getName() + ")";
        }
        return "InitializationSafe(null)";
    }

}
