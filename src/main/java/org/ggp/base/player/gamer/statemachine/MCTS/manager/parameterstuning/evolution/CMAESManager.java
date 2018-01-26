package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.rescalers.ValueRescaler;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ContinuousMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep.SelfAdaptiveESProblemRepresentation;
import org.ggp.base.util.Interval;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;

import csironi.ggp.course.utils.MyPair;
import inriacmaes.CMAEvolutionStrategy;

/**
 * This class manages the execution of the CMA-ES algorithm on each of the role problems.
 * Each role problem has its own instance of CMA-ES. For each role, CMA-ES is executed assuming that:
 * 1. We want to find the combination of values (one for each parameter/dimension) that MINIMIZES the fitness.
 * 2. For each parameter/dimension the value that is part of the optimal solution (i.e. combination) is in the
 *    interval specified by cmaEsBoundaries. (I.e. the optimal solution is in the hypercube [a,b]^n, where [a,b]
 *    is the interval specified by cmaEsBoundaries and n is the number of dimensions/parameters being tuned).
 * 3. The feasible values for each dimension/tuned parameter are also in the interval specified by cmaEsBoundaries.
 * To deal with the previous assumptions, this manager must take care of:
 * 1. Invert the rewards obtained by the parameter combinations, so that a parameter combination with maximum
 * 	  reward has minimum fitness and viceversa. This is done by setting fitness(combo)=-MCTSreward(combo).
 * 2. Whenever a population is returned by CMA-ES to be sampled, each parameter of each individual is rescaled
 *    by a function that maps the interval specified by cmaEsBoundaries to the interval that represents the actual
 *    domain of the parameter.
 * 3. CMA-ES might return an individual with one or more component values outside of the feasible interval specified
 *    by cmaEsBoundaries. In this case its fitness is computed by substituting the individual with the closest feasible
 *    individual and then adding a penalty to the fitness that is proportional to the distance between the infeasible
 *    individual and it closest feasible individual.
 *
 * Also, parameters like the initialStandardDeviation and the initialX (mean value) are computed according to the
 * dimension of the considered interval specified by cmaEsBoundaries. From the documentation (see footnote on page 29
 * of the tutorial) the optimum should be within the initial cube [m-3*sigma(1,...,1), m+3*sigma(1,...,1)], so if we
 * expect the optimum to be in the cube determined by the rescaled feasible values of all the tuned parameters (i.e.
 * the values specified by cmaEsBoundaries), than a good choice for m (i.e. initialX) and sigma (i.e. initialStandardDeviation)
 * seems to be 0.5*(b-a) and 0.3*(b-a) for each parameter.
 *
 * NOTE: all the settings and assumptions are based on the tutorial at https://www.lri.fr/~hansen/cmaesintro.html
 *       (https://arxiv.org/pdf/1604.00772.pdf) and on the practical hints on the source code page at
 *       https://www.lri.fr/~hansen/cmaes_inmatlab.html#java.
 *
 * @author c.sironi
 *
 */
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

	/**
	 * Re-scales the values returned by the CMA-ES algorithm for the population individuals into the scale of feasible
	 * values for the tuned parameters. I.e. we assume that the feasible values for each dimension optimized by CMA-ES are
	 * in the interval [0,1] and we use this function to re-scale such values so that the interval [0,1] corresponds
	 * to the actual feasible interval of values of each parameter. NOTE that the ValueRescaler must be able to re-scale
	 * also values that don't fall in the interval [0,1], because CMA-ES might return a point to sample outside of the
	 * feasible region. The CMA-ES interval [0,1] and the actual interval of feasible values for a given parameter are
	 * used by the value re-scaler to compute a function that re-scales any value in R to another value in R so that the
	 * interval [0,1] is rescaled to the interval of feasible values for the parameter.
	 */
	private ValueRescaler valueRescaler;

	/**
	 * Gives the interval in which we consider feasible the value of any of the variables
	 */
	private Interval cmaEsBoundaries;

	public CMAESManager(GameDependentParameters gameDependentParameters, Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.useMean = gamerSettings.getBooleanPropertyValue("EvolutionManager.useMean");

		try {
			this.valueRescaler = (ValueRescaler) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.VALUE_RESCALERS.getConcreteClasses(),
					gamerSettings.getPropertyValue("EvolutionManager.valueRescalerType"))).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating ValueRescaler " + gamerSettings.getPropertyValue("EvolutionManager.valueRescalerType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		this.cmaEsBoundaries = gamerSettings.getDoublePropertyIntervalValue("EvolutionManager.cmaEsBoundaries");
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		super.setReferences(sharedReferencesCollector);
		this.valueRescaler.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		super.clearComponent();
		this.valueRescaler.setUpComponent();
	}

	@Override
	public void setUpComponent() {
		super.setUpComponent();
		this.valueRescaler.setUpComponent();
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
        cma.setInitialStandardDeviation(0.3*(this.cmaEsBoundaries.getRightExtreme()-this.cmaEsBoundaries.getLeftExtreme()));
        // Set initial X (same as in default settings file)
        cma.setInitialX(0.5*(this.cmaEsBoundaries.getRightExtreme()-this.cmaEsBoundaries.getLeftExtreme()));
        // Set minimum fitness we want to reach
        cma.options.stopFitness = -101.0;
        // Set to off the logging on file of the CMA-ES instance
        cma.options.writeDisplayToFile = 0;
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

	private MyPair<double[][],double[]> repairPopulationAndComputePenalty(double[][] pop){

		double[][] repairedPopulation = new double[pop.length][];

		double[] penalty = new double[pop.length];

		for(int individualIndex = 0; individualIndex < pop.length; individualIndex++) {

			repairedPopulation[individualIndex] = new double[pop[individualIndex].length];
			penalty[individualIndex] = 0;

			double repairedValue;

			for(int paramIndex = 0; paramIndex < pop[individualIndex].length; paramIndex++) {
				// Check if the value is feasible
				if(this.cmaEsBoundaries.contains(pop[individualIndex][paramIndex])) {
					repairedPopulation[individualIndex][paramIndex] = pop[individualIndex][paramIndex];
				}else {
					repairedValue = this.repairValue(pop[individualIndex][paramIndex]);
					penalty[individualIndex] = penalty[individualIndex] + this.computePenalty(pop[individualIndex][paramIndex], repairedValue);
				}
			}
		}






		return new MyPair<double[][],double[]>(repairedPopulation, penalty);
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

		String params = indentation + "USE_MEAN = " + this.useMean +
				indentation + "VALUE_RESCALER = " + this.valueRescaler.printComponent(indentation + "  ") +
				indentation + "CMA_ES_BOUNDARIES = " + this.cmaEsBoundaries.toString() +
				indentation + "INITIAL_STANDARD_DEVIATION = " + (0.3*(this.cmaEsBoundaries.getRightExtreme()-this.cmaEsBoundaries.getLeftExtreme())) +
				indentation + "INITIAL_X = " + (0.5*(this.cmaEsBoundaries.getRightExtreme()-this.cmaEsBoundaries.getLeftExtreme()));

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}

}
