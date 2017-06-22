package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;

/**
 * This class keeps track (in a random order) of all possible combinations of individuals
 * (i.e. their indices) from a given number of populations and returns one combination at
 * a time represented by the index of each individual in the corresponding population.
 *
 * E.g. given two populations with 3 individuals each, this class will have the following
 * parameter:
 *
 * - combosOfIndividualsIndices = [0 0] [0 1] [0 2] [1 0] [1 1] [1 2] (note that the order
 *   of the combinations is shuffled every time a new iteration over the combinations starts).
 *
 * NOTE that this class assumes that the original populations are ordered and won't change the
 * ordering of their individuals while being iterated upon.
 *
 * @author C.Sironi
 *
 */
public class AllCombosOfIndividualsIterator extends CombosOfIndividualsIterator {

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

	public AllCombosOfIndividualsIterator(CompleteMoveStats[][] populations) {

		this.combosOfIndividualsIndices = new ArrayList<List<Integer>>();

		this.computeCombosOfCombosIndices(populations, new ArrayList<Integer>());

		Collections.shuffle(this.combosOfIndividualsIndices);

		this.currentComboIndex = 0;
	}

	private void computeCombosOfCombosIndices(CompleteMoveStats[][] populations, List<Integer> partialCombo){

		if(partialCombo.size() == populations.length){ // The combination of individuals is complete
			this.combosOfIndividualsIndices.add(new ArrayList<Integer>(partialCombo));
		}else{
			for(int i = 0; i < populations[partialCombo.size()].length; i++){
				partialCombo.add(new Integer(i));
				this.computeCombosOfCombosIndices(populations, partialCombo);
				partialCombo.remove(partialCombo.size()-1);
			}
		}

	}

	@Override
	public List<Integer> getCurrentComboOfIndividualsIndices() {
		return this.combosOfIndividualsIndices.get(this.currentComboIndex);
	}

	@Override
	public List<Integer> getNextComboOfIndividualsIndices() {
		this.currentComboIndex++;

		if(this.currentComboIndex < this.combosOfIndividualsIndices.size()){
			return this.getCurrentComboOfIndividualsIndices();
		}else{
			return null;
		}
	}

	@Override
	public void startNewIteration() {

		Collections.shuffle(this.combosOfIndividualsIndices);

		this.currentComboIndex = 0;

	}

}
