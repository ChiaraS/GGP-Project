package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs;

import java.util.HashMap;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.structure.Move;

public class IncrementalMab extends Mab {

	/**
	 * Statistics for the moves.
	 */
	private Map<Move,MoveStats> movesStats;

	public IncrementalMab() {
		this(null);
	}

	public IncrementalMab(Move[] moves) {

		super();

		this.movesStats = new HashMap<Move,MoveStats>();

		if(moves !=null){
			for(int i = 0; i < moves.length; i++){
				this.movesStats.put(moves[i], new MoveStats());
			}
		}

	}

    public Map<Move,MoveStats> getMoveStats(){
    	return this.movesStats;
    }

}
