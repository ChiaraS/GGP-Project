package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.util.statemachine.structure.Move;

public class MASTAfterMove extends AfterMoveStrategy {

	private Map<Move, MoveStats> mastStatistics;

	private double decayFactor;

	public MASTAfterMove(GameDependentParameters gameDependentParameters, Map<Move, MoveStats> mastStatistics, double decayFactor) {

		super(gameDependentParameters);

		this.mastStatistics = mastStatistics;
		this.decayFactor = decayFactor;
	}

	@Override
	public String getStrategyParameters() {
		return "DECAY_FACTOR = " + this.decayFactor;
	}

	@Override
	public String printStrategy() {

		String params = this.getStrategyParameters();

		if(params != null){
			return "[AFTER_MOVE_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[AFTER_MOVE_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

	@Override
	public void afterMoveActions() {

		// VERSION 1: decrease, then check if the visits became 0 and, if so, remove the statistic
		// for the move. -> This means that if the move will be explored again in the next step of
		// the search, a new entry for the move will be created. However it's highly likely that the
		// number of visits decreases to 0 because this move is never explored again because the real
		// game ended up in a part of the tree where this move will not be legal anymore. In this case
		// we won't keep around statistics that we will never use again, but we risk also to end up
		// removing the statistic object for a move that will be explored again during the next steps
		// and we will have to recreate the object (in this case we'll consider as garbage an object
		// that instead we would have needed again).
		Iterator<Entry<Move,MoveStats>> iterator = this.mastStatistics.entrySet().iterator();
		Entry<Move,MoveStats> theEntry;
		while(iterator.hasNext()){
			theEntry = iterator.next();
			theEntry.getValue().decreaseByFactor(this.decayFactor);
			if(theEntry.getValue().getVisits() == 0){
				iterator.remove();
			}
		}

		// VERSION 2: decrease and don't check anything.
		/*
		for(MoveStats m : this.mastStatistics.values()){
			m.decreaseByFactor(this.decayFactor);
		}
		*/
	}

}
