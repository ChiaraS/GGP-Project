package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.fitness;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.logging.GamerLogger;

import csironi.ggp.course.utils.MyPair;

/**
 * This class computes the fitness by considering each individual with the same index
 * as the same individual. For each individual considers the maximum score achieved by
 * any of the roles that the individual controlled. One point (i.e. one win) is then
 * split equally by all distinct individuals that achieved the maximum score (NOTE that
 * this is the same technique used to compute the wins statistics when performing the
 * experiments).
 *
 * HOWEVER, if the game has only one player (and thus only one individual at a time is
 * evaluated) the fitness of the individual will be its score rescaled between 0 and 1
 * (NOTE that this instead is different from what is done when computing the win percentage
 * for single-player games when performing the experiments).
 *
 * @author c.sironi
 *
 */
public class WinFitnessComputer extends FitnessComputer {

	public WinFitnessComputer(GameDependentParameters gameDependentParameters, Random random,
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

	/**
	 * Note that this method looses track of which role each individual was controlling.
	 * Moreover, if the same individual was used more than once it is returned only once with one score.
	 * Thus, this method should be used only when the fitness is used to update the statistics of the
	 * same population for all individuals, not when each individual is from a different population.
	 */
	@Override
	public List<MyPair<Integer,Double>> computeFitness(List<Integer> individuals, double[] rewards){

		if(individuals.size() != rewards.length){
			GamerLogger.logError("FitnessComputer", "WinFitnessComputer - Impossible to compute fitness for " + individuals.size() +
					" individual(s) with " + rewards.length + " reward(s)!");
			throw new RuntimeException("WinFitnessComputer - Impossible to compute fitness for " + individuals.size() +
					" individual(s) with " + rewards.length + " reward(s)!");
		}

		// For each unique individual, compute the corresponding score.
		List<MyPair<Integer,Double>> individualsWithScore = new ArrayList<MyPair<Integer,Double>>();

		// Single player: return the same score
		if(individuals.size() == 1){
			individualsWithScore.add(new MyPair<Integer,Double>(individuals.get(0), rewards[0]));
		}else{
        	// For more roles we need to find the individual(s) that won and split 100 between them
			double maxScore = -Double.MAX_VALUE;
        	double splitScore;
        	Set<Integer> distinctIndividuals = new HashSet<Integer>();
        	Set<Integer> maxScoreIndividuals = new HashSet<Integer>();

        	// Find the distinct individual(s) that got highest score
        	for(int i = 0; i < rewards.length; i++){
        		distinctIndividuals.add(individuals.get(i));
        		if(rewards[i] > maxScore){
        			maxScore = rewards[i];
        			maxScoreIndividuals.clear();
        			maxScoreIndividuals.add(individuals.get(i));
        		}else if(rewards[i] == maxScore){
        			maxScoreIndividuals.add(individuals.get(i));
        		}
        	}

        	// Split 100 among the distinct individual(s) that got highest score
        	splitScore = 100.0/((double)maxScoreIndividuals.size());

        	// Memorize the score for every player
        	for(Integer individual : distinctIndividuals){
        		if(maxScoreIndividuals.contains(individual)){
        			individualsWithScore.add(new MyPair<Integer,Double>(individual, splitScore));
        		}else{
        			individualsWithScore.add(new MyPair<Integer,Double>(individual, 0.0));
        		}
        	}
        }

		return individualsWithScore;
	}

	@Override
	public String getComponentParameters(String indentation) {
		return null;
	}

}
