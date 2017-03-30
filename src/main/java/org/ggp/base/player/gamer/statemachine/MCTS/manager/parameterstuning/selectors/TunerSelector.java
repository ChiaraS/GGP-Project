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

	/**
	 * Computes a bias for each move (i.e. parameter or combination of parameters) depending on the penalty value
	 * specified for the parameter(s) values that compose the move.
	 * NOTE that whenever a BiasComputer is specified, it will ALWAYS add a bias to the evaluation of the move,
	 * even when some or all the penalty values are not specified in the settings. It will assume that non-
	 * specified values are 0, so will give to those moves the highest bias. This will result in a different
	 * behavior than the one that will be obtained by not adding a bias at all. If no bias must be added to any
	 * move, then no BiasComputer must be specified.
	 */
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
	 * The following methods do the same thing but one works with an array of statistics and returns
	 * the index of the selected statistic, while the other works with a map of statistics and returns the
	 * move corresponding to the selected statistic.
	 */

	/**
	 * This method selects one of the statistics and returns its index.does the same thing as the previous one but excludes from the selection the statistics
	 * for values that are not feasible (i.e. the corresponding entry in the valuesFeasibility array is set
	 * to false).
	 *
	 * @param movesStats array of statistics.
	 * @param valuesFeasibility for each entry in the movesStats array specifies if it has to be considered (true)
	 * or not (false) in the current selection. This array allows to exclude form the selection the statistics of the
	 * moves that are not feasible (i.e. when selecting a value for a tunable parameter allows to exclude those values
	 * that are not feasible for the current configuration of other parameter values). If this array is null, all moves
	 * will be considered feasible and the corresponding movesStats will be taken into account.
	 * @param movesPenalty penalty associated to each move.
	 * @param numUpdates
	 * @return
	 */
	public abstract int selectMove(MoveStats[] movesStats, boolean[] valuesFeasibility, double[] movesPenalty, int numUpdates);

	public abstract Move selectMove(Map<Move, Pair<MoveStats,Double>> movesInfo, int numUpdates);



}
