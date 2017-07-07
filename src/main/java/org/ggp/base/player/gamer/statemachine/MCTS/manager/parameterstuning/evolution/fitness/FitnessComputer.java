package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.fitness;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

import csironi.ggp.course.utils.MyPair;

/**
 * Given a list of individuals and the corresponding rewards obtained by the last simulation
 * this class computes and returns the fitness for each individual.
 *
 * @author c.sironi
 *
 */
public abstract class FitnessComputer extends SearchManagerComponent {

	public FitnessComputer(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// Do nothing
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	public abstract List<MyPair<Integer,Double>> computeFitness(List<Integer> individuals, int[] rewards);

	@Override
	public String getComponentParameters(String indentation) {
		return null;
	}

}
