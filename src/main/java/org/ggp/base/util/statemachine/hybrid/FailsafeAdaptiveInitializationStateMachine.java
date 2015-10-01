package org.ggp.base.util.statemachine.hybrid;

import org.ggp.base.util.statemachine.StateMachine;

/**
 * Same as the AdaptiveInitializationStateMachine, but instead of keeping only the fastest
 * state machine, this one also keeps the other state machines, ordering them from fastest
 * to slowest, so that whenever a state machine fails, the one right after it in the ordered
 * list can be queried.
 *
 * This state machine can be used when there is a chance that a state machine fails throwing
 * a StateMachineException, but another state machine on the same query might succeed.
 * And moreover, it makes sense to use this state machine only when we are sure that the given
 * state machines are not already internally backed by each other (otherwise useless computations
 * might be performed asking twice to the same state machine to backup a certain state machine).
 *
 * @author C.Sironi
 *
 */
public class FailsafeAdaptiveInitializationStateMachine extends
		AdaptiveInitializationStateMachine {

	public FailsafeAdaptiveInitializationStateMachine(
			StateMachine[] allTheMachines, long initializeBy) {
		super(allTheMachines, initializeBy);
		// TODO Auto-generated constructor stub
	}

}
