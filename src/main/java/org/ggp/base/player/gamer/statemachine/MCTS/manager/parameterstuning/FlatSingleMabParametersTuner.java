package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.FixedMab;
import org.ggp.base.util.logging.GamerLogger;

public class FlatSingleMabParametersTuner extends SingleMabParametersTuner {

	/**
	 * All possible combinations of unit moves (i.e. all possible combinatorial moves).
	 * These won't change for the whole life span of the gamer, thus they don't have to be
	 * recomputed after each game.
	 */
	//private CombinatorialCompactMove[] combinatorialMoves;
	private List<CombinatorialCompactMove> newCombinatorialMoves;

	/**
	 * Penalty of all the possible combinations of unit moves (i.e. all possible combinatorial moves).
	 */
	private double[] combinatorialMovesPenalty;

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

	/**
	 * Memorizes for each MAB the index of the best combinatorial move.
	 */
	private int[] bestCombinationsIndices;

	public FlatSingleMabParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.newCombinatorialMoves = null;

		this.combinatorialMovesPenalty = null;

		this.rolesMabs = null;

		this.selectedCombinationsIndices = null;

		this.bestCombinationsIndices = null;

	}

	/*
	public FlatSingleMabParametersTuner(FlatSingleMabParametersTuner toCopy) {
		super(toCopy);

		this.combinatorialMoves = null;

		this.combinatorialMovesPenalty = null;

		this.rolesMabs = null;

		this.selectedCombinationsIndices = null;

	}*/

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);
		// Here the parameters manager has been initialized properly and can be used to compute
		// the combinatorial moves and their penalty.

		// Build the lists of all possible parameter combinations for my role and (if being tuned)
		// for the other roles (note that if the other roles are also being tuned all the possible
		// combinations of values are the same for each role.

		// Create all the possible combinatorial moves and corresponding penalty
		this.newCombinatorialMoves = new ArrayList<CombinatorialCompactMove>();
		int[] partialCombo = new int[this.parametersManager.getNumTunableParameters()];
		for(int i = 0; i < partialCombo.length; i++){
			partialCombo[i] = -1;
		}
		this.crossProduct(0, partialCombo);

        this.combinatorialMovesPenalty = new double[this.newCombinatorialMoves.size()];
       	for(int i = 0; i < this.newCombinatorialMoves.size(); i++){
       		this.combinatorialMovesPenalty[i] = this.computeCombinatorialMovePenalty(this.newCombinatorialMoves.get(i).getIndices());
        }

	}

    private void crossProduct(int paramIndex, int[] partialCombo){

		//System.out.println("ParamIndex = " + paramIndex + ", Combo = " + Arrays.toString(partialCombo));

        if (paramIndex == this.parametersManager.getNumTunableParameters()) {
        	//System.out.println("Adding");
            this.newCombinatorialMoves.add(new CombinatorialCompactMove(this.copyArray(partialCombo)));
            //System.out.println("Returning");
        } else {
        	boolean atLeastOneFeasibleCombo = false;
            for(int i = 0; i < this.parametersManager.getNumPossibleValues(paramIndex); i++) {

            	//System.out.println(i);

            	//System.out.println("Possibe values = " + this.parametersManager.getNumPossibleValues(paramIndex));
            	partialCombo[paramIndex] = i;
            	if(this.parametersManager.isValid(partialCombo)){
            		atLeastOneFeasibleCombo = true;
            		//System.out.println("PRE - ParamIndex = " + paramIndex + ", Combo = " + Arrays.toString(partialCombo));
            		this.crossProduct(paramIndex+1, partialCombo);
            		//System.out.println("POST - ParamIndex = " + paramIndex + ", Combo = " + Arrays.toString(partialCombo));
            	}
            	//System.out.println("PREPRE - ParamIndex = " + paramIndex + ", Combo = " + Arrays.toString(partialCombo));
            	partialCombo[paramIndex] = -1;
            	//System.out.println("POSTPOST - ParamIndex = " + paramIndex + ", Combo = " + Arrays.toString(partialCombo));
            }
            if(!atLeastOneFeasibleCombo){
            	String partialComboString = "[ ";
            	for(int j = 0; j < paramIndex; j++){
            		partialComboString += this.parametersManager.getName(j) + "=" + this.parametersManager.getPossibleValues(j)[partialCombo[j]];
            	}
            	partialComboString += "]";
            	GamerLogger.logError("ParametersTuner", "FlatSingleMabParametersTuner - No valid value detected for parameter " +
            			this.parametersManager.getName(paramIndex) + "for the following partial combination: " + partialComboString);
				throw new RuntimeException("FlatSingleMabParametersTuner - No valid value detected for a parameter when computing all combinatorial moves!");
            }
        }
    }

    private int[] copyArray(int[] array){
    	int[] newArray = new int[array.length];
    	for(int i = 0; i < array.length; i++){
    		newArray[i] = array[i];
    	}
    	return newArray;
    }


	/**
     * After the end of each game clear the tuner.
     */
	@Override
	public void clearComponent(){
		super.clearComponent();
		this.rolesMabs = null;
		this.selectedCombinationsIndices = null;
	}

    /**
     * Before the start of each game creates a new MAB problem for each role being tuned.
     *
     * @param numRolesToTune either 1 (my role) or all the roles of the game we're going to play.
     */
	@Override
	public void setUpComponent(){

		super.setUpComponent();

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		// Create a MAB representation of the combinatorial problem for each role
		this.rolesMabs = new FixedMab[numRolesToTune];

		for(int i = 0; i < this.rolesMabs.length; i++){
			rolesMabs[i] = new FixedMab(this.newCombinatorialMoves.size());
		}

		this.selectedCombinationsIndices = new int[numRolesToTune];

	}

	/*
	@Override
	public void setClassesAndPenalty(String[] classesNames, int[] classesLength, String[][] classesValues, double[][] unitMovesPenalty){

		super.setClassesAndPenalty(classesNames, classesLength, classesValues, unitMovesPenalty);

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

		// Create all the possible combinatorial moves and corresponding penalty
		this.combinatorialMoves = new CombinatorialCompactMove[numCombinatorialMoves];
        if(this.unitMovesPenalty != null){
        	this.combinatorialMovesPenalty = new double[numCombinatorialMoves];
        }

		this.crossProduct(new int[1], new LinkedList<Integer>());
	}*/

	/*
	public void newSetClassesAndPenalty(String[] classesNames, int[] classesLength, String[][] classesValues, double[][] unitMovesPenalty){

		super.setClassesAndPenalty(classesNames, classesLength, classesValues, unitMovesPenalty);

		// Build the lists of all possible parameter combinations for my role and (if being tuned)
		// for the other roles (note that if the other roles are also being tuned all the possible
		// combinations of values are the same for each role.

		// Create all the possible combinatorial moves and corresponding penalty
		int[] partialCombo = new int[this.parametersManager.getNumTunableParameters()];
		for(int i = 0; i < partialCombo.length; i++){
			partialCombo[i] = -1;
		}
		this.newCrossProduct(0, partialCombo);

        this.combinatorialMovesPenalty = new double[this.newCombinatorialMoves.size()];
       	for(int i = 0; i < this.newCombinatorialMoves.size(); i++){
       		this.combinatorialMovesPenalty[i] = this.computeCombinatorialMovePenalty(this.newCombinatorialMoves.get(i).getIndices());
        }
	}*/

	/*
    private void crossProduct(int[] nextFreeIndex, LinkedList<Integer> partial){
        if (partial.size() == this.classesLength.length) {
            this.combinatorialMoves[nextFreeIndex[0]] = new CombinatorialCompactMove(this.toIntArray(partial));
            if(this.unitMovesPenalty != null){
            	this.combinatorialMovesPenalty[nextFreeIndex[0]] = this.computeCombinatorialMovePenalty(this.combinatorialMoves[nextFreeIndex[0]].getIndices());
            }
            nextFreeIndex[0]++;
        } else {
            for(int i = 0; i < this.classesLength[partial.size()]; i++) {
                partial.addLast(new Integer(i));
                this.crossProduct(nextFreeIndex, partial);
                partial.removeLast();
            }
        }
    }
    */

    /*
    protected int[] toIntArray(LinkedList<Integer> partial){
    	int[] intArray = new int[partial.size()];

    	int index = 0;
    	for(Integer i : partial){
    		intArray[index] = i.intValue();
    		index++;
    	}
    	return intArray;
    }
    */

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
	public void setNextCombinations(){

		int[][] nextCombinations = new int[this.rolesMabs.length][];

		for(int i = 0; i < this.rolesMabs.length; i++){
			this.selectedCombinationsIndices[i] = this.nextCombinationSelector.selectMove(this.rolesMabs[i].getMoveStats(),
					null, this.combinatorialMovesPenalty, this.rolesMabs[i].getNumUpdates());
			nextCombinations[i] = this.newCombinatorialMoves.get(this.selectedCombinationsIndices[i]).getIndices();
		}

		this.parametersManager.setParametersValues(nextCombinations);

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
	public void setBestCombinations(){

		int[][] nextCombinations = new int[this.rolesMabs.length][];

		for(int i = 0; i < this.rolesMabs.length; i++){
			this.selectedCombinationsIndices[i] = this.bestCombinationSelector.selectMove(this.rolesMabs[i].getMoveStats(),
					null, this.combinatorialMovesPenalty, this.rolesMabs[i].getNumUpdates());
			nextCombinations[i] = this.newCombinatorialMoves.get(this.selectedCombinationsIndices[i]).getIndices();
		}

		this.parametersManager.setParametersValues(nextCombinations);

		this.stopTuning();

	}

	/**
	 * Updates the statistics of each MAB for the last selected move according to the given rewards.
	 *
	 * @param rewards
	 */
	@Override
	public void updateStatistics(int[] goals){

		int[] neededRewards;

		// We have to check if the ParametersTuner is tuning parameters only for the playing role
		// or for all roles and update the statistics with appropriate rewards.
		if(this.tuneAllRoles){
			neededRewards = goals;
		}else{
			neededRewards = new int[1];
			neededRewards[0] = goals[this.gameDependentParameters.getMyRoleIndex()];

		}

		if(neededRewards.length != this.rolesMabs.length){
			GamerLogger.logError("ParametersTuner", "FlatSingleMabParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + neededRewards.length +
					") to update the MAB problems (" + this.rolesMabs.length + ").");
			throw new RuntimeException("FlatSingleMabParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}

		for(int roleMabIndex = 0; roleMabIndex < neededRewards.length; roleMabIndex++){

			MoveStats stat = this.rolesMabs[roleMabIndex].getMoveStats()[this.selectedCombinationsIndices[roleMabIndex]];

			stat.incrementScoreSum(neededRewards[roleMabIndex]);
			stat.incrementVisits();

			this.rolesMabs[roleMabIndex].incrementNumUpdates();
		}

	}

	@Override
	public void logStats(){

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "GlobalParamTunerStats", "");

		String globalParamsOrder = "[ ";
		for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
			globalParamsOrder += (this.parametersManager.getName(paramIndex) + " ");
		}
		globalParamsOrder += "]";

		for(int roleMabIndex = 0; roleMabIndex < this.rolesMabs.length; roleMabIndex++){

			int roleIndex;
			if(this.tuneAllRoles){
				roleIndex = roleMabIndex;
			}else{
				roleIndex = this.gameDependentParameters.getMyRoleIndex();
			}

			CombinatorialCompactMove theValuesIndices;
			String theValues;

			MoveStats[] allMoveStats = this.rolesMabs[roleMabIndex].getMoveStats();

			for(int comboIndex = 0; comboIndex < allMoveStats.length; comboIndex++){

				theValuesIndices = this.newCombinatorialMoves.get(comboIndex);
				theValues = "[ ";
				for(int paramIndex = 0; paramIndex < theValuesIndices.getIndices().length; paramIndex++){
					theValues += (this.parametersManager.getPossibleValues(paramIndex)[theValuesIndices.getIndices()[paramIndex]] + " ");
				}
				theValues += "]";

				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "GlobalParamTunerStats", "ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) + ";PARAMS=;" + globalParamsOrder + ";COMBINATORIAL_MOVE=;" + theValues + ";PENALTY=;" + (this.combinatorialMovesPenalty != null ? this.combinatorialMovesPenalty[comboIndex] : 0) + ";VISITS=;" + allMoveStats[comboIndex].getVisits() + ";SCORE_SUM=;" + allMoveStats[comboIndex].getScoreSum() + ";AVG_VALUE=;" + (allMoveStats[comboIndex].getVisits() <= 0 ? "0" : (allMoveStats[comboIndex].getScoreSum()/((double)allMoveStats[comboIndex].getVisits()))));
			}

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "GlobalParamTunerStats", "");

		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = "";

		/*
		if(this.newCombinatorialMoves != null){
			String combinatorialMovesString = "[ ";

			for(CombinatorialCompactMove move : this.newCombinatorialMoves){

				combinatorialMovesString += move + " ";

			}

			combinatorialMovesString += "]";

			params += indentation + "COMBINATORIAL_MOVES = " + combinatorialMovesString;
		}else{
			params += indentation + "COMBINATORIAL_MOVES = null";
		}

		if(this.combinatorialMovesPenalty != null){
			String combinatorialMovesPenaltyString = "[ ";

			for(int i = 0; i < this.combinatorialMovesPenalty.length; i++){

				combinatorialMovesPenaltyString += this.combinatorialMovesPenalty[i] + " ";

			}

			combinatorialMovesPenaltyString += "]";

			params += indentation + "COMBINATORIAL_MOVES_PENALTY = " + combinatorialMovesPenaltyString;
		}else{
			params += indentation + "COMBINATORIAL_MOVES_PENALTY = null";
		}
		*/

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

		if(this.bestCombinationsIndices != null){
			String bestCombinationsIndicesString = "[ ";

			for(int i = 0; i < this.bestCombinationsIndices.length; i++){

				bestCombinationsIndicesString += this.bestCombinationsIndices[i] + " ";

			}

			bestCombinationsIndicesString += "]";

			params += indentation + "SELECTED_COMBINATIONS_INDICES = " + bestCombinationsIndicesString;
		}else{
			params += indentation + "SELECTED_COMBINATIONS_INDICES = null";
		}

		params += indentation + "NUM_COMBINATORIAL_MOVES = " + (this.newCombinatorialMoves != null ? this.newCombinatorialMoves.size() : 0) +
		indentation + "num_roles_mabs = " + (this.rolesMabs != null ? this.rolesMabs.length : 0);

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

	@Override
	public void decreaseStatistics(double factor) {
		for(int i = 0; i < this.rolesMabs.length; i++){
			this.rolesMabs[i].decreaseStatistics(factor);
		}
	}

	@Override
	public boolean isMemorizingBestCombo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void memorizeBestCombinations() {
		// TODO Auto-generated method stub

	}

}
