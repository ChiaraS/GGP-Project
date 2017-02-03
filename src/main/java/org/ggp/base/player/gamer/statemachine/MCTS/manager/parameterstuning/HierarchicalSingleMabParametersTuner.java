package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.HierarchicalFixedMab;
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
	 */
	private int[][] selectedExpandedCombinationsIndices;

	public HierarchicalSingleMabParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.rolesMabs = null;

		this.selectedExpandedCombinationsIndices = null;

	}

	public HierarchicalSingleMabParametersTuner(SingleMabParametersTuner toCopy) {
		super(toCopy);

		this.rolesMabs = null;

		this.selectedExpandedCombinationsIndices = null;

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

		for(int i = 0; i < this.rolesMabs.length; i++){
			rolesMabs[i] = new HierarchicalFixedMab(this.classesLength, 0);
		}

		this.selectedExpandedCombinationsIndices = new int[numRolesToTune][this.classesLength.length];

	}

	@Override
	public int[][] selectNextCombinations() {

		HierarchicalFixedMab currentRoleMab;

		// For each role...
		for(int i = 0; i < this.rolesMabs.length; i++){

			currentRoleMab = this.rolesMabs[i];

			// ...for each parameter (i.e. each level of the hierarchical representation)...
			for(int j = 0; j < this.classesLength.length; j++){
				this.selectedExpandedCombinationsIndices[i][j] = this.nextCombinationSelector.selectMove(currentRoleMab.getMoveStats(),
						currentRoleMab.getNumUpdates());
				currentRoleMab = currentRoleMab.getNextMabs()[this.selectedExpandedCombinationsIndices[i][j]];
			}

		}

		// Attention! For now we are sure that the returned matrix won't be modified outside of this class,
		// However, modify to return a copy if this doesn't hold anymore!
		return this.selectedExpandedCombinationsIndices;

	}

	@Override
	public int[][] getBestCombinations() {

		HierarchicalFixedMab currentRoleMab;

		// For each role...
		for(int i = 0; i < this.rolesMabs.length; i++){

			currentRoleMab = this.rolesMabs[i];

			// ...for each parameter (i.e. each level of the hierarchical representation)...
			for(int j = 0; j < this.classesLength.length; j++){
				this.selectedExpandedCombinationsIndices[i][j] = this.bestCombinationSelector.selectMove(currentRoleMab.getMoveStats(),
						currentRoleMab.getNumUpdates());
				currentRoleMab = currentRoleMab.getNextMabs()[this.selectedExpandedCombinationsIndices[i][j]];
			}

		}

		// Attention! For now we are sure that the returned matrix won't be modified outside of this class,
		// However, modify to return a copy if this doesn't hold anymore!
		return this.selectedExpandedCombinationsIndices;

	}

	@Override
	public int getNumIndependentCombinatorialProblems() {
		return this.rolesMabs.length;
	}

	@Override
	public void updateStatistics(int[] rewards) {

		if(rewards.length != this.rolesMabs.length){
			GamerLogger.logError("ParametersTuner", "HierarchicalSingleMabParametersTuner - Impossible to update move statistics! Wrong number of rewards (" + rewards.length +
					") to update the MAB problems (" + this.rolesMabs.length + ").");
			throw new RuntimeException("HierarchicalSingleMabParametersTuner - Impossible to update move statistics! Wrong number of rewards!");
		}

		HierarchicalFixedMab currentRoleMab;
		MoveStats stat;

		// For each role...
		for(int i = 0; i < this.rolesMabs.length; i++){

			currentRoleMab = this.rolesMabs[i];

			// ...for each parameter (i.e. each level of the hierarchical representation)...
			for(int j = 0; j < this.classesLength.length; j++){

				stat = currentRoleMab.getMoveStats()[this.selectedExpandedCombinationsIndices[i][j]];

				stat.incrementScoreSum(rewards[i]);
				stat.incrementVisits();

				currentRoleMab.incrementNumUpdates();

				currentRoleMab = currentRoleMab.getNextMabs()[this.selectedExpandedCombinationsIndices[i][j]];
			}

		}

	}

	@Override
	public void logStats() {
		// TODO Auto-generated method stub

	}

}