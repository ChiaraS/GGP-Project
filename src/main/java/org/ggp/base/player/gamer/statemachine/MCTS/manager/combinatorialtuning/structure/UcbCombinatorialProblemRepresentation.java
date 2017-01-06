package org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning.structure;


/**
 * This class represents the combinatorial problem as a Multi-Armed Bandit problem where each
 * combinatorial move is seen as different arm of the MAB.
 *
 * Given all the classes of unit-moves this class creates all possible combinatorial moves.
 *
 * NOTE: this class is not re-used between games because of the diffrenet number of roles a game has
 *
 * @author C.Sironi
 *
 */
public class UcbCombinatorialProblemRepresentation {

	/**
	 * Statistics for all possible combinatorial moves.
	 * (Combinatorial moves = all possible combinations of indices of the unit moves).
	 */
	private CombinatorialMoveStats[] combinatorialMovesStats;

	/**
	 *  Number of times any of the combinatorial actions has been evaluated.
	 */
	private int numUpdates;

	public UcbCombinatorialProblemRepresentation(int[][] combinatorialMoves) {

		this.combinatorialMovesStats = new CombinatorialMoveStats[combinatorialMoves.length];

		for(int i = 0; i < combinatorialMoves.length; i++){
			this.combinatorialMovesStats[i] = new CombinatorialMoveStats(combinatorialMoves[i]);
		}

		this.numUpdates = 0;

	}

    public CombinatorialMoveStats[] getCombinatorialMoveStats(){
    	return this.combinatorialMovesStats;
    }

    public int getNumUpdates(){
    	return this.numUpdates;
    }

    public void incrementNumUpdates(){
    	this.numUpdates++;
    }

}
