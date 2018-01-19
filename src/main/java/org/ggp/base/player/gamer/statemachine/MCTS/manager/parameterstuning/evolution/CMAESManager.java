package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.CombinatorialCompactMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ContinuousMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.SelfAdaptiveESProblemRepresentation;
import org.ggp.base.util.logging.GamerLogger;

import csironi.ggp.course.utils.MyPair;
import inriacmaes.CMAEvolutionStrategy;

public class CMAESManager extends ContinuousEvolutionManager {

	public CMAESManager(GameDependentParameters gameDependentParameters, Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		// Do nothing
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
	 * Returns a new CMA-ES strategy instance and the corresponding initial population that will be used
	 * to tune the parameters of a role.
	 */
	public MyPair<CMAEvolutionStrategy,CompleteMoveStats[]> getInitialCMAESPopulation() {

		CMAEvolutionStrategy cma = new CMAEvolutionStrategy();

		// Is this really necessary? When calling init(), if no properties were read the CMAEvolutionStrategy should find
		// default values for the properties in the classes CMAParameters and CMAOptions. TODO: verify if this is true.
		//cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties

		// Set number of tuned parameters
        cma.setDimension(this.continuousParametersManager.getNumTunableParameters()); // overwrite some loaded properties

        // Set population size only if specified, otherwise use default
        if(this.populationsSize > 0) {
        	cma.parameters.setPopulationSize(this.populationsSize);
        }

        // Initialize the CMA-ES algorithm
        cma.init();

        if(cma.stopConditions.getNumber() > 0) { // Termination before even being able to start

        	String[] satisfiedTerminationCriteria = cma.stopConditions.getMessages();
        	String satisfiedTerminationCriteriaString = satisfiedTerminationCriteria[0];
        	for(int i = 1; i < satisfiedTerminationCriteria.length; i++) {
        		satisfiedTerminationCriteriaString += (" - " + satisfiedTerminationCriteria[i]);
        	}

        	GamerLogger.logError("EvolutionManager", "CMAESManager - Impossible to initialize population. Termination criteria satisfied before CMA-ES algortihm could even start optimization: " + satisfiedTerminationCriteriaString);
			throw new RuntimeException("CMAESManager - Impossible to initialize population. Termination criteria satisfied before CMA-ES algortihm could even start optimization: " + satisfiedTerminationCriteriaString);

        }

        // Sample the initial population
        double[][] pop = cma.samplePopulation();

        // Transform the initial population into a population of statistics for ContinuousMoves
        // (i.e. each individual is represented by an instance of CompleteMoveStats, that contains:
        // 1. the individual as a combination of parameter values rescaled from [-inf,+inf] to its
        // own interval of feasible values
        // 2. the sum of the fitness obtained by all evaluations of the individual
        // 3. the number of times the individual has been evaluated
		CompleteMoveStats[] population = this.scaleDownPopulation(pop);

		return new MyPair<CMAEvolutionStrategy,CompleteMoveStats[]>(cma,population);

	}

	/**
	 * Transforms the population sampled by CMA-ES to a population of individuals with their statistics
	 * by scaling down the values of the parameters from [-inf,+inf] to the interval of feasible values
	 * for each parameter.
	 * @return
	 */
	private CompleteMoveStats[] scaleDownPopulation(double[][] pop) {
        // Transform the initial population into a population of statistics for ContinuousMoves
        // (i.e. each individual is represented by an instance of CompleteMoveStats, that contains:
        // 1. the individual as a combination of parameter values rescaled from [-inf,+inf] to its
        // own interval of feasible values
        // 2. the sum of the fitness obtained by all evaluations of the individual
        // 3. the number of times the individual has been evaluated
		CompleteMoveStats[] population = new CompleteMoveStats[pop.length];

		double[] rescaledValuesOfIndividual;

		for(int individualIndex = 0; individualIndex < pop.length; individualIndex++) {
			rescaledValuesOfIndividual = new double[pop[individualIndex].length];
			for(int paramIndex = 0; paramIndex < pop[individualIndex].length; paramIndex++) {
				rescaledValuesOfIndividual[paramIndex] =
						new org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.utils.Utils().mapToInterval(
								this.continuousParametersManager.getPossibleValuesInterval(paramIndex).getRightExtreme(),
								this.continuousParametersManager.getPossibleValuesInterval(paramIndex).getLeftExtreme(),
								pop[individualIndex][paramIndex]);
			}
			population[individualIndex] = new CompleteMoveStats(new ContinuousMove(rescaledValuesOfIndividual));
		}

		return population;
	}

	public void evolvePopulation(SelfAdaptiveESProblemRepresentation roleProblem) {

		// If a role problem is passed to this function we know that the CMA-ES has not been stopped yet for the role

		double[] fitness = new double[roleProblem.getCMAEvolutionStrategy().parameters.getPopulationSize()];
		for(CompleteMoveStats stat : roleProblem.getPopulation()) {

			// Compute fitness and invert it because CMA-ES minimized the function, while we want to maximize


		}

		roleProblem.getCMAEvolutionStrategy().updateDistribution(fitness);

		if(roleProblem.getCMAEvolutionStrategy().stopConditions.getNumber() > 0) { // Stop optimization

			// TODO: compute fitness of mean
			//roleProblem.getCMAEvolutionStrategy().setFitnessOfMeanX(fitnessOfMeanComputedByAnMCTSSimulation)


		}else { // Get new population

		}








		// The size of the elite must be at least 1
		if(this.eliteSize <= 0){
			GamerLogger.logError("EvolutionManager", "StandardEvolutionManager - Impossible to evolve the population. Elite size " + this.eliteSize + " <= 0.");
			throw new RuntimeException("StandardEvolutionManager - Impossible to evolve the population. Elite size " + this.eliteSize + " <= 0.");
		}

		Arrays.sort(roleProblem.getPopulation(),
				new Comparator<CompleteMoveStats>(){

			@Override
			public int compare(CompleteMoveStats o1, CompleteMoveStats o2) {

				double value1;
				if(o1.getVisits() == 0){
					value1 = 0;
				}else{
					value1 = o1.getScoreSum()/o1.getVisits();
				}
				double value2;
				if(o2.getVisits() == 0){
					value2 = 0;
				}else{
					value2 = o2.getScoreSum()/o2.getVisits();
				}
				// Sort from largest to smallest
				if(value1 > value2){
					return -1;
				}else if(value1 < value2){
					return 1;
				}else{
					return 0;
				}
			}

		});

		// For the individuals that we are keeping, reset all statistics.
		/*
		for(int i = 0; i < this.eliteSize; i++){
			roleProblem.getPopulation()[i].resetStats();
		}
		*/

		// For other individuals, generate new individuals to substitute them and reset also statistics.
		// Keep the first eliteSize best individuals and create new individuals
		// to substitute the ones that are being thrown away.
		for(int i = this.eliteSize; i < roleProblem.getPopulation().length; i++){

			if(this.random.nextDouble() < this.crossoverProbability){
				// Create new individual with crossover
				roleProblem.getPopulation()[i].resetStats(this.crossoverManager.crossover(((CombinatorialCompactMove)roleProblem.getPopulation()[this.random.nextInt(this.eliteSize)].getTheMove()),
						((CombinatorialCompactMove)roleProblem.getPopulation()[this.random.nextInt(this.eliteSize)].getTheMove())));
			}else{
				// Create new individual with mutation
				roleProblem.getPopulation()[i].resetStats(this.mutationManager.mutation(((CombinatorialCompactMove)roleProblem.getPopulation()[this.random.nextInt(this.eliteSize)].getTheMove())));
			}

		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		return super.getComponentParameters(indentation);

	}

}
