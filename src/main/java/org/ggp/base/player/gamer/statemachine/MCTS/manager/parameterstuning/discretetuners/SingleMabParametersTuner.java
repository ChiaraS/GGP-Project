package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.discretetuners;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

/**
 * This tuner selects the combinations of values for the parameter of a the tuned role(s).
 *
 * @author C.Sironi
 *
 */
public abstract class SingleMabParametersTuner extends DiscreteParametersTuner {

	/**
	 * Given the statistics of each combination, selects the next to evaluate.
	 */
	protected TunerSelector nextCombinationSelector;

	/**
	 * Given the statistics of each combination, selects the best one among them.
	 */
	protected TunerSelector bestCombinationSelector;

	public SingleMabParametersTuner(GameDependentParameters gameDependentParameters,
			Random random, GamerSettings gamerSettings,	SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		String[] tunerSelectorDetails = gamerSettings.getIDPropertyValue("ParametersTuner.nextCombinationSelectorType");

		try {
			this.nextCombinationSelector = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("ParametersTuner.nextCombinationSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		tunerSelectorDetails = gamerSettings.getIDPropertyValue("ParametersTuner.bestCombinationSelectorType");

		try {
			this.bestCombinationSelector = (TunerSelector) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("ParametersTuner.bestCombinationSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

	}

	/*
	public SingleMabParametersTuner(SingleMabParametersTuner toCopy) {
		super(toCopy); */

		/* TODO: ATTENTON! Here we just copy the reference because all tuners use the same selector!
		However, doing so, whenever the methods clearComponent() and setUpComponent() are called  on
		this and all the other copies of this tuner they will be called on the same TunerSelector
		multiple times. Now it's not a problem, since for all TunerSelectors those methods do nothing,
		but if they get changed then consider this issue!!! A solution is to deep-copy also the tuner,
		but it'll require more memory. */
		/* This is how to deep-copy it:
		try {
			this.tunerSelector = (TunerSelector) SearchManagerComponent.getCopyConstructorForSearchManagerComponent(toCopy.getTunerSelector().getClass()).newInstance(toCopy.getTunerSelector());
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + toCopy.getTunerSelector().getClass().getSimpleName() + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}
		*/

	/*
		this.nextCombinationSelector = toCopy.getNextCombinationSelector();

		this.bestCombinationSelector = toCopy.getBestCombinationSelector();

	}*/

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);
		this.nextCombinationSelector.setReferences(sharedReferencesCollector);
		this.bestCombinationSelector.setReferences(sharedReferencesCollector);
	}

    /**
     * After the end of each game clear the tuner.
     */
	@Override
	public void clearComponent(){
		super.clearComponent();
		this.nextCombinationSelector.clearComponent();
		this.bestCombinationSelector.clearComponent();
	}

    /**
     * Before the start of each game creates a new MAB problem for each role being tuned.
     *
     * @param numRolesToTune either 1 (my role) or all the roles of the game we're going to play.
     */
	@Override
	public void setUpComponent(){
		super.setUpComponent();
		this.nextCombinationSelector.setUpComponent();
		this.bestCombinationSelector.setUpComponent();
	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "NEXT_COMBINATION_SELECTOR = " + this.nextCombinationSelector.printComponent(indentation + "  ") +
				indentation + "BEST_COMBINATION_SELECTOR = " + this.bestCombinationSelector.printComponent(indentation + "  ");

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

	public TunerSelector getNextCombinationSelector(){
		return this.nextCombinationSelector;
	}

	public TunerSelector getBestCombinationSelector(){
		return this.bestCombinationSelector;
	}

    /*
    public static void main(String args[]){
    	int[] l = new int[3];
    	l[0] = 3;
    	l[1] = 2;
    	l[2] = 4;
    	SingleMabParametersTuner t = new SingleMabParametersTuner(l);
    }
    */

	/*
	public static void main(String args[]){

		Random random = new Random();

		SingleMabParametersTuner singleMabParametersTuner = new SingleMabParametersTuner(random, 0.7, 0.01, Double.MAX_VALUE);

		int[] classesLength = new int[4];

		classesLength[0] = 9;
		classesLength[1] = 8;
		classesLength[2] = 10;
		classesLength[3] = 11;

		singleMabParametersTuner.setClassesLength(classesLength);

		singleMabParametersTuner.setUp(1);

		int[] rewards = new int[1];

		for(int i = 0; i < 1000000; i++){
			rewards[0] = random.nextInt(101);
			singleMabParametersTuner.selectNextCombinations();
			singleMabParametersTuner.updateStatistics(rewards);
		}

	}
	*/

}
