package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

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
			rolesMabs[rolesMabsIndex] = new HierarchicalFixedMab(this.parametersManager.getNumPossibleValuesForAllParams(), 0);
		}

		this.selectedExpandedCombinationsIndices = new int[numRolesToTune][this.parametersManager.getNumTunableParameters()];

	}

	@Override
	public void setNextCombinations() {

		HierarchicalFixedMab currentRoleMab;

		// For each tuning role...
		for(int rolesMabsIndex = 0; rolesMabsIndex < this.rolesMabs.length; rolesMabsIndex++){

			currentRoleMab = this.rolesMabs[rolesMabsIndex];

			for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
				this.selectedExpandedCombinationsIndices[rolesMabsIndex][paramIndex] = -1; // It means that the index has not been set yet
			}

			// ...for each parameter (i.e. each level of the hierarchical representation)...
			for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
				this.selectedExpandedCombinationsIndices[rolesMabsIndex][paramIndex] = this.nextCombinationSelector.selectMove(
						currentRoleMab.getMoveStats(),
						this.parametersManager.getValuesFeasibility(paramIndex, this.selectedExpandedCombinationsIndices[rolesMabsIndex]),
						(this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex) : new double[this.parametersManager.getNumPossibleValues(paramIndex)]),
						currentRoleMab.getNumUpdates());
				if(currentRoleMab.getNextMabs() != null){
					currentRoleMab = currentRoleMab.getNextMabs()[this.selectedExpandedCombinationsIndices[rolesMabsIndex][paramIndex]];
				}
			}
		}

		this.parametersManager.setParametersValues(this.selectedExpandedCombinationsIndices);

	}

	@Override
	public void setBestCombinations() {

		HierarchicalFixedMab currentRoleMab;

		// For each tuning role...
		for(int rolesMabsIndex = 0; rolesMabsIndex < this.rolesMabs.length; rolesMabsIndex++){

			currentRoleMab = this.rolesMabs[rolesMabsIndex];

			for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
				this.selectedExpandedCombinationsIndices[rolesMabsIndex][paramIndex] = -1; // It means that the index has not been set yet
			}

			// ...for each parameter (i.e. each level of the hierarchical representation)...
			for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
				this.selectedExpandedCombinationsIndices[rolesMabsIndex][paramIndex] = this.bestCombinationSelector.selectMove(
						currentRoleMab.getMoveStats(),
						this.parametersManager.getValuesFeasibility(paramIndex, this.selectedExpandedCombinationsIndices[rolesMabsIndex]),
						(this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex) : new double[this.parametersManager.getNumPossibleValues(paramIndex)]),
						currentRoleMab.getNumUpdates());
				if(currentRoleMab.getNextMabs() != null){
					currentRoleMab = currentRoleMab.getNextMabs()[this.selectedExpandedCombinationsIndices[rolesMabsIndex][paramIndex]];
				}
			}
		}

		this.parametersManager.setParametersValues(this.selectedExpandedCombinationsIndices);

		this.stopTuning();

	}

	@Override
	public void updateStatistics(int[] goals) {

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
			for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){

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

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "HierParametersTunerStats", "");

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
				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "HierParametersTunerStats",
						"ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) +
						";PARAMS=;[ " + partialParams + this.parametersManager.getName(paramIndex) +
						" ];PARTIAL_VALUES=;[ " + partialParamValues + this.parametersManager.getPossibleValues(paramIndex)[paramValueIndex] +
						" ];PENALTY=;" + (this.parametersManager.getPossibleValuesPenalty(paramIndex) != null ? this.parametersManager.getPossibleValuesPenalty(paramIndex)[paramValueIndex] : 0) +
						";VISITS=;" + stats[paramValueIndex].getVisits() + ";SCORE_SUM=;" + stats[paramValueIndex].getScoreSum() +
						";AVG_VALUE=;" + (stats[paramValueIndex].getVisits() <= 0 ? "0" : (stats[paramValueIndex].getScoreSum()/((double)stats[paramValueIndex].getVisits()))));
			}

			HierarchicalFixedMab[] nextMabs = currentMab.getNextMabs();

			if(nextMabs != null){
				for(int paramValueIndex = 0; paramValueIndex < nextMabs.length; paramValueIndex++){
					GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "HierParametersTunerStats", "");
					this.logStatsOfMab(nextMabs[paramValueIndex], roleIndex, partialParamValues +
							this.parametersManager.getPossibleValues(paramIndex)[paramValueIndex] + " ",
							partialParams + this.parametersManager.getName(paramIndex) + " ", paramIndex+1);
				}
			}
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
