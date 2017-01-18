package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
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
	 * Given the statistics of each move, selects one according to the given selector
	 */
	private TunerSelector tunerSelector;

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

		String[] tunerSelectorDetails = gamerSettings.getIDPropertyValue("ParametersTuner.tunerSelectorType");

		try {
			this.tunerSelector = (TunerSelector) TunerSelector.getConstructorForTunerSelector(ProjectSearcher.TUNER_SELECTORS.getConcreteClasses(), tunerSelectorDetails[0]).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, tunerSelectorDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating TunerSelector " + gamerSettings.getPropertyValue("ParametersTuner.tunerSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		this.rolesMabs = null;

		this.selectedCombinationsIndices = null;

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.tunerSelector.setReferences(sharedReferencesCollector);

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
		this.tunerSelector.clearComponent();
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

		this.tunerSelector.setUpComponent();

	}

	/**
	 * Selects for each MAB (i.e. each role being tuned) the next combinatorial move
	 * (i.e. the next combination of parameters).
	 *
	 * @return for each tuned role, a list with the indices of the values to be set to each parameters.
	 */
	@Override
	public int[][] selectNextCombinations(){

		int[][] nextCombinations = new int[this.rolesMabs.length][];

		for(int i = 0; i < this.rolesMabs.length; i++){
			this.selectedCombinationsIndices[i] = this.tunerSelector.selectMove(this.rolesMabs[i].getMoveStats(),
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
				indentation + "TUNER_SELECTOR = " + this.tunerSelector.printComponent(indentation + "  ") +
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
