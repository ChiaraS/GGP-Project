package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class keeps track (in a random order) of all possible combinations of individuals
 * (i.e. their indices) from a given number of populations and returns one combination at
 * a time represented by the index of each individual in the corresponding population.
 *
 * E.g. given two populations with 3 individuals each, this class will have the following
 * parameter:
 *
 * - combosOfIndividualsIndices = [0 0] [0 1] [0 2] [1 0] [1 1] [1 2] [2 0] [2 1] [2 2]
 *   (note that the order of the combinations is shuffled every time a new iteration over the
 *   combinations starts).
 *
 * NOTE that this class assumes that the original populations are ordered and won't change the
 * ordering of their individuals while being iterated upon.
 *
 * This class can also be used to compute all possible combinations of n individuals from the
 * same population, by specifying n as number of populations and the size of the only considered
 * population as populationsSize. When computing all the combinations of n individuals from the
 * same population, removeSameIndexCombos can be specified as true to cause the combinations that
 * repeat the same individual n times to be excluded from the possible combinations.
 * E.g. given a population with 3 individuals, if we want all combinations of 2 individuals from
 * the population, this class will produce the following:
 *
 * 	- combosOfIndividualsIndices = [0 0] [0 1] [0 2] [1 0] [1 1] [1 2] [2 0] [2 1] [2 2], if the input
 *    of the constructor is (numPopulations = 2; populationsSize = 3; removeSameIndexCombos = false)
 *
 * 	- combosOfIndividualsIndices = [0 1] [0 2] [1 0] [1 2] [2 0] [2 1], if the input of the constructor
 * 	  is (numPopulations = 2; populationsSize = 3; removeSameIndexCombos = true)
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

	public AllCombosOfIndividualsIterator(int[] populationsSizes) {
		this(populationsSizes, false);
	}

	/**
	 *
	 * @param populationsSizes array specifying the size of each of the considered populations.
	 * @param removeSameIndexCombos true if combinations with the same index for each population must be excluded.
	 */
	public AllCombosOfIndividualsIterator(int[] populationsSizes, boolean removeSameIndexCombos) {

		this.combosOfIndividualsIndices = new ArrayList<List<Integer>>();

		this.computeCombosOfCombosIndices(populationsSizes, removeSameIndexCombos, new ArrayList<Integer>());

		// Initialize to an index such that (this.currentComboIndex >= this.combosOfIndividualsIndices.size())
		// In this way both methods getCurrentComboOfIndividualsIndices() and getNextComboOfIndividualsIndices()
		// will return null and it will be clear that a new iteration has to be started.
		this.currentComboIndex = this.combosOfIndividualsIndices.size();
	}

	private void computeCombosOfCombosIndices(int[] populationsSizes, boolean removeSameIndexCombos, List<Integer> partialCombo){

		if(partialCombo.size() == populationsSizes.length){ // The combination of individuals is complete
			if(!removeSameIndexCombos || !this.isSameIndexCombo(partialCombo)){
				this.combosOfIndividualsIndices.add(new ArrayList<Integer>(partialCombo));
			}
		}else{
			for(int i = 0; i < populationsSizes[partialCombo.size()]; i++){
				partialCombo.add(new Integer(i));
				this.computeCombosOfCombosIndices(populationsSizes, removeSameIndexCombos, partialCombo);
				partialCombo.remove(partialCombo.size()-1);
			}
		}

	}

	private boolean isSameIndexCombo(List<Integer> combo){

		if(combo.size() < 2){
			return false;
		}

		int firstIndex = combo.get(0).intValue();

		for(int i = 1; i < combo.size(); i++){
			if(combo.get(i).intValue() != firstIndex){
				return false;
			}
		}

		return true;

	}

	@Override
	public List<Integer> getCurrentComboOfIndividualsIndices() {

		if(this.currentComboIndex < this.combosOfIndividualsIndices.size()){
			return this.combosOfIndividualsIndices.get(this.currentComboIndex);
		}else{
			return null;
		}
	}

	@Override
	public List<Integer> getNextComboOfIndividualsIndices() {
		this.currentComboIndex++;

		return this.getCurrentComboOfIndividualsIndices();
	}

	@Override
	public void startNewIteration() {

		Collections.shuffle(this.combosOfIndividualsIndices);

		this.currentComboIndex = 0;

	}

	@Override
	public String toString(){

		String combos = "[ ";
		for(List<Integer> combo : this.combosOfIndividualsIndices){
			combos += "[ ";
			for(Integer index : combo){
				combos += index.intValue() + " ";
			}
			combos += "] ";
		}
		combos += "]";

		return "Combos = " + combos + "\nCurrentComboIndex = " + this.currentComboIndex;

	}

	public static void main(String[] args){

		int[] ps = new int[3];
		for(int i = 0; i < ps.length; i++){
			ps[i] = 3;
		}

		AllCombosOfIndividualsIterator iterator1 = new AllCombosOfIndividualsIterator(ps);

		AllCombosOfIndividualsIterator iterator2 = new AllCombosOfIndividualsIterator(ps, true);

		System.out.println("ITERATOR1:");
		System.out.println(iterator1);
		System.out.println();
		System.out.println("ITERATOR2:");
		System.out.println(iterator2);
		System.out.println();


	}

}
