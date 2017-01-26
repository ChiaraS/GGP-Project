package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors;

import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.MultiInstanceSearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.statemachine.structure.Move;

public abstract class TunerSelector extends MultiInstanceSearchManagerComponent{

	public TunerSelector(GameDependentParameters gameDependentParameters,
			Random random, GamerSettings gamerSettings,	SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

	}

	/**
	 * These two separate methods do the same thing but one works with an array of statistics and returns
	 * the index of the selected statistic, while the other works with a map of statistics and returns the
	 * move corresponding to the selected statistic.
	 */
	public abstract int selectMove(MoveStats[] movesStats, int numUpdates);

	public abstract Move selectMove(Map<Move,MoveStats> movesStats, int numUpdates);

}
