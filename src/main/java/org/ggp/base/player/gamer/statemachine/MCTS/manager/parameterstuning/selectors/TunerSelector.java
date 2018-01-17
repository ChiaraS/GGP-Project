package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.biascomputers.BiasComputer;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.Move;

import csironi.ggp.course.utils.MyPair;

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
	 * This method selects one of the statistics and returns its index. It also excludes from the selection the
	 * statistics for values that are not feasible (i.e. the corresponding entry in the valuesFeasibility array is set
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

	/**
	 * This method selects one of the statistics and returns the corresponding move.
	 * If this method is called on statistics for combinations of values there is no need to specify the feasibility, because
	 * all combinations are feasible (i.e. the fact that they have been added to the map already means that they are always
	 * feasible). If this method is called on statistics for single parameter values then the feasibility must be specified as
	 * a set of Moves that are feasible (this set is a subset of the moves in the movesInfo map).
	 * If the feasibleMoves set is passes as null then all moves are considered feasible.
	 *
	 * @param movesStats map of statistics.
	 * @param numUpdates
	 * @return
	 */
	public abstract Move selectMove(Map<Move,MyPair<MoveStats,Double>> movesInfo, Set<Move> feasibleMoves, int numUpdates);

	/**
	 * This method selects one of the statistics and returns its index. It also excludes from the selection the
	 * statistics for values that are not feasible (i.e. the corresponding entry in the valuesFeasibility array is set
	 * to false).
	 *
	 * @param movesStats array(s) of statistics. Each entry of the array corresponds to different arrays of statistics.
	 * Corresponding entries of different arrays correspond to the same move (combination of parameters), thus each array
	 * of move stats shares the same array of valuesFeasibility and movesPenalty.
	 * This method selects the best statistic over all the given ones, returning the index of the statistic array that
	 * contains the best statistics and the index of the best statistic in such array.
	 * @param valuesFeasibility for each entry in the movesStats array specifies if it has to be considered (true)
	 * or not (false) in the current selection. This array allows to exclude form the selection the statistics of the
	 * moves that are not feasible (i.e. when selecting a value for a tunable parameter allows to exclude those values
	 * that are not feasible for the current configuration of other parameter values). If this array is null, all moves
	 * will be considered feasible and the corresponding movesStats will be taken into account.
	 * @param movesPenalty penalty associated to each move.
	 * @param numUpdates must be the sum of numUpdates of ALL the given statistics, thus over ALL the lists of MoveStats!
	 * @return
	 */
	public abstract MyPair<Integer,Integer> selectMove(MoveStats[][] movesStats, boolean[] valuesFeasibility, double[] movesPenalty, int numUpdates);

	/**
	 * This method selects one of the statistics and returns the corresponding move.
	 * For now there is no check if some values are not feasible because this method is always called on statistics for
	 * combinations of values that are always feasible (i.e. the fact that they have been added to the map already means
	 * that they are always feasible.
	 *
	 * @param movesStats map(s) of statistics in a list. Each entry of the list corresponds to different maps of
	 * statistics. This method selects the best statistic over all the given ones, returning the index in the list of
	 * the map of statistics that contains the best statistics and the move associated to the best statistic.
	 * @param numUpdates must be the sum of numUpdates of ALL the given statistics, thus over ALL the Mpas of MoveStats!
	 * @return
	 */
	public abstract MyPair<Integer,Move> selectMove(List<Map<Move,MyPair<MoveStats,Double>>> movesInfo, int numUpdates);

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "BIAS_COMPUTER = " + (this.biasComputer == null ? "null" :
			this.biasComputer.printComponent(indentation + "  "));

		return params;

	}

}
