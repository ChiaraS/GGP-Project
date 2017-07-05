package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.crossover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;

public class UniformCrossover extends CrossoverManager {

	public UniformCrossover(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
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
	 * IMPLEMENTATION OF UNIFORM CROSSOVER
	 * Given two parents, creates a child where each gene (parameter value) is selected 50% of times
	 * from the first parent and 50% from the second parent.
	 * @param parent1
	 * @param parent2
	 * @return
	 */
	@Override
	public CombinatorialCompactMove crossover(CombinatorialCompactMove parent1, CombinatorialCompactMove parent2){

		// NOTE! We have to keep in mind that there is a constraint on K and Ref when mutating parameter values.
		// Crossover follows the following idea: for each parameter in a random order, select the value from one
		// of the two parents randomly. If this value makes the combination generated so far infeasible, use the
		// value of the other parent. If this value also makes the combination infeasible, pick for the parameter
		// a random value among the feasible ones.

		int[] parentCombo1 = parent1.getIndices();
		int[] parentCombo2 = parent2.getIndices();
		int[] childCombo = new int[parentCombo1.length];

		List<Integer> order = new ArrayList<Integer>();
		for(int paramIndex = 0; paramIndex < childCombo.length; paramIndex++){
			childCombo[paramIndex] = -1;
			order.add(new Integer(paramIndex));
		}

		Collections.shuffle(order);

		for(Integer paramIndex : order){
			if(this.random.nextDouble() < 0.5){
				// Get value from first parent
				childCombo[paramIndex.intValue()] = parentCombo1[paramIndex.intValue()];

				if(!this.parametersManager.isValid(childCombo)){

					// If combination is invalid, get value from second parent
					childCombo[paramIndex.intValue()] = parentCombo2[paramIndex.intValue()];

					if(!this.parametersManager.isValid(childCombo)){
						// If combination is still invalid, get random feasible value
						childCombo[paramIndex.intValue()] = -1;
						List<Integer> feasibleValues = this.parametersManager.getFeasibleValues(paramIndex.intValue(), childCombo);
						childCombo[paramIndex.intValue()] = feasibleValues.get(this.random.nextInt(feasibleValues.size())).intValue();
					}

				}

			}else{
				// Get value from second parent
				childCombo[paramIndex.intValue()] = parentCombo2[paramIndex.intValue()];

				if(!this.parametersManager.isValid(childCombo)){

					// If combination is invalid, get value from second parent
					childCombo[paramIndex.intValue()] = parentCombo1[paramIndex.intValue()];

					if(!this.parametersManager.isValid(childCombo)){
						// If combination is still invalid, get random feasible value
						childCombo[paramIndex.intValue()] = -1;
						List<Integer> feasibleValues = this.parametersManager.getFeasibleValues(paramIndex.intValue(), childCombo);
						childCombo[paramIndex.intValue()] = feasibleValues.get(this.random.nextInt(feasibleValues.size())).intValue();
					}

				}

			}

		}

		return new CombinatorialCompactMove(childCombo);

	}

}
