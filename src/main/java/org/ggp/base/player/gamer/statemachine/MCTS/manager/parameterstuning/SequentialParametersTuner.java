package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.SequentialProblemRepresentation;

public class SequentialParametersTuner extends ParametersTuner {

	/**
	 * If true, after all the parameters have been tuned once sequentially,
	 * this tuner will randomize their order before tuning them all again sequentially.
	 * If false, they will be tuned sequentially repeatedly always in the same order.
	 */
	private boolean shuffleTuningOrder;

	/**
	 * Selects the next value to evaluate for the currently tuned parameter.
	 */
	private TunerSelector nextValueSelector;

	/**
	 * Selects the best value to set for the currently tuned parameter.
	 */
	private TunerSelector bestValueSelector;

	/**
	 * Indices of the parameters in the order in which they should be tuned.
	 */
	private List<Integer> tuningOrder;

	/**
	 * Index that keeps track of which parameter is being tuned in the tuning order.
	 * E.g.: if the tuning order is [ 2 3 0 1 ], and orderIndex=1 the parameter being
	 * tuned at the moment is 3. If we must start tuning the next parameter, then we
	 * will switch to parameter 0.
	 */
	private int orderIndex;

	private SequentialProblemRepresentation[] roleProblems;

	private int[][] selectedCombinations;

	private int[][] bestCombinations;

	/**
	 * Number of samples that should be taken for the current parameter being tuned.
	 * After maxSamplesPerParam samples the tuner will start tuning the next parameter.
	 */
	private int maxSamplesPerParam;

	/**
	 * Number of samples taken so far for the parameter currently being tuned.
	 */
	private int currentNumSamples;

	public SequentialParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);

	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();
		this.nextValueSelector.setUpComponent();
		this.bestValueSelector.setUpComponent();

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		// We need to use the role problems if:
		// 1. We are not going to reuse the best combo of previous games
		// 2. We are going to reuse the best combo of previous games, but that has not been computed yet
		// 3. We are going to reuse the best combo of previous games, it has been computed, but its size
		// doesn't correspond to the number of roles that we have to tune.
		// (TODO: here we should check that we reuse the best combo ONLY if we are playing the exact same game
		// and not just a game with the same number of roles).
		if(!this.reuseBestCombos || this.bestCombinations == null || this.bestCombinations.length != numRolesToTune){

			// If we need to use the role problems, here we have to check if we need new ones or if we should
			// reuse previous ones that have been saved.
			// We need new ones if:
			// 1. We don't want to reuse the previous ones
			// 2. We want to reuse the previous ones, but we have none yet
			// 3. We want to reuse the previous ones, we have them but their size doesn't correspond to the number
			// of roles that we have to tune.
			// (TODO: here we should check that we reuse role problems ONLY if we are playing the exact same game
			// and not just a game with the same number of roles).
			if(!this.reuseStats || this.roleProblems == null || this.roleProblems.length != numRolesToTune){
				// Create the representation of the combinatorial problem for each role
				this.roleProblems = new SequentialProblemRepresentation[numRolesToTune];
				for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){
					roleProblems[roleProblemIndex] = new SequentialProblemRepresentation(this.parametersManager.getNumPossibleValuesForAllParams());
				}

				this.selectedCombinations = new int[numRolesToTune][this.parametersManager.getNumTunableParameters()];

				// TODO: is it ok to re-shuffle order and reset orderIndex and currentNumSamples only when
				// we don't re-use the statistics? In this way, when re-using the statistics the tuning starts
				// exactly where it was left (i.e. from last tuned parameter, taking only the remaining samples
				// if we are changing parameter after every maxSamplesPerParam samples. Or from the next parameter to
				// tune in the tuningOrder, if we are changing parameter after every move).
				// Should we just keep the stats, but re-start tuning from the 1st parameter in the order taking
				// all the predefined samples?
				if(this.shuffleTuningOrder){
					Collections.shuffle(this.tuningOrder);
				}

				this.orderIndex = 0;
				this.currentNumSamples = 0;
			}

			this.bestCombinations = null;
		}else{
			this.roleProblems = null;
			this.selectedCombinations = null;
		}

	}

	@Override
	public void clearComponent() {
		super.clearComponent();
		this.nextValueSelector.clearComponent();
		this.bestValueSelector.clearComponent();

		if(!this.reuseStats){
			this.roleProblems = null;
			this.selectedCombinations = null;
		}
	}

	@Override
	public void setNextCombinations() {

		// If maxSamplesPerParam==Integer.MAX_VALUE it means we are tuning one parameter per move, so we don't
		// need to check how many samples have been taken so far to know if we need to start tuning the next
		// parameter. After performing a move in the real game, this class will be told to start tuning the next
		// parameter by the AfterMoveStrategy.
		if(this.maxSamplesPerParam < Integer.MAX_VALUE && this.currentNumSamples == this.maxSamplesPerParam){
			// CHANGE param!
		}


	}

	public void changeTunedParameter(){


	}

	@Override
	public void setBestCombinations() {
		// TODO Auto-generated method stub
		this.stopTuning();
	}

	@Override
	public int getNumIndependentCombinatorialProblems() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateStatistics(int[] goals) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logStats() {
		// TODO Auto-generated method stub

	}

	@Override
	public void decreaseStatistics(double factor) {
		// TODO Auto-generated method stub

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
