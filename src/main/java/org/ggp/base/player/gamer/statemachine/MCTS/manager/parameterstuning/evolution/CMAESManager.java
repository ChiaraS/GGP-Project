package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ContinuousMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.SelfAdaptiveESProblemRepresentation;
import org.ggp.base.util.logging.GamerLogger;

import csironi.ggp.course.utils.MyPair;
import inriacmaes.CMAEvolutionStrategy;

public class CMAESManager extends ContinuousEvolutionManager {

	/**
	 * True if when returning a solution before the CMA-ES algorithm has terminated we want to return the
	 * mean value, even if it has not been evaluated yet. The mean value is expected to have the best fitness,
	 * however if the tuning was interrupted before we could have the chance to evaluate it, we cannot know for
	 * sure. If mean==true we set it as best value anyway, otherwise we query the CMA-ES class to get the best
	 * value evaluated so far. Note that if we did have time to evaluate the mean at least once after the end of
	 * the optimization with the CMA-ES algorithm, we query the CMA-ES instance for the best solution evaluated
	 * so far (which might be the mean), ignoring this class field.
	 * NOTE that returning the mean or the best value when the execution of the algorithm is stopped before its
	 * termination makes absolutely no difference because the game is over and the returned combination will not
	 * be used. The only case when it makes a difference is if we want to re-use the best combination to play
	 * the same game again.
	 */
	private boolean useMean;

	public CMAESManager(GameDependentParameters gameDependentParameters, Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.useMean = gamerSettings.getBooleanPropertyValue("EvolutionManager.useMean");
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

		System.out.println("Before = " + cma.options.stopFitness);

		// Is this really necessary? When calling init(), if no properties were read the CMAEvolutionStrategy should find
		// default values for the properties in the classes CMAParameters and CMAOptions. TODO: verify if this is true.
		//cma.readProperties(); // read options, see file CMAEvolutionStrategy.properties

		// Set essential parameters
		// TODO: here we set the parameters that must be specified. Fix the code to read them from a properties file
		// maybe the same as used for the other settings of the agent?
		//cma.readProperties();
		// Set number of tuned parameters
        cma.setDimension(this.continuousParametersManager.getNumTunableParameters()); // overwrite some loaded properties
        // Set initial standard deviation (same as in default settings file)
        cma.setInitialStandardDeviation(0.3);
        // Set initial X (same as in default settings file)
        cma.setInitialX(0.5);
        // Set minimum fitness we want to reach
        cma.options.stopFitness = -101.0;
        // Set minimum change in function value that must be observed for current population
        // wrt the last 10+ceil(30*dimensions/lambda) iterations???
        //cma.options.

        System.out.println("After = " + cma.options.stopFitness);

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

		for(int individualIndex = 0; individualIndex < pop.length; individualIndex++) {
			population[individualIndex] = this.scaleDownIndividual(pop[individualIndex]);
		}

		return population;
	}

	private CompleteMoveStats scaleDownIndividual(double[] individual) {
		double[] rescaledValuesOfIndividual = new double[individual.length];
		for(int paramIndex = 0; paramIndex < individual.length; paramIndex++) {
			rescaledValuesOfIndividual[paramIndex] =
					new org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.utils.Utils().mapToInterval(
							this.continuousParametersManager.getPossibleValuesInterval(paramIndex).getRightExtreme(),
							this.continuousParametersManager.getPossibleValuesInterval(paramIndex).getLeftExtreme(),
							individual[paramIndex]);
		}
		return new CompleteMoveStats(new ContinuousMove(rescaledValuesOfIndividual));

	}

	public void evolvePopulation(SelfAdaptiveESProblemRepresentation roleProblem) {

		// If a role problem is passed to this function we know that the CMA-ES has not been stopped yet for the role

		roleProblem.getCMAEvolutionStrategy().updateDistribution(this.computeFitness(roleProblem.getPopulation()));

		if(roleProblem.getCMAEvolutionStrategy().stopConditions.getNumber() > 0) { // Stop optimization

			String toLog = "Terminating role instance due to";
			for (String s : roleProblem.getCMAEvolutionStrategy().stopConditions.getMessages())
				toLog += ("  " + s);
			GamerLogger.log("CMAESManager", toLog);

			roleProblem.setPopulation(null);
			roleProblem.resetTotalUpdates();
			roleProblem.setMeanValueCombo(this.scaleDownIndividual(roleProblem.getCMAEvolutionStrategy().getMeanX()));

		}else { // Get new population

			double[][] pop = roleProblem.getCMAEvolutionStrategy().samplePopulation();

	        // Transform the initial population into a population of statistics for ContinuousMoves
	        // (i.e. each individual is represented by an instance of CompleteMoveStats, that contains:
	        // 1. the individual as a combination of parameter values rescaled from [-inf,+inf] to its
	        // own interval of feasible values
	        // 2. the sum of the fitness obtained by all evaluations of the individual
	        // 3. the number of times the individual has been evaluated
			roleProblem.setPopulation(this.scaleDownPopulation(pop));
			roleProblem.resetTotalUpdates();

		}

	}

	private double[] computeFitness(CompleteMoveStats[] population) {
		double[] fitness = new double[population.length];

		String popString = "POPULATION = ";
		String fitString = "FITNESS = [";

		for(int individualIndex = 0; individualIndex < population.length; individualIndex++) {

			ContinuousMove combo = (ContinuousMove) population[individualIndex].getTheMove();
			popString += "[";
			for(int paramIndex = 0; paramIndex < combo.getContinuousMove().length; paramIndex++) {
				popString += (" " + combo.getContinuousMove()[paramIndex]);
			}
			popString += " ]";

			// Compute fitness and invert it (i.e. fitness=-score) because CMA-ES minimizes the function, while we want to maximize
			if(population[individualIndex].getVisits() <= 0) {
				GamerLogger.logError("EvolutionManager", "CMAESManager - Impossible to compute fitness of population. Found individual with no visits (visits=" + population[individualIndex].getVisits() + ") to compute its fitness.");
				throw new RuntimeException("CMAESManager - Impossible to compute fitness of population. Found individual with no visits (visits=" + population[individualIndex].getVisits() + ") to compute its fitness.");
			}
			fitness[individualIndex] = -(population[individualIndex].getScoreSum()/((double)population[individualIndex].getVisits())); // -0.0 shouldn't be a problem here right?

			fitString += (" " + fitness[individualIndex]);

		}

		fitString += " ]";

		System.out.println();
		System.out.println(popString);
		System.out.println(fitString);
		System.out.println();

		return fitness;
	}

	public CompleteMoveStats updateMeanFitnessAndGetBest(SelfAdaptiveESProblemRepresentation roleProblem) {

		if(roleProblem.getMeanValueCombo().getVisits() <= 0) {
			GamerLogger.logError("EvolutionManager", "CMAESManager - Impossible to set fitness of mean value. The individual with mean value has no positive numebr of visits (visits=" + roleProblem.getMeanValueCombo().getVisits() + ") to compute its fitness.");
			throw new RuntimeException("CMAESManager - Impossible to set fitness of mean value. The individual with mean value has no positive numebr of visits (visits=" + roleProblem.getMeanValueCombo().getVisits() + ") to compute its fitness.");
		}
		double meanFitness = -(roleProblem.getMeanValueCombo().getScoreSum()/((double)roleProblem.getMeanValueCombo().getVisits()));

		roleProblem.setMeanValueCombo(null);

		return this.scaleDownIndividual(roleProblem.getCMAEvolutionStrategy().setFitnessOfMeanX(meanFitness).getX());

	}

	public CompleteMoveStats updatePopulationFitnessAndGetBestSoFar(SelfAdaptiveESProblemRepresentation roleProblem) {

		roleProblem.getCMAEvolutionStrategy().updateDistribution(this.computeFitness(roleProblem.getPopulation()));

		return this.getBestSoFar(roleProblem);

	}

	public CompleteMoveStats getBestSoFar(SelfAdaptiveESProblemRepresentation roleProblem) {

		// Return the best combination found so far
		if(this.useMean) {
			return this.scaleDownIndividual(roleProblem.getCMAEvolutionStrategy().getMeanX());
		}else {
			return this.scaleDownIndividual(roleProblem.getCMAEvolutionStrategy().getBestX());
		}

	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "USE_MEAN = " + this.useMean;

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

}
