package org.ggp.base.player.gamer.statemachine.MCTS.prover;

public abstract class ProverUctMctsGamer extends ProverMctsGamer {

	/**
	 * Parameters used by the MCTS manager.
	 */
	protected double c;

	/**
	 * Default value to be assigned to a move during selection if such move has never been seen before
	 * and a value for it cannot be thus computed.
	 */
	protected double unexploredMoveDefaultSelectionValue;

	/**
	 *
	 */
	public ProverUctMctsGamer() {
		// TODO: change code so that the parameters can be set from outside.

		super();

		this.c = 0.7;

		this.unexploredMoveDefaultSelectionValue = Double.MAX_VALUE;

	}

}
