package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.discretetuners;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.util.logging.GamerLogger;

public class RandomParametersTuner extends DiscreteParametersTuner {

	/**
	 * If true, this tuner selects a random combination for each tuned role only the first time
	 * and then keeps it fixed for the rest of the game. If false, this tuner will select every
	 * time a new random combination for each tuned role.
	 */
	private boolean fixedCombination;

	private List<CombinatorialCompactMove> allCombiantions;

	private int[][] combinationsVisits;

	public RandomParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.fixedCombination = gamerSettings.getBooleanPropertyValue("ParametersTuner.fixedCombination");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);

		this.allCombiantions = this.discreteParametersManager.getAllLegalParametersCombinations();

	}

	/**
     * After the end of each game clear the tuner.
     */
	@Override
	public void clearComponent(){
		super.clearComponent();
		this.combinationsVisits = null;
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

		this.combinationsVisits = new int[numRolesToTune][this.allCombiantions.size()];

	}

	@Override
	public void setNextCombinations() {

		//System.out.println("Next");

		if(this.fixedCombination) {
			this.setBestCombinations();
		}else {

			int numRolesToTune;

			if(this.tuneAllRoles){
				numRolesToTune = this.gameDependentParameters.getNumRoles();
			}else{
				numRolesToTune = 1;
			}

			int[][] nextCombinations = new int[numRolesToTune][];

			int nextComboIndex;

			for(int roleIndex = 0; roleIndex < nextCombinations.length; roleIndex++){

				nextComboIndex = this.random.nextInt(this.allCombiantions.size());

				this.combinationsVisits[roleIndex][nextComboIndex]++;

				nextCombinations[roleIndex] = this.allCombiantions.get(nextComboIndex).getIndices();

			}

			this.discreteParametersManager.setParametersValues(nextCombinations);
		}

	}

	@Override
	public void setBestCombinations() {

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		int[][] nextCombinations = new int[numRolesToTune][];

		int nextComboIndex;

		for(int roleIndex = 0; roleIndex < nextCombinations.length; roleIndex++){

			nextComboIndex = this.random.nextInt(this.allCombiantions.size());

			this.combinationsVisits[roleIndex][nextComboIndex]++;

			nextCombinations[roleIndex] = this.allCombiantions.get(nextComboIndex).getIndices();

		}

		this.discreteParametersManager.setParametersValues(nextCombinations);

		// Log the combination that we are selecting as best
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "BestParamsCombo", this.getLogOfCombinations(nextCombinations));

		this.stopTuning();

	}

	@Override
	public void updateStatistics(int[] goals) {

		//System.out.println("Update");
		// Do nothing

	}

	/**
	 * Note that this method takes extremely long (minutes) when using 5 parameters for a total of 59400 combinations
	 */
	@Override
	public void logStats() {

		//System.out.println("Logging");

		String toLog = "";

		String globalParamsOrder = this.getGlobalParamsOrder();

		for(int roleIndex = 0; roleIndex < this.combinationsVisits.length; roleIndex++){

			int actualRoleIndex;
			if(this.tuneAllRoles){
				actualRoleIndex = roleIndex;
			}else{
				actualRoleIndex = this.gameDependentParameters.getMyRoleIndex();
			}

			CombinatorialCompactMove combination;
			String theValues;

			for(int comboIndex = 0; comboIndex < this.allCombiantions.size(); comboIndex++){

				//System.out.println(comboIndex);

				combination = this.allCombiantions.get(comboIndex);
				theValues = "[ ";
				for(int paramIndex = 0; paramIndex < combination.getIndices().length; paramIndex++){
					theValues += (this.discreteParametersManager.getPossibleValues(paramIndex)[combination.getIndices()[paramIndex]] + " ");
				}
				theValues += "]";

				toLog += ("\nROLE=;" + actualRoleIndex + ";PARAMS=;" + globalParamsOrder + ";COMB_MOVE=;" + theValues +
						";PENALTY=;0;VISITS=;" + this.combinationsVisits[roleIndex][comboIndex] + ";SCORE_SUM=;0;AVG_VALUE=;0");
			}

			toLog += "\n";

		}

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "GlobalParamTunerStats", toLog);

	}

	@Override
	public void decreaseStatistics(double factor) {
		// Do nothing
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

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "FIXED_COMBINATION = " + this.fixedCombination +
				indentation + "NUM_COMBINATORIAL_MOVES = " + (this.allCombiantions != null ? this.allCombiantions.size() : 0) ;

		String superParams = super.getComponentParameters(indentation);

		if(superParams != null){
			return  params + superParams;
		}else{
			return params;
		}

	}

}
