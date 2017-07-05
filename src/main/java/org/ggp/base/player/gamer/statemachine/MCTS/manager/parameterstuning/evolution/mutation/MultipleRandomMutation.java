package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;

public class MultipleRandomMutation extends MutationManager {

	/**
	 * If creating a new individual with mutation of single parent, for each gene of the individual (i.e.
	 * parameter in the combination) change to a random value with probability geneMutationProbability,
	 * and leave the same value with probability (1-geneMutationProbability).
	 */
	private double geneMutationProbability;

	public MultipleRandomMutation(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.geneMutationProbability = gamerSettings.getDoublePropertyValue("MutationManager.geneMutationProbability");

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
	 * IMPLEMENTATION OF UNIFORM RANDOM MUTATION
	 * Given a parent, creates a child by mutating each of its genes (parameters) with probability geneMutationProbability.
	 * If a gene must mutate, then a random value is selected for it among the feasible values.
	 *
	 * @param parent
	 * @return
	 */
	@Override
	public CombinatorialCompactMove mutation(CombinatorialCompactMove parent){

		// NOTE! We have to keep in mind that there is a constraint on K and Ref when mutating parameter values.

		int[] parentCombo = parent.getIndices();

		int[] childCombo = new int[parentCombo.length];

		List<Integer> paramIndicesToMutate = new ArrayList<Integer>();

		// First of all check which parameters will have to mutate and which will stay the same.
		// For the parameters that will mutate, initialize the value as -1. Copy the value of the other parameters.
		for(int paramIndex = 0; paramIndex < parentCombo.length; paramIndex++){
			if(this.random.nextDouble() < this.geneMutationProbability){ // Mutate
				childCombo[paramIndex] = -1;
				paramIndicesToMutate.add(new Integer(paramIndex));
			}else{ // Keep value.
				childCombo[paramIndex] = parentCombo[paramIndex];
			}
		}

		// If nothing has to mutate, return.
		if(!paramIndicesToMutate.isEmpty()){
			// Mutate values in a random order (to guarantee more fairness when K and Ref are involved - it's
			// more fair to alternate which one is picked first since they influence each other's values).
			Collections.shuffle(paramIndicesToMutate);

			List<Integer> feasibleValues;

			for(Integer paramIndex : paramIndicesToMutate){
				feasibleValues = this.parametersManager.getFeasibleValues(paramIndex.intValue(), childCombo);
				childCombo[paramIndex.intValue()] = feasibleValues.get(this.random.nextInt(feasibleValues.size())).intValue();
			}
		}

		return  new CombinatorialCompactMove(childCombo);

	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "GENE_MUTATION_PROBABILITY = " + this.geneMutationProbability;

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

}
