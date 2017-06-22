package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.util.logging.GamerLogger;

/**
 * This class keeps track for each population of the index of each individual in the
 * population. It iterates over combinations of individuals as follows:
 *
 * For each population p in {p_0,...,p_n}:
 * 		shuffle the individuals in p
 * EndFor
 * For (i = 0; i < p_0.length; i++)
 * 		return combination of individuals (p_0[i],...,p_n[i])
 * EndFor
 *
 * When done iterating, if the method startNewIteration() is called, the same process is repeated.
 *
 * Note that this procedure assumes that all populations have the same length! For now it is always
 * guaranteed that populations have the same size. Adapt this class if it becomes necessary for it
 * to deal with populations of different sizes.
 *
 * @author C.Sironi
 *
 */
public class RandomCombosOfIndividualsIterator extends CombosOfIndividualsIterator {

	private List<List<Integer>> individualsIndicesPerPopulation;

	private int currentIndex;

	public RandomCombosOfIndividualsIterator(CompleteMoveStats[][] populations) {

		if(populations == null || populations.length < 1){
			GamerLogger.logError("CombosOfIndividualsIterator", "RandomCombosOfIndividualsIterator - Impossible to create RandomCombosOfIndividualsIterator! No populations specified!");
			throw new RuntimeException("RandomCombosOfIndividualsIterator - Impossible to create RandomCombosOfIndividualsIterator! No populations specified!");
		}
		// Used to check if all populations have the same size
		int popSize = populations[0].length;

		this.individualsIndicesPerPopulation = new ArrayList<List<Integer>>();

		ArrayList<Integer> individualsIndicesOfCurrentPopulation;

		for(int populationIndex = 0; populationIndex < populations.length; populationIndex++){

			if(populations[populationIndex].length != popSize){
				GamerLogger.logError("CombosOfIndividualsIterator", "RandomCombosOfIndividualsIterator - Impossible to create RandomCombosOfIndividualsIterator! Specified populations have different size!");
				throw new RuntimeException("RandomCombosOfIndividualsIterator - Impossible to create RandomCombosOfIndividualsIterator! Specified populations have different size!");
			}

			individualsIndicesOfCurrentPopulation = new ArrayList<Integer>();

			for(int individualIndex = 0; individualIndex < populations[populationIndex].length; individualIndex++){
				individualsIndicesOfCurrentPopulation.add(new Integer(individualIndex));
			}

			Collections.shuffle(individualsIndicesOfCurrentPopulation);

			this.individualsIndicesPerPopulation.add(individualsIndicesOfCurrentPopulation);
		}

		this.currentIndex = 0;
	}

	@Override
	public List<Integer> getCurrentComboOfIndividualsIndices() {

		List<Integer> currentCombo = new ArrayList<Integer>();

		for(int populationIndex = 0; populationIndex < this.individualsIndicesPerPopulation.size(); populationIndex++){
			currentCombo.add(this.individualsIndicesPerPopulation.get(populationIndex).get(this.currentIndex));
		}

		return currentCombo;
	}

	@Override
	public List<Integer> getNextComboOfIndividualsIndices() {
		this.currentIndex++;

		if(this.currentIndex < this.individualsIndicesPerPopulation.get(0).size()){ // Then we have the next individual for all populations because we guarantee that all populations have the same size.
			return this.getCurrentComboOfIndividualsIndices();
		}else{
			return null;
		}
	}

	@Override
	public void startNewIteration() {

		for(List<Integer> populationIndices : this.individualsIndicesPerPopulation){
			Collections.shuffle(populationIndices);
		}

		this.currentIndex = 0;

	}

}
