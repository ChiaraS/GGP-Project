package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.FixedMab;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

/**
 * This tuner selects the combinations of values for the parameter of a the tuned role(s).
 *
 * @author C.Sironi
 *
 */
public class SingleMabParametersTuner extends ParametersTuner {

	/**
	 * All possible combinations of unit moves (i.e. all possible combinatorial moves).
	 * These won't change for the whole life span of the gamer, thus they don't have to be
	 * recomputed after each game.
	 */
	private CombinatorialCompactMove[] combinatorialMoves;

	/**
	 * Given the statistics of each combination, selects the next to evaluate.
	 */
	private TunerSelector nextCombinationSelector;

	/**
	 * Given the statistics of each combination, selects the best one among them.
	 */
	private TunerSelector bestCombinationSelector;

	/**
	 * For each role being tuned, representation of the combinatorial problem of settings values to the
	 * parameters as a multi-armed bandit problem.
	 *
	 * Note: this has either length=1 when tuning only my role or length=numRoles when tuning all roles.
	 */
	private FixedMab[] rolesMabs;

	/**
	 * Memorizes for each MAB the index of the last selected combinatorial move.
	 */
	private int[] selectedCombinationsIndices;

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

		this.rolesMabs = null;

		this.selectedCombinationsIndices = null;

	}

	public SingleMabParametersTuner(SingleMabParametersTuner toCopy) {
		super(toCopy);

		this.combinatorialMoves = null;

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

		this.nextCombinationSelector = toCopy.getNextCombinationSelector();

		this.bestCombinationSelector = toCopy.getBestCombinationSelector();

		this.rolesMabs = null;

		this.selectedCombinationsIndices = null;

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.nextCombinationSelector.setReferences(sharedReferencesCollector);
		this.bestCombinationSelector.setReferences(sharedReferencesCollector);
	}

	@Override
	public void setClassesLength(int[] classesLength){
		super.setClassesLength(classesLength);

		// Build the lists of all possible parameter combinations for my role and (if being tuned)
		// for the other roles (note that if the other roles are also being tuned all the possible
		// combinations of values are the same for each role.

		int numCombinatorialMoves = 1;

		// Count all the possible combinatorial moves
		for(int i = 0; i < this.classesLength.length; i++){
			if(classesLength[i] <= 0){
				GamerLogger.logError("SearchManagerCreation", "SingleMabParametersTuner - Initialization with class of moves of length less than 1. No values for the calss!");
				throw new RuntimeException("SingleMabParametersTuner - Initialization with class of moves of length 0. No values for the calss!");
			}

			numCombinatorialMoves *= classesLength[i];
		}

		// Create all the possible combinatorial moves
		this.combinatorialMoves = new CombinatorialCompactMove[numCombinatorialMoves];

		this.crossProduct(new int[1], new LinkedList<Integer>());
	}

    private void crossProduct(int[] nextFreeIndex, LinkedList<Integer> partial){
        if (partial.size() == this.classesLength.length) {
            this.combinatorialMoves[nextFreeIndex[0]] = new CombinatorialCompactMove(this.toIntArray(partial));
            nextFreeIndex[0]++;
        } else {
            for(int i = 0; i < this.classesLength[partial.size()]; i++) {
                partial.addLast(new Integer(i));
                this.crossProduct(nextFreeIndex, partial);
                partial.removeLast();
            }
        }
    }

    protected int[] toIntArray(LinkedList<Integer> partial){
    	int[] intArray = new int[partial.size()];

    	int index = 0;
    	for(Integer i : partial){
    		intArray[index] = i.intValue();
    		index++;
    	}
    	return intArray;
    }

    /**
     * After the end of each game clear the tuner.
     */
	@Override
	public void clearComponent(){
		this.rolesMabs = null;
		this.selectedCombinationsIndices = null;
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

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		// Create a MAB representation of the combinatorial problem for each role
		this.rolesMabs = new FixedMab[numRolesToTune];

		for(int i = 0; i < this.rolesMabs.length; i++){
			rolesMabs[i] = new FixedMab(this.combinatorialMoves.length);
		}

		this.selectedCombinationsIndices = new int[numRolesToTune];

		this.nextCombinationSelector.setUpComponent();
		this.bestCombinationSelector.setUpComponent();

	}

	/**
	 * SELECT NEXT COMBINATION TO EVALUATE
	 * Selects for each MAB (i.e. each role being tuned) the next combinatorial move
	 * (i.e. the next combination of parameters) to be evaluated and memorizes it as
	 * selected. This means that it will be the move for which the statistics will be
	 * updated next time the updateStatistics() method will be called.
	 *
	 * @return for each tuned role, a list with the indices of the values to be set to each parameters.
	 */
	@Override
	public int[][] selectNextCombinations(){

		int[][] nextCombinations = new int[this.rolesMabs.length][];

		for(int i = 0; i < this.rolesMabs.length; i++){
			this.selectedCombinationsIndices[i] = this.nextCombinationSelector.selectMove(this.rolesMabs[i].getMoveStats(),
					this.rolesMabs[i].getNumUpdates());
			nextCombinations[i] = this.combinatorialMoves[this.selectedCombinationsIndices[i]].getIndices();
		}

		return nextCombinations;

	}

	/**
	 * SELECT BEST COMBINATION FOUND SO FAR
	 * Selects for each MAB (i.e. each role being tuned) the best combinatorial move
	 * (i.e. the best combination of parameters) so far and memorizes it as selected.
	 * This means that it will be the move for which the statistics will be updated
	 * next time the updateStatistics() method will be called.
	 *
	 * Note: this method exists to allow two different strategies to select a move.
	 * When we are evaluating a move we don't want to always select the best, but
	 * we prefer to have an exploration of less good moves, too (e.g using UCB).
	 * However if we want to stick to a single combination of parameters for the rest
	 * of the search we prefer it to be the best overall.
	 *
	 * @return for each tuned role, a list with the indices of the values to be set to each parameters.
	 */
	@Override
	public int[][] getBestCombinations(){

		int[][] nextCombinations = new int[this.rolesMabs.length][];

		for(int i = 0; i < this.rolesMabs.length; i++){
			this.selectedCombinationsIndices[i] = this.bestCombinationSelector.selectMove(this.rolesMabs[i].getMoveStats(),
					this.rolesMabs[i].getNumUpdates());
			nextCombinations[i] = this.combinatorialMoves[this.selectedCombinationsIndices[i]].getIndices();
		}

		return nextCombinations;

	}

	/**
	 * Updates the statistics of each MAB for the last selected move according to the given rewards.
	 *
	 * @param rewards
	 */
	@Override
	public void updateStatistics(int[] rewards){

		if(rewards.length != this.rolesMabs.length){
			GamerLogger.logError("ParametersTuner", "SingleMabParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + rewards.length +
					") to update the MAB problems (" + this.rolesMabs.length + ").");
			throw new RuntimeException("SingleMabParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}

		for(int i = 0; i < rewards.length; i++){

			MoveStats stat = this.rolesMabs[i].getMoveStats()[this.selectedCombinationsIndices[i]];

			stat.incrementScoreSum(rewards[i]);
			stat.incrementVisits();

			this.rolesMabs[i].incrementNumUpdates();
		}

	}

	@Override
	public void logStats(){

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "");

		for(int i = 0; i < this.rolesMabs.length; i++){

			MoveStats[] allMoveStats = this.rolesMabs[i].getMoveStats();

			for(int j = 0; j < allMoveStats.length; j++){
				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "MAB=;" + i + ";COMBINATORIAL_MOVE=;" + this.combinatorialMoves[j] + ";VISITS=;" + allMoveStats[j].getVisits() + ";SCORE_SUM=;" + allMoveStats[j].getScoreSum() + ";AVG_VALUE=;" + (allMoveStats[j].getVisits() <= 0 ? "0" : (allMoveStats[j].getScoreSum()/((double)allMoveStats[j].getVisits()))));
			}

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "ParametersTunerStats", "");

		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "NUM_COMBINATORIAL_MOVES = " + (this.combinatorialMoves != null ? this.combinatorialMoves.length : 0) +
				indentation + "NEXT_COMBINATION_SELECTOR = " + this.nextCombinationSelector.printComponent(indentation + "  ") +
				indentation + "BEST_COMBINATION_SELECTOR = " + this.bestCombinationSelector.printComponent(indentation + "  ") +
				indentation + "num_roles_mabs = " + (this.rolesMabs != null ? this.rolesMabs.length : 0);

		if(this.selectedCombinationsIndices != null){
			String selectedCombinationsIndicesString = "[ ";

			for(int i = 0; i < this.selectedCombinationsIndices.length; i++){

				selectedCombinationsIndicesString += this.selectedCombinationsIndices[i] + " ";

			}

			selectedCombinationsIndicesString += "]";

			params += indentation + "SELECTED_COMBINATIONS_INDICES = " + selectedCombinationsIndicesString;
		}else{
			params += indentation + "SELECTED_COMBINATIONS_INDICES = null";
		}

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

	@Override
	public int getNumIndependentCombinatorialProblems() {
		return this.rolesMabs.length;
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
