package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.util.List;

/**
 * This class is used by some tuners to keep track of the next combination to test for each role.
 * This class (for now) is used by evolutionary tuners that evolve multiple populations (one for
 * each role) at the same time. These tuners need to know which combinations to match against each
 * other for each role. This class keeps track of the next combination of combinations that must be
 * tested.
 *
 * For example, given one population for each role we might want to evaluate each individual of each
 * population against ALL combinations of individuals of the other populations. In this case, this
 * class will keep track of all possible combinations of combinations (i.e. their indices in the
 * population) and return one combination of combinations at a time.
 *
 * An alternative is that we want to evaluate random combinations of combinations so that in the end
 * each individual is evaluated at least x times. In this case, this class will order the individuals
 * of each population randomly and return them in order for each population. This causes each individual
 * to be evaluated exactly once against random individuals from other populations. The same process can
 * be repeated x times to have x evaluations for each individual.
 *
 * @author C.Sironi
 *
 */
public abstract class CombosOfIndividualsIterator {

	/**
	 * Returns the currently considered combination of individuals.
	 *
	 * @return
	 */
	public abstract List<Integer> getCurrentComboOfIndividualsIndices();

	/**
	 * Advances to and returns the next combination of individuals.
	 * If there is no next combination because all combinations have been iterated upon, returns null.
	 * @return
	 */
	public abstract List<Integer> getNextComboOfIndividualsIndices();

	/**
	 * Starts a new iteration over the combinations of individuals.
	 * NOTE that this method is meant to be called once an iteration is over, thus when the
	 * getNextComboOfIndividualsIndices() method has returned null.
	 * This method must also be called after creating the class to set up the first iteration.
	 * @return
	 */
	public abstract void startNewIteration();

}
