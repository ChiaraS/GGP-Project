package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs;

import java.util.HashMap;
import java.util.Map;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.util.statemachine.structure.Move;

import csironi.ggp.course.utils.Pair;

public class IncrementalMab extends Mab {

	/**
	 * Statistics for the moves.
	 */
	private Map<Move,Pair<MoveStats,Double>> movesInfo;

	public IncrementalMab() {
		this(null, null);
	}

	public IncrementalMab(Move[] moves, double[] movesPenalty) {

		super();

		this.movesInfo = new HashMap<Move,Pair<MoveStats,Double>>();

		if(moves !=null){
			if(movesPenalty != null){
				for(int i = 0; i < moves.length; i++){
					this.movesInfo.put(moves[i], new Pair<MoveStats,Double>(new MoveStats(), new Double(movesPenalty[i])));
				}
			}else{
				for(int i = 0; i < moves.length; i++){
					this.movesInfo.put(moves[i], new Pair<MoveStats,Double>(new MoveStats(), new Double(-1)));
				}
			}
		}

	}

    public Map<Move,Pair<MoveStats,Double>> getMovesInfo(){
    	return this.movesInfo;
    }

}
