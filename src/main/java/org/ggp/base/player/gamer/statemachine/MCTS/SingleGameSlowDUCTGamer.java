/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;

import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

/**
 * This class behaves exactly like the SlowDUCTGamer, but it doesn't create a new state machine
 * with a new propnet for every match. This gamer assumes to be playing always the same game and
 * thus always uses the same propnet. It is useful only to perform tests, where the same game
 * is played for a high amount of times in a row.
 *
 * @author C.Sironi
 *
 */
public class SingleGameSlowDUCTGamer extends SlowDUCTGamer {

	/**
	 * True if this gamer never tried to build a propnet before.
	 * It is used when returning the initial state machine. If this gamer has no
	 * state machine it might be because it is the first time the player is being used
	 * or because it already tried to build a state machine based on the propnet and failed,
	 * so we should avoid trying again.
	 */
	private boolean firstTry;

	public SingleGameSlowDUCTGamer(){
		super();
		this.firstTry = true;
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#getInitialStateMachine()
	 */
	@Override
	public StateMachine getInitialStateMachine() {

		// If the propnet machine already exists, return it.
		if(this.thePropnetMachine != null){

			System.out.println("Returning SAME propnet state machine.");

			return this.thePropnetMachine;
		}

		// Otherwise, if it doesn't exist because we never tried to build a propnet,
		// create it and return it
		if(firstTry){

			firstTry = false;

			return super.getInitialStateMachine();

		}else{

			System.out.println("Already FAILED with propnet, not gonna try again: returning prover state machine.");

			return new CachedStateMachine(new ProverStateMachine());
		}
	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineStop()
	 */
	@Override
	public void stateMachineStop() {

		System.out.println("Stop");

		this.mctsManager = null;
		System.gc();

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.statemachine.StateMachineGamer#stateMachineAbort()
	 */
	@Override
	public void stateMachineAbort() {

		System.out.println("Abort");
		this.mctsManager = null;
		System.gc();

	}

}
