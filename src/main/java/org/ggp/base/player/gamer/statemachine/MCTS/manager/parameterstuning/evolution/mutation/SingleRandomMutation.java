package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.mutation;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;

public class SingleRandomMutation extends MutationManager {

	public SingleRandomMutation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		super.clearComponent();
	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();
	}

	/**
	 * IMPLEMENTATION OF SINGLE RANDOM MUTATION
	 * Given a parent, creates a child by mutating only ONE of its genes (parameters) randomly.
	 * The gene (parameter) selected to mutate will be assigned a new value chosen randomly among
	 * the feasible values for the parameter.
	 *
	 * @param parent
	 * @return
	 */
	@Override
	public CombinatorialCompactMove mutation(CombinatorialCompactMove parent) {

		// NOTE! We have to keep in mind that there is a constraint on K and Ref when mutating parameter values.
		// It is possible that the parameter selected to be mutated has no other feasible values than the one
		// currently set (e.g. Ref is randomly selected to mutate, but K = 0 and Ref can only be -1), thus the
		// mutation will lead to the exact same individual.

		int[] parentCombo = parent.getIndices();

		int[] childCombo = new int[parentCombo.length];

		// Copy parent in children
		for(int paramIndex = 0; paramIndex < parentCombo.length; paramIndex++){
			childCombo[paramIndex] = parentCombo[paramIndex];
		}

		// PROBLEM: as it was, the code was always preventing combinations with K=0 and Ref=-1 to be
		// generated. This was because we only change one parameter value, so if we have K!=0 and we want
		// to change Ref, -1 won't be a feasible value, while if we have Ref!=-1 and we want to change K,
		// 0 won't be a feasible value. Until we change K to 0 we won't be able to change Ref to -1 and
		// viceversa, so the two values block each other from being selected. It was possible to have
		// K=0 and Ref=-1 only if the values were set for an individual in the initial population that is
		// selected randomly.
		// As a quick fix, whenever K(Ref) is selected to be mutated, we allow to choose any of its feasible
		// values independently of the value of Ref(K) (but still considering possible conditions on values
		// given by other parameters). Then, if the current value of Ref(K) is not feasible given the new
		// value of K(Ref), we select a new feasible value for Ref(K).
		int mutationIndex = this.random.nextInt(childCombo.length);

		// Check if the parameter to mutate is K and Ref is also being tuned
		if(mutationIndex == this.discreteParametersManager.getIndexOfK() && this.discreteParametersManager.getIndexOfRef() != -1) {
				this.conditionalMutation(childCombo, this.discreteParametersManager.getIndexOfK(), this.discreteParametersManager.getIndexOfRef());
			}else if(mutationIndex == this.discreteParametersManager.getIndexOfRef() && this.discreteParametersManager.getIndexOfK() != -1) { // Check if the parameter to mutate is Ref and K is also being tuned
				this.conditionalMutation(childCombo, this.discreteParametersManager.getIndexOfRef(), this.discreteParametersManager.getIndexOfK());
			}else { // Otherwise just proceed normally
				childCombo[mutationIndex] = -1;
				List<Integer> feasibleValues = this.discreteParametersManager.getFeasibleValues(mutationIndex, childCombo);
				childCombo[mutationIndex] = feasibleValues.get(this.random.nextInt(feasibleValues.size())).intValue();
			}

		return  new CombinatorialCompactMove(childCombo);

	}

	/**
	 * This method changes a combination of parameters so that the value for the parameter at firstIndex
	 * is selected independently from the value set for the parameter at secondIndex. Then if the value
	 * for the parameter at secondIndex is not anymore feasible given the new value for the parameter at
	 * firstIndex, the value of the parameter at secondIndex is re-set to a new random feasible value.
	 *
	 * @param childCombo a complete combination that we want to mutate.
	 * @param firstIndex index in the combination of the parameter that must be mutated independently of the
	 * value of the parameter at index secondIndex.
	 * @param secondIndex index in the combination of the parameter whose value must be checked and changed
	 * if not feasible after the parameter at position firstIndex has been changed.
	 */
	private void conditionalMutation(int[] childCombo, int firstIndex, int secondIndex) {
		// Reset index of value for firstIndex in the child combo
		childCombo[firstIndex] = -1;
		// Save index of value for secondIndex
		int valueAtSecondIndex = childCombo[secondIndex];
		// Reset index of value for secondIndex in the child combo
		childCombo[secondIndex] = -1;
		// Get all feasible values for firstIndex and select one
		List<Integer> feasibleValues = this.discreteParametersManager.getFeasibleValues(firstIndex, childCombo);
		childCombo[firstIndex] = feasibleValues.get(this.random.nextInt(feasibleValues.size())).intValue();
		// Get all feasible values for secondIndex given the value for firstIndex
		feasibleValues =  this.discreteParametersManager.getFeasibleValues(secondIndex, childCombo);
		// If the previous value is still feasible, set it. Otherwise select a new random value for Ref among the feasible ones.
		if(feasibleValues.contains(new Integer(valueAtSecondIndex))) {
			childCombo[secondIndex] = valueAtSecondIndex;
		}else {
			childCombo[secondIndex] = feasibleValues.get(this.random.nextInt(feasibleValues.size())).intValue();
		}
	}

}
