package org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning;

import java.util.LinkedList;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning.selectors.UcbSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning.structure.CombinatorialMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.combinatorialtuning.structure.UcbCombinatorialProblemRepresentation;
import org.ggp.base.util.logging.GamerLogger;

/**
 * This tuner selects the combinations of values for the parameter of a the tuned role(s).
 *
 * @author C.Sironi
 *
 */
public class UcbCombinatorialTuner extends CombinatorialTuner {

	private double tunerC;

	private double tunerValueOffset;

	/**
	 * First play urgency for the tuner (i.e. default value of a combination that has never been explored).
	 */
	private double tunerFpu;

	/**
	 * All possible combinations of unit moves (i.e. all possible combinatorial moves).
	 * These won't change for the whole life span of the gamer, thus they don't have to be
	 * recomputed after each game.
	 */
	private int[][] combinatorialMoves;

	/**
	 * Given the statistics of each move, selects one according to the UCB formula
	 */
	private UcbSelector ucbSelector;

	/**
	 * For each role being tuned, representation of the combinatorial problem of settings values to the
	 * parameters as a multi-armed bandit problem.
	 *
	 * Note: this has either length=1 when tuning only my role or length=numRoles when tuning all roles.
	 */
	private UcbCombinatorialProblemRepresentation[] rolesMabs;

	/**
	 * Memorizes for each MAB the index of the last selected combinatorial move.
	 */
	private int[] selectedCombinationsIndices;

	public UcbCombinatorialTuner(Random random, double tunerC, double tunerValueOffset, double tunerFpu) {

		super();

		this.tunerC = tunerC;
		this.tunerValueOffset = tunerValueOffset;
		this.tunerFpu = tunerFpu;

		this.ucbSelector = new UcbSelector(random);

		this.rolesMabs = null;

		this.selectedCombinationsIndices = null;

		/*
		for(int i = 0; i < this.combinatorialMoves.length; i++){
			System.out.print("[ ");
			for(int j = 0; j < combinatorialMoves[i].length; j++){
				System.out.print(combinatorialMoves[i][j] + " ");
			}
			System.out.println("]");
		}
		*/

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
				GamerLogger.logError("SearchManagerCreation", "UcbCombinatorialTuner - Initialization with class of moves of length less than 1. No values for the calss!");
				throw new RuntimeException("UcbCombinatorialTuner - Initialization with class of moves of length 0. No values for the calss!");
			}

			numCombinatorialMoves *= classesLength[i];
		}

		// Create all the possible combinatorial moves
		this.combinatorialMoves = new int[numCombinatorialMoves][];

		this.crossProduct(new int[1], new LinkedList<Integer>());
	}

    private void crossProduct(int[] nextFreeIndex, LinkedList<Integer> partial){
        if (partial.size() == this.classesLength.length) {
            this.combinatorialMoves[nextFreeIndex[0]] = this.toIntArray(partial);
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
	public void clear(){
		this.rolesMabs = null;
		this.selectedCombinationsIndices = null;
	}

    /**
     * Before the start of each game creates a new MAB problem for each role being tuned.
     *
     * @param numRolesToTune either 1 (my role) or all the roles of the game we're going to play.
     */
	@Override
	public void setUp(int numRolesToTune){

		// Create a MAB representation of the combinatorial problem for each role
		this.rolesMabs = new UcbCombinatorialProblemRepresentation[numRolesToTune];

		for(int i = 0; i < this.rolesMabs.length; i++){
			rolesMabs[i] = new UcbCombinatorialProblemRepresentation(this.combinatorialMoves);
		}

		this.selectedCombinationsIndices = new int[numRolesToTune];

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
			this.selectedCombinationsIndices[i] = this.ucbSelector.selectMove(this.rolesMabs[i].getCombinatorialMoveStats(),
					this.rolesMabs[i].getNumUpdates(), this.tunerC, this.tunerValueOffset, this.tunerFpu);
			nextCombinations[i] = this.rolesMabs[i].getCombinatorialMoveStats()[this.selectedCombinationsIndices[i]].getTheCombinatorialMove();
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
			GamerLogger.logError("CombinatorialTuner", "UcbCombinatorialTuner - Impossible to update move statistics! Wrong number of rewards (" + rewards.length +
					") to update the MAB problems (" + this.rolesMabs.length + ").");
			throw new RuntimeException("UcbCombinatorialTuner - Initialization with class of moves of length 0. No values for the calss!");
		}

		for(int i = 0; i < rewards.length; i++){

			CombinatorialMoveStats stat = this.rolesMabs[i].getCombinatorialMoveStats()[this.selectedCombinationsIndices[i]];

			stat.incrementScoreSum(rewards[i]);
			stat.incrementVisits();

			this.rolesMabs[i].incrementNumUpdates();
		}

	}

	@Override
	public void logStats(){

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "CombinatorialTunerStats", "");

		for(int i = 0; i < this.rolesMabs.length; i++){

			CombinatorialMoveStats[] allMoveStats = this.rolesMabs[i].getCombinatorialMoveStats();

			for(int j = 0; j < allMoveStats.length; j++){

				int[] cMove = allMoveStats[j].getTheCombinatorialMove();

				String cMoveString = "[ ";
				for(int k = 0; k < cMove.length; k++){
					cMoveString += cMove[k] + " ";
				}
				cMoveString += "]";

				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "CombinatorialTunerStats", "MAB=;" + i + ";COMBINATORIAL_MOVE=;" + cMoveString + ";VISITS=;" + allMoveStats[j].getVisits() + ";SCORE_SUM=;" + allMoveStats[j].getScoreSum() + ";AVG_VALUE=;" + (allMoveStats[j].getVisits() <= 0 ? "0" : (allMoveStats[j].getScoreSum()/((double)allMoveStats[j].getVisits()))));
			}

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "CombinatorialTunerStats", "");

		}

	}

	public String getCombinatorialTunerParameters(String indentation) {

		String params = indentation + "EXPLORATION_CONSTANT = " + this.tunerC + indentation + "VALUE_OFFSET = " + this.tunerValueOffset + indentation + "FIRST_PLAY_URGENCY = " + this.tunerFpu + indentation + "NUM_COMBINATORIAL_MOVES = " + this.combinatorialMoves.length;

		/* TODO: print also other parameters???
		if(this.numUpdates != null){

			String numUpdatesString = "[ ";

			for(int i = 0; i < this.numUpdates.length; i++){

				numUpdatesString += this.numUpdates[i] + " ";

			}

			numUpdatesString += "]";

			params += indentation + "num_updates = " + numUpdatesString;
		}else{
			params += indentation + "num_updates = null";
		}

		if(this.populations != null){

			String populationsString = "[ ";

			for(int i = 0; i < this.populations.length; i++){

				if(this.populations[i] != null){

					populationsString += "[ ";

					for(int j = 0; j < this.populations[i].length; j++){

						populationsString += this.populations[i][j].getParameter() + " ";

					}

					populationsString += "] ";

				}else{
					populationsString += "null ";
				}

			}

			populationsString += "]";

			params += indentation + "populations = " + populationsString;


		}else{
			params += indentation + "populations = null";
		}

		if(this.currentSelectedIndividuals != null){

			String currentSelectedIndividualsString = "[ ";

			for(int i = 0; i < this.currentSelectedIndividuals.length; i++){

				currentSelectedIndividualsString += this.currentSelectedIndividuals[i] + " ";

			}

			currentSelectedIndividualsString += "]";

			params += indentation + "current_selected_individuals_indices = " + currentSelectedIndividualsString;
		}else{
			params += indentation + "current_selected_individuals_indices = null";
		}
		*/

		return params;
	}

	@Override
	public String printCombinatorialTuner(String indentation) {
		String params = this.getCombinatorialTunerParameters(indentation);

		if(params != null){
			return this.getClass().getSimpleName() + params;
		}else{
			return this.getClass().getSimpleName();
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
    	UcbCombinatorialTuner t = new UcbCombinatorialTuner(l);
    }
    */

	public static void main(String args[]){

		Random random = new Random();

		UcbCombinatorialTuner ucbCombinatorialTuner = new UcbCombinatorialTuner(random, 0.7, 0.01, Double.MAX_VALUE);

		int[] classesLength = new int[4];

		classesLength[0] = 9;
		classesLength[1] = 8;
		classesLength[2] = 10;
		classesLength[3] = 11;

		ucbCombinatorialTuner.setClassesLength(classesLength);

		ucbCombinatorialTuner.setUp(1);

		int[] rewards = new int[1];

		for(int i = 0; i < 1000000; i++){
			rewards[0] = random.nextInt(101);
			ucbCombinatorialTuner.selectNextCombinations();
			ucbCombinatorialTuner.updateStatistics(rewards);
		}


	}

}
