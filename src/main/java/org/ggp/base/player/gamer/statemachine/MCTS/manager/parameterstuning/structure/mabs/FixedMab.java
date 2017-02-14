package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;


/**
 * This class represents the combinatorial problem as a Multi-Armed Bandit problem where each
 * combinatorial move is seen as different arm of the MAB.
 *
 * Given all the classes of unit-moves this class creates all possible combinatorial moves.
 *
 * NOTE: this class is not re-used between games because of the different number of roles a game has
 *
 * @author C.Sironi
 *
 */
public class FixedMab extends Mab{

	/**
	 * Statistics for all possible moves.
	 */
	private MoveStats[] movesStats;

	public FixedMab(int numMoves) {

		super();

		this.movesStats = new MoveStats[numMoves];

		for(int i = 0; i < numMoves; i++){
			this.movesStats[i] = new MoveStats();
		}

	}

    public MoveStats[] getMoveStats(){
    	return this.movesStats;
    }

}
