package ggpbasebenchmark;

import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;

public abstract class SearchAlgorithm {

	protected long nbLegals;
	protected long nbUpdates;
	protected long nbGoals;
	private int playclock;
	protected StateMachine stateMachine;

	public SearchAlgorithm(int playclock) {
	}

	public SearchAlgorithm(StateMachine stateMachine, int playclock) {
		this.stateMachine = stateMachine;
		this.playclock = playclock;
	}

	public void run(final ProverMachineState state) {
		reset();
		if (playclock == Integer.MAX_VALUE) {
			doSearch(state);
		} else {
			final Object notifier = new Object();
			Thread t = new Thread() {
				@Override
				public void run() {
					doSearch(state);
					synchronized (notifier) {
						notifier.notifyAll();
					}
				}
			};
			t.start();
			try {
				synchronized (notifier) {
					notifier.wait(playclock*1000); // wait until time is up or thread is finished
				}
				// time is up -> interrupt thread
				t.interrupt();

				// Added this instruction to make sure that the thread t has really terminated execution
				// and the runTrace() method won't use the state machine before this thread is done using it.
				t.join();
			} catch (InterruptedException e) {
				// thread finished and thus our notifier.wait(...) call got interrupted
				// or this thread has been interrupted while waiting for t to terminate execution
			}
		}
	}

	public abstract void doSearch(ProverMachineState state);

	public void reset() {
		nbLegals = 0;
		nbUpdates = 0;
		nbGoals = 0;
	}

	public long getNbLegals() {
		return nbLegals;
	}

	public long getNbUpdates() {
		return nbUpdates;
	}

	public long getNbGoals() {
		return nbGoals;
	}

	public int getPlayclock() {
		return playclock;
	}

	public void setPlayclock(int playclock) {
		this.playclock = playclock;
	}

	public void evaluateGoals(ProverMachineState state) throws StateMachineException {
		try {
			stateMachine.getGoals(state);
			++nbGoals;
		} catch (GoalDefinitionException e) {
			e.printStackTrace();
		}
	}

	public void evaluateGoals(InternalPropnetMachineState internalState) throws StateMachineException {
		try {
			((InternalPropnetStateMachine) this.stateMachine).getGoals(internalState);
			++nbGoals;
		} catch (GoalDefinitionException e) {
			e.printStackTrace();
		}
	}

	public boolean timeout() {
		return Thread.currentThread().isInterrupted();
	}

}
