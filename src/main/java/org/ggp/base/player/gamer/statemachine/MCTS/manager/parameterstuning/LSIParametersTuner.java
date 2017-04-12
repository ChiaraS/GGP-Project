package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.RandomSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.LsiProblemRepresentation;

import csironi.ggp.course.utils.Pair;

public class LSIParametersTuner extends TwoPhaseParametersTuner {

	/**
	 * Number of samples (i.e. simulations) that will be dedicated to the generation of
	 * candidate combinatorial actions (i.e. combinations of parameters) that will be
	 * evaluated in the subsequent phase.
	 */
	private int numGenSamples;

	/**
	 * Number of samples (i.e. simulations) that will be dedicated to the evaluation of
	 * the generated combinatorial actions (i.e. combinations of parameters) before
	 * committing to a single combinatorial action (i.e. combination of parameters).
	 */
	private int numEvalSamples;

	/**
	 * Counts the total number of samples taken so far.
	 */
	private int totalSamplesCounter;

	/**
	 * Lsi problem representations for each of the roles for which the parameters are being tuned.
	 */
	private LsiProblemRepresentation[] roleProblems;

	/**
	 * Random selector used to select random values for the parameters when completing combinatorial moves.
	 */
	private RandomSelector randomSelector;

	public LSIParametersTuner(GameDependentParameters gameDependentParameters, Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		super.setReferences(sharedReferencesCollector);

		this.randomSelector.setReferences(sharedReferencesCollector);

	}

	@Override
	public void clearComponent() {

		super.clearComponent();

		this.randomSelector.clearComponent();
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpComponent() {

		super.setUpComponent();

		this.randomSelector.setUpComponent();

		this.totalSamplesCounter = 0;

		int numRolesToTune;

		if(this.tuneAllRoles){
			numRolesToTune = this.gameDependentParameters.getNumRoles();
		}else{
			numRolesToTune = 1;
		}

		this.roleProblems = new LsiProblemRepresentation[numRolesToTune];

		int numSamplesPerValue = this.numGenSamples/this.parametersManager.getTotalNumPossibleValues(); // Same result as if we used floor(this.numGenSamples/this.parametersManager.getTotalNumPossibleValues();)

		List<Pair<CombinatorialCompactMove,Integer>> actionsToTest;

		// For each role for which we are tuning create the corresponding role problem
		for(int roleProblemIndex = 0; roleProblemIndex < this.roleProblems.length; roleProblemIndex++){

			// For each value x of each parameter we generate numSamplesPerValue sample combinations containing x,
			// completing the parameter combination with random values for the other parameters.
			actionsToTest = new ArrayList<Pair<CombinatorialCompactMove,Integer>>();

			for(int paramIndex = 0; paramIndex < this.parametersManager.getNumTunableParameters(); paramIndex++){
				for(int valueIndex = 0; valueIndex < this.parametersManager.getNumPossibleValues(paramIndex); valueIndex++){
					for(int i = 0; i < numSamplesPerValue; i++){
						actionsToTest.add(new Pair<CombinatorialCompactMove,Integer>(new CombinatorialCompactMove(this.randomlyCompleteCombinatorialMove(paramIndex,valueIndex)),new Integer(paramIndex)));
					}
				}
			}

			// Randomize order in which the combinations will be tested for each role so that the combinations
			// won't be tested always against the same combination for all the roles.
			Collections.shuffle(actionsToTest);

			this.roleProblems[roleProblemIndex] = new LsiProblemRepresentation(actionsToTest, this.parametersManager.getNumPossibleValuesForAllParams());
		}

	}

	private int[] randomlyCompleteCombinatorialMove(int paramIndex, int valueIndex){

		int[] combinatorialMove = new int[this.parametersManager.getNumTunableParameters()];
		for(int i = 0; i < combinatorialMove.length; i++){
			if(i == paramIndex){
				combinatorialMove[i] = valueIndex;
			}else{
				combinatorialMove[i] = -1;
			}
		}

		for(int i = 0; i < combinatorialMove.length; i++){
			if(i != paramIndex){
				combinatorialMove[i] = this.randomSelector.selectMove(new MoveStats[0],
						this.parametersManager.getValuesFeasibility(i, combinatorialMove), null, -1);
			}
		}

		return combinatorialMove;

	}

	@Override
	public void setNextCombinations() {

		/*
		if(generation){

			//

		}else if(evaluation){

		}else{
			return best;
		}
		*/

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
	public void updateStatistics(int[] rewards) {
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
