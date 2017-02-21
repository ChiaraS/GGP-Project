package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.biascomputers.BiasComputer;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.Move;

import csironi.ggp.course.utils.Pair;

public abstract class TunerSelector extends SearchManagerComponent{

	protected BiasComputer biasComputer;

	public TunerSelector(GameDependentParameters gameDependentParameters,
			Random random, GamerSettings gamerSettings,	SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		if(gamerSettings.specifiesProperty("TunerSelector" + id + ".biasComputerType")){

			String[] biasComputerDetails = gamerSettings.getIDPropertyValue("TunerSelector" + id + ".biasComputerType");

			try {
				this.biasComputer = (BiasComputer) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(
						SearchManagerComponent.getCorrespondingClass(ProjectSearcher.BIAS_COMPUTERS.getConcreteClasses(),
								biasComputerDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, biasComputerDetails[1]);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// TODO: fix this!
				GamerLogger.logError("SearchManagerCreation", "Error when instantiating BiasComputer " + gamerSettings.getPropertyValue("TunerSelector" + id + ".biasComputerType") + ".");
				GamerLogger.logStackTrace("SearchManagerCreation", e);
				throw new RuntimeException(e);
			}

		}else{
			this.biasComputer = null;
		}

	}

	/**
	 * These two separate methods do the same thing but one works with an array of statistics and returns
	 * the index of the selected statistic, while the other works with a map of statistics and returns the
	 * move corresponding to the selected statistic.
	 */
	public abstract int selectMove(MoveStats[] movesStats, double[] movesPenalty, int numUpdates);

	public abstract Move selectMove(Map<Move, Pair<MoveStats,Double>> movesInfo, int numUpdates);

}
