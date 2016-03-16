/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS;


/**
 * This gamer performs UCT Monte Carlo Tree Search.
 *
 * The gamer can be set in two ways:
 * - [DUCT=true] (default): the player will perform Decoupled UCT Monte Carlo Tree Search.
 * - [DUCT=false]: the player will perform Sequential UCT Monte Carlo Tree Search.
 *
 * {At the beginning of each game it tries to build the propnet. If it builds it will use for the
 * whole game the state machine based on the propnet otherwise it will use the cached prover.
 * Depending on the chosen state machine it will perform DUCT using the corresponding tree structure
 * (i.e. the one that uses internal propnet states to perform MCTS if the propnet managed to build,
 * the one that uses standard states otherwise). TODO: not true yet. Now it only uses the propnet
 * state machine and throws exception if it cannot be built.}
 *
 * @author C.Sironi
 *
 */
public abstract class UCTMCTSGamer extends MCTSGamer {

	/**
	 * Parameters used by the MCTS manager.
	 */
	protected double c;

	/**
	 *
	 */
	public UCTMCTSGamer() {
		// TODO: change code so that the parameters can be set from outside.

		super();

		this.c = 0.7;

	}

}
