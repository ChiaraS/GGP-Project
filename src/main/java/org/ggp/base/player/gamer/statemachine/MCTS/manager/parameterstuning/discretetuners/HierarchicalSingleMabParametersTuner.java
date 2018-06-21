package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.discretetuners;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.HierarchicalFixedMab;
import org.ggp.base.util.logging.GamerLogger;

public class HierarchicalSingleMabParametersTuner extends SingleMabParametersTuner {

	/**
	 * For each role being tuned, representation of the combinatorial problem of settings values to the
	 * parameters as a multi-armed bandit problem with hierarchical expansion of the moves.
	 *
	 * Note: this has either length=1 when tuning only my role or length=numRoles when tuning all roles.
	 */
	private HierarchicalFixedMab[] rolesMabs;

	/**
	 * Memorizes for each MAB the indices of the last selected move for each class (i.e. indices of the
	 * last selected value for each parameter).
	 *
	 * selectedExpandedCombinationsIndices[mabIndex][paramIndex] = index of the value selected for paramIndex
	 * for the role associated with the MAB at mabIndex.
	 */
	private int[][] selectedExpandedCombinationsIndices;

	/**
	 * Memorizes for each MAB the indices of the best move for each class (i.e. indices of the
	 * best value for each parameter).
	 *
	 * bestExpandedCombinationsIndices[mabIndex][paramIndex] = index of the value selected for paramIndex
	 * for the role associated with the MAB at mabIndex.
	 */
	private int[][] bestExpandedCombinationsIndices;

	public HierarchicalSingleMabParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.rolesMabs = null;

		this.selectedExpandedCombinationsIndices = null;

	}

	/*
	public HierarchicalSingleMabParametersTuner(SingleMabParametersTuner toCopy) {
		super(toCopy);

		this.rolesMabs = null;

		this.selectedExpandedCombinationsIndices = null;

	}*/

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);
	}

	/**
     * After the end of each game clear the tuner.
     */
	@Override
	public void clearComponent(){
		super.clearComponent();
		this.rolesMabs = null;
		this.selectedExpandedCombinationsIndices = null;
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
		this.rolesMabs = new HierarchicalFixedMab[numRolesToTune];

		for(int rolesMabsIndex = 0; rolesMabsIndex < this.rolesMabs.length; rolesMabsIndex++){
			rolesMabs[rolesMabsIndex] = new HierarchicalFixedMab(this.discreteParametersManager.getNumPossibleValuesForAllParams(), 0);
		}

		this.selectedExpandedCombinationsIndices = new int[numRolesToTune][this.discreteParametersManager.getNumTunableParameters()];

	}

	@Override
	public void setNextCombinations() {

		HierarchicalFixedMab currentRoleMab;

		// For each tuning role...
		for(int rolesMabsIndex = 0; rolesMabsIndex < this.rolesMabs.length; rolesMabsIndex++){

			currentRoleMab = this.rolesMabs[rolesMabsIndex];

			for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){
				this.selectedExpandedCombinationsIndices[rolesMabsIndex][paramIndex] = -1; // It means that the index has not been set yet
			}

			// ...for each parameter (i.e. each level of the hierarchical representation)...
			for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){
				this.selectedExpandedCombinationsIndices[rolesMabsIndex][paramIndex] = this.nextCombinationSelector.selectMove(
						currentRoleMab.getMoveStats(),
						this.discreteParametersManager.getValuesFeasibility(paramIndex, this.selectedExpandedCombinationsIndices[rolesMabsIndex]),
						(this.discreteParametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.discreteParametersManager.getPossibleValuesPenalty(paramIndex) : new double[this.discreteParametersManager.getNumPossibleValues(paramIndex)]),
						currentRoleMab.getNumUpdates());
				if(currentRoleMab.getNextMabs() != null){
					currentRoleMab = currentRoleMab.getNextMabs()[this.selectedExpandedCombinationsIndices[rolesMabsIndex][paramIndex]];
				}
			}
		}

		this.discreteParametersManager.setParametersValues(this.selectedExpandedCombinationsIndices);

	}

	@Override
	public void setBestCombinations() {

		HierarchicalFixedMab currentRoleMab;

		// For each tuning role...
		for(int rolesMabsIndex = 0; rolesMabsIndex < this.rolesMabs.length; rolesMabsIndex++){

			currentRoleMab = this.rolesMabs[rolesMabsIndex];

			for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){
				this.selectedExpandedCombinationsIndices[rolesMabsIndex][paramIndex] = -1; // It means that the index has not been set yet
			}

			// ...for each parameter (i.e. each level of the hierarchical representation)...
			for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){
				this.selectedExpandedCombinationsIndices[rolesMabsIndex][paramIndex] = this.bestCombinationSelector.selectMove(
						currentRoleMab.getMoveStats(),
						this.discreteParametersManager.getValuesFeasibility(paramIndex, this.selectedExpandedCombinationsIndices[rolesMabsIndex]),
						(this.discreteParametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.discreteParametersManager.getPossibleValuesPenalty(paramIndex) : new double[this.discreteParametersManager.getNumPossibleValues(paramIndex)]),
						currentRoleMab.getNumUpdates());
				if(currentRoleMab.getNextMabs() != null){
					currentRoleMab = currentRoleMab.getNextMabs()[this.selectedExpandedCombinationsIndices[rolesMabsIndex][paramIndex]];
				}
			}
		}

		this.discreteParametersManager.setParametersValues(this.selectedExpandedCombinationsIndices);

		this.stopTuning();

	}

	@Override
	public void updateStatistics(double[] goals) {

		double[] neededRewards;

		// We have to check if the ParametersTuner is tuning parameters only for the playing role
		// or for all roles and update the statistics with appropriate rewards.
		if(this.tuneAllRoles){
			neededRewards = goals;
		}else{
			neededRewards = new double[1];
			neededRewards[0] = goals[this.gameDependentParameters.getMyRoleIndex()];
		}

		if(neededRewards.length != this.rolesMabs.length){
			GamerLogger.logError("ParametersTuner", "HierarchicalSingleMabParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + neededRewards.length +
					") to update the MAB problems (" + this.rolesMabs.length + ").");
			throw new RuntimeException("HierarchicalSingleMabParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}

		HierarchicalFixedMab currentRoleMab;
		MoveStats stat;

		// For each role MAB...
		for(int roleMabIndex = 0; roleMabIndex < this.rolesMabs.length; roleMabIndex++){

			currentRoleMab = this.rolesMabs[roleMabIndex];

			// ...for each parameter (i.e. each level of the hierarchical representation)...
			for(int paramIndex = 0; paramIndex < this.discreteParametersManager.getNumTunableParameters(); paramIndex++){

				stat = currentRoleMab.getMoveStats()[this.selectedExpandedCombinationsIndices[roleMabIndex][paramIndex]];

				stat.incrementScoreSum(neededRewards[roleMabIndex]);
				stat.incrementVisits();

				currentRoleMab.incrementNumUpdates();

				if(currentRoleMab.getNextMabs() != null){
					currentRoleMab = currentRoleMab.getNextMabs()[this.selectedExpandedCombinationsIndices[roleMabIndex][paramIndex]];
				}
			}

		}

	}

	@Override
	public void logStats() {

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "HierParamTunerStats", "");

		for(int roleMabIndex = 0; roleMabIndex < this.rolesMabs.length; roleMabIndex++){

			int roleIndex;
			if(this.tuneAllRoles){
				roleIndex = roleMabIndex;
			}else{
				roleIndex = this.gameDependentParameters.getMyRoleIndex();
			}

			this.logStatsOfMab(this.rolesMabs[roleMabIndex], roleIndex, "", "", 0);
		}

	}

	private void logStatsOfMab(HierarchicalFixedMab currentMab, int roleIndex, String partialParamValues, String partialParams, int paramIndex){
		if(currentMab != null){
			MoveStats[] stats = currentMab.getMoveStats();

			for(int paramValueIndex = 0; paramValueIndex < stats.length; paramValueIndex++){
				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "HierParamTunerStats",
						"ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) +
						";PARAMS=;[ " + partialParams + this.discreteParametersManager.getName(paramIndex) +
						" ];PARTIAL_VALUES=;[ " + partialParamValues + this.discreteParametersManager.getPossibleValues(paramIndex)[paramValueIndex] +
						" ];PENALTY=;" + (this.discreteParametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.discreteParametersManager.getPossibleValuesPenalty(paramIndex)[paramValueIndex] : 0) +
						";VISITS=;" + stats[paramValueIndex].getVisits() + ";SCORE_SUM=;" + stats[paramValueIndex].getScoreSum() +
						";AVG_VALUE=;" + (stats[paramValueIndex].getVisits() <= 0 ? "0" : (stats[paramValueIndex].getScoreSum()/((double)stats[paramValueIndex].getVisits()))));
			}

			HierarchicalFixedMab[] nextMabs = currentMab.getNextMabs();

			if(nextMabs != null){
				for(int paramValueIndex = 0; paramValueIndex < nextMabs.length; paramValueIndex++){
					GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "HierParamTunerStats", "");
					this.logStatsOfMab(nextMabs[paramValueIndex], roleIndex, partialParamValues +
							this.discreteParametersManager.getPossibleValues(paramIndex)[paramValueIndex] + " ",
							partialParams + this.discreteParametersManager.getName(paramIndex) + " ", paramIndex+1);
				}
			}
		}
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
		this.bestExpandedCombinationsIndices = this.selectedExpandedCombinationsIndices;
	}


}
