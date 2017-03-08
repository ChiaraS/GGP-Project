package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.statemachine.structure.Move;

public class MastAfterMove extends AfterMoveStrategy {

	private Map<Move, MoveStats> mastStatistics;

	private double decayFactor;

	public MastAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.decayFactor = gamerSettings.getDoublePropertyValue("AfterMoveStrategy" + id + ".decayFactor");
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.mastStatistics = sharedReferencesCollector.getMastStatistics();
	}

	@Override
	public void clearComponent() {
		// Do nothing (because the MAST statistics will be already cleared by the strategy that populates them,
		// i.e. the backpropagation strategy that uses the MastUpdater).
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "DECAY_FACTOR = " + this.decayFactor + indentation + "mast_statistics = " + (this.mastStatistics == null ? "null" : (this.mastStatistics.size()+" entries"));
	}

	@Override
	public void afterMoveActions() {

		/*
		String toPrint2 = "MastStats[";
		if(this.mastStatistics == null){
			toPrint2 += "null]\n";
		}else{
			for(Entry<Move, MoveStats> mastStatistic : this.mastStatistics.entrySet()){
				toPrint2 += "\n  MOVE(" + mastStatistic.getKey().toString() + "), " + mastStatistic.getValue().toString();
			}
			toPrint2 += "  ]\n";
		}
		toPrint2 += "]";
		System.out.println(toPrint2);
		*/



		if(this.decayFactor == 0.0){ // If we want to throw away everything, we just clear all the stats. No need to iterate.
			this.mastStatistics.clear();
		}else if(this.decayFactor != 1.0){ // If the decay factor is 1.0 we keep everything without modifying anything.
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
		}

		/*
		String toPrint = "MastStats[";
		if(this.mastStatistics == null){
			toPrint += "null]\n";
		}else{
			for(Entry<Move, MoveStats> mastStatistic : this.mastStatistics.entrySet()){
				toPrint += "\n  MOVE(" + mastStatistic.getKey().toString() + "), " + mastStatistic.getValue().toString();
			}
			toPrint += "  ]\n";
		}

		toPrint += "]";

		System.out.println(toPrint);
		*/

		// VERSION 2: decrease and don't check anything.
		/*
		for(MoveStats m : this.mastStatistics.values()){
			m.decreaseByFactor(this.decayFactor);
		}
		*/
	}

}
