package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.EvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;

/**
 * This class tunes the parameters for each role independently. Each role has its own population.
 * When evaluating the current populations, all combinations of individuals, one from each population,
 * are tested in a MCTS simulation.
 *
 * E.g. if we have the following populations:
 * 	p_0 = {c_00; c_01; c_02}
 * 	p_1 = {c_10; c_11; c_12}
 *	p_2 = {c_20; c_21; c_22}
 * where c_ij is combination j of population of role i, when we want to compute the fitness of each
 * combination (i.e. individual) we perform k MCTS simulations for each possible combination of
 * combinations. Thus we will run MCTS simulation with the following combinations of combinations:
 * (c_00, c_10, c20) (c_00, c_10, c21) (c_00, c_10, c22) (c_00, c_11, c20) (c_00, c_11, c21) (c_00, c_11, c22)
 * (c_00, c_12, c20) (c_00, c_12, c21) (c_00, c_12, c22) (c_01, c_10, c20) (c_01, c_10, c21) (c_01, c_10, c22)
 * (c_01, c_11, c20) (c_01, c_11, c21) (c_01, c_11, c22) (c_01, c_12, c20) (c_01, c_12, c21) (c_01, c_12, c22)
 * (c_02, c_10, c20) (c_02, c_10, c21) (c_02, c_10, c22) (c_02, c_11, c20) (c_02, c_11, c21) (c_02, c_11, c22)
 * (c_02, c_12, c20) (c_02, c_12, c21) (c_02, c_12, c22).
 *
 * @author C.Sironi
 *
 */
public class MultiPopEvoParametersTuner extends ParametersTuner {

	/**
	 * Takes care of evolving a given population depending on the fitness of its individuals.
	 */
	private EvolutionManager evolutionManager;

	/**
	 * Size of the populations. It's the same for all roles.
	 */
	private int populationsSize;

	/**
	 * Number of time all possible combinations of combinations (i.e. individuals) must be evaluated
	 * before using the collected statistics to evolve the population.
	 */
	private int evalRepetitions;

	/**
	 * Used to count the repetitions performed so far.
	 */
	private int evalRepetitionsCount;

	/**
	 * One population of combinations (individuals) for each role being tuned.
	 */
	private CompleteMoveStats[][] populations;

	/**
	 * List with the indices of all possible combinations that can be obtained by taking one
	 * combination (i.e individual) for each role.
	 */
	private List<List<Integer>> combosOfIndividualsIndices;

	/**
	 * Index of the currently tested combination of combinations (i.e. individuals) in the
	 * combosOfCombosIndices list.
	 */
	private int currentComboIndex;


	public MultiPopEvoParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector){
		super.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		super.clearComponent();
	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();

		// combosOfCombosIndices is fixed for the whole game until we change number of roles, so
		// we can initialize it here.

		this.combosOfIndividualsIndices = new ArrayList<List<Integer>>();

		this.computeCombosOfCombosIndices(new ArrayList<Integer>());

		Collections.shuffle(this.combosOfIndividualsIndices);

		this.currentComboIndex = 0;
	}

	private void computeCombosOfCombosIndices(List<Integer> partialCombo){

		if(partialCombo.size() == this.populations.length){ // The combination of individuals is complete
			this.combosOfIndividualsIndices.add(new ArrayList<Integer>(partialCombo));
		}else{
			for(int i = 0; i < this.populations[partialCombo.size()].length; i++){
				partialCombo.add(new Integer(i));
				this.computeCombosOfCombosIndices(partialCombo);
				partialCombo.remove(partialCombo.size()-1);
			}
		}

	}

	@Override
	public void setNextCombinations() {

		 int[][] nextCombinations = new int[this.populations.length][];

		 List<Integer> individualsIndices = this.combosOfIndividualsIndices.get(this.currentComboIndex);

		 Move theParametersCombination;

		 for(int populationIndex = 0; populationIndex < this.populations.length; populationIndex++){
			 theParametersCombination = this.populations[populationIndex][individualsIndices.get(populationIndex)].getTheMove();
			 if(theParametersCombination instanceof CombinatorialCompactMove){
				 nextCombinations[populationIndex] = ((CombinatorialCompactMove) theParametersCombination).getIndices();
			 }else{
				 GamerLogger.logError("ParametersTuner", "MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
				 throw new RuntimeException("MultiPopEvoParametersTuner - Impossible to set next combinations. The Move is not of type CombinatorialCompactMove but of type " + theParametersCombination.getClass().getSimpleName() + ".");
			 }
		 }

		 this.parametersManager.setParametersValues(nextCombinations);

	}

	@Override
	public void setBestCombinations() {
		// TODO Auto-generated method stub

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

	@Override
	public String getComponentParameters(String indentation) {

		return "";
	}

}
