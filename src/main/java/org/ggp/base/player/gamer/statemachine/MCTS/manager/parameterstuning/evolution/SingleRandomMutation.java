package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

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

		int mutationIndex = this.random.nextInt(childCombo.length);

		childCombo[mutationIndex] = -1;

		List<Integer> feasibleValues = this.parametersManager.getFeasibleValues(mutationIndex, childCombo);
		childCombo[mutationIndex] = feasibleValues.get(this.random.nextInt(feasibleValues.size())).intValue();

		return  new CombinatorialCompactMove(childCombo);

	}

}
