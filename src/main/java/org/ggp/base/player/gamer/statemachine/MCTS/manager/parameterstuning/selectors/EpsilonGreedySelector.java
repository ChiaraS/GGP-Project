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
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.Move;

import csironi.ggp.course.utils.MyPair;

/**
 * Selects a move according to tunerSelector1 with probability epsilon,
 * otherwise selects a move according to tunerSelector2.
 *
 * @author C.Sironi
 *
 */
public class EpsilonGreedySelector extends TunerSelector{

	private double epsilon;

	private TunerSelector tunerSelector1;

	private TunerSelector tunerSelector2;

	public EpsilonGreedySelector(
			GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.epsilon = gamerSettings.getDoublePropertyValue("TunerSelector" + id + ".epsilon");

		String[] tunerSelectorDetails = gamerSettings.getIDPropertyValue("TunerSelector" + id + ".tunerSelector1Type");

		try {
			this.tunerSelector1 = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("TunerSelector" + id + ".tunerSelector1Type") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		tunerSelectorDetails = gamerSettings.getIDPropertyValue("TunerSelector" + id + ".tunerSelector2Type");

		try {
			this.tunerSelector2 = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("TunerSelector" + id + ".tunerSelector2Type") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.tunerSelector1.setReferences(sharedReferencesCollector);
		this.tunerSelector2.setReferences(sharedReferencesCollector);

	}

	/*
	 * TODO: ATTENTION! If you add code here that does something on the state of the tuner remember that this method might
	 * be called multiple times after each game if the player is using the SequentialParametersTuner!!! If you want this method
	 * to be called only once per instance of TunerSelector then change the code in the copy constructor of the ParametersTuner
	 * subclasses in order to deep-copy also the TunerSelectors!
	 */
	@Override
	public void clearComponent() {
		this.tunerSelector1.clearComponent();
		this.tunerSelector2.clearComponent();
	}

	/*
	 * TODO: ATTENTION! If you add code here that does something on the state of the tuner remember that this method might
	 * be called multiple times before each game if the player is using the SequentialParametersTuner!!! If you want this method
	 * to be called only once per instance of TunerSelector then change the code in the copy constructor of the ParametersTuner
	 * subclasses in order to deep-copy also the TunerSelectors!
	 */
	@Override
	public void setUpComponent() {
		this.tunerSelector1.setUpComponent();
		this.tunerSelector2.setUpComponent();
	}

	/**
	 * TODO: adapt the MctsManager code to also use this class.
	 *
	 * @param moveStats list(s) with the statistics for each move.
	 * @param numUpdates number of total visits of the moves so far (i.e. number of times any move
	 * has been visited).
	 * @param c constant to be used for this selection.
	 * @param valueOffset the selected move will be a random one among the ones with value in the
	 * interval [maxValue-valueOffset, maxValue].
	 * @param fpu first play urgency.
	 * @return the index of the selected move.
	 */
	@Override
	public int selectMove(MoveStats[] movesStats, boolean[] valuesFeasibility, double[] movesPenalty, int numUpdates){

		if(this.random.nextDouble() < this.epsilon){
			return this.tunerSelector1.selectMove(movesStats, valuesFeasibility, movesPenalty, numUpdates);
		}else{
			return this.tunerSelector2.selectMove(movesStats, valuesFeasibility, movesPenalty, numUpdates);
		}

	}

	@Override
	public Move selectMove(Map<Move,MyPair<MoveStats,Double>> movesInfo, Set<Move> feasibleMoves, int numUpdates) {
		if(this.random.nextDouble() < this.epsilon){
			return this.tunerSelector1.selectMove(movesInfo, feasibleMoves, numUpdates);
		}else{
			return this.tunerSelector2.selectMove(movesInfo, feasibleMoves, numUpdates);
		}
	}

	@Override
	public MyPair<Integer,Integer> selectMove(MoveStats[][] movesStats, boolean[] valuesFeasibility, double[] movesPenalty, int numUpdates){

		if(this.random.nextDouble() < this.epsilon){
			return this.tunerSelector1.selectMove(movesStats, valuesFeasibility, movesPenalty, numUpdates);
		}else{
			return this.tunerSelector2.selectMove(movesStats, valuesFeasibility, movesPenalty, numUpdates);
		}

	}

	@Override
	public MyPair<Integer,Move> selectMove(List<Map<Move,MyPair<MoveStats,Double>>> movesInfo, int numUpdates) {
		if(this.random.nextDouble() < this.epsilon){
			return this.tunerSelector1.selectMove(movesInfo, numUpdates);
		}else{
			return this.tunerSelector2.selectMove(movesInfo, numUpdates);
		}
	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "EPSILON = " + this.epsilon +
				indentation + "TUNER_SELECTOR_1 = " + this.tunerSelector1.printComponent(indentation + "  ") +
				indentation + "TUNER_SELECTOR_2 = " + this.tunerSelector2.printComponent(indentation + "  ");

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

}
