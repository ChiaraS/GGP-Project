package org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning.structure;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;

public class CombinatorialMoveStats extends MoveStats {

	/**
	 * The combinatorial move (i.e. the list of indices that the values to be assigned to the different
	 * parameters have in the corresponding list of available values for the parameter).
	 */
	private int[] combinatorialMove;

	public CombinatorialMoveStats(int[] combinatorialMove) {
		super();
		this.combinatorialMove = combinatorialMove;
	}

	public CombinatorialMoveStats(int visits, double scoreSum, int[] combinatorialMove) {
		super(visits, scoreSum);
		this.combinatorialMove = combinatorialMove;
	}

	public int[] getTheCombinatorialMove() {
		return this.combinatorialMove;
	}

	@Override
	public String toString(){

		String combinatorialMoveString = "[ ";
		for(int i = 0; i < this.combinatorialMove.length; i++){
			combinatorialMoveString += this.combinatorialMove[i] + " ";
		}
		combinatorialMoveString += "]";

		return "COMBINATORIAL_MOVE(" + combinatorialMoveString + "), " + super.toString();
	}

}
