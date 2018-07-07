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
	 * True if the initial solution must be set as a random point in the cmaEsBoundaries interval for each
	 * parameter, false if it must be the central point of the interval (i.e. (rightExtreme + leftExtreme)/2).
	 */
	private boolean randomInitialSolution;

	/**
	 * If true we set to -1 the threshold on the fitness difference between solutions named stopTolFunHist.
	 * This means that CMA-ES will keep tuning even if the observed difference in fitness is very low or even null.
	 */
	private boolean disableStopTolFunHistTermination;

	/**
	 * If true we disable the check on the threshold on the fitness difference between solutions named stopTolFun.
	 * This means that CMA-ES will keep tuning even if the observed difference in fitness is very low or even null.
	 */
	private boolean disableStopTolFunTermination;

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
	 * Gives the interval in which we consider feasible the value of any of the variables for the CMA-ES. Before computing
	 * the fitness of any individual we will re-scale these values to the actual feasible interval of each parameter being
	 * tuned.
	 */
	private Interval cmaEsBoundaries;

	/**
	 * Re-scaling factor for the penalty that is added to solutions that have been repaired because they were not feasible.
	 */
	private double alpha;

	/**
	 * Value used to compute the fitness from the reward as follows: fitness(combo)=maxReward-reward(combo)
	 * CMA-ES minimizes the fitness, but we want to maximize the reward so we need to set the fitness by
	 * inverting the reward.
	 */
	private double maxReward;

	public CMAESManager(GameDependentParameters gameDependentParameters, Random random, GamerSettings gamerSettings,
			SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.randomInitialSolution =  gamerSettings.getBooleanPropertyValue("EvolutionManager.randomInitialSolution");

		this.disableStopTolFunHistTermination = gamerSettings.getBooleanPropertyValue("EvolutionManager.disableStopTolFunHistTermination");

		this.disableStopTolFunTermination = gamerSettings.getBooleanPropertyValue("EvolutionManager.disableStopTolFunTermination");

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

		this.alpha = gamerSettings.getDoublePropertyValue("EvolutionManager.alpha");

		this.maxReward = gamerSettings.getDoublePropertyValue("EvolutionManager.maxReward");
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
	public SelfAdaptiveESProblemRepresentation createRoleProblemWithInitialPopulation() {

		CMAEvolutionStrategy cma = new CMAEvolutionStrategy();

		cma.turnOffPrinting = true;

		//System.out.println("Before = " + cma.options.stopFitness);

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
        if(this.randomInitialSolution) {
        	// Set initial X by getting random value in the cmaEsBoundaries interval for each parameter
        	double[] l = new double[this.continuousParametersManager.getNumTunableParameters()];
        	double[] u = new double[this.continuousParametersManager.getNumTunableParameters()];
        	for(int paramIndex = 0; paramIndex < this.continuousParametersManager.getNumTunableParameters(); paramIndex++) {
        		l[paramIndex] = this.cmaEsBoundaries.getLeftExtreme();
        		u[paramIndex] = this.cmaEsBoundaries.getRightExtreme();
        	}

        	cma.setInitialX(l, u);

        }else {
        	// Set initial X (same as in default settings file)
        	cma.setInitialX(0.5*(this.cmaEsBoundaries.getRightExtreme()+this.cmaEsBoundaries.getLeftExtreme()));
        }
        // Set minimum fitness we want to reach and then stop.
        // We don't want to stop even if we find the minimum possible fitness, so we set this to the minimum possible value.
        cma.options.stopFitness = -Double.MAX_VALUE;
        // Set to off the logging on file of the CMA-ES instance
        cma.options.writeDisplayToFile = 0;
        // Disable minimum change in function value that must be observed
        if(this.disableStopTolFunHistTermination) {
        	cma.options.stopTolFunHist = -1;
        }
        if(this.disableStopTolFunTermination) {
        	cma.options.stopTolFun = -1;
        }

        //System.out.println("After = " + cma.options.stopFitness);

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

		// The repaired population
		double[][] repairedPopulation = new double[pop.length][];

		// For each parameter in each individual, true if it has been repaired, false if it's still the original value.
		boolean[][] repaired = new boolean[pop.length][];

		double[] penalty = new double[pop.length];

        this.repairPopulationAndComputePenalty(pop, repairedPopulation, repaired, penalty);

        // Transform the initial (repaired) population into a population of statistics for ContinuousMoves
        // (i.e. each individual is represented by an instance of CompleteMoveStats, that contains:
        // 1. the individual as a combination of parameter values rescaled from [-inf,+inf] to its
        // own interval of feasible values
        // 2. the sum of the fitness obtained by all evaluations of the individual
        // 3. the number of times the individual has been evaluated
		return new SelfAdaptiveESProblemRepresentation(cma, this.rescalePopulation(repairedPopulation), repaired, penalty);

	}

	private void repairPopulationAndComputePenalty(double[][] pop, double[][] repairedPopulation,
			boolean[][] repaired, double[] penalty){

		for(int individualIndex = 0; individualIndex < pop.length; individualIndex++) {

			repairedPopulation[individualIndex] = new double[pop[individualIndex].length];
			repaired[individualIndex] = new boolean[pop[individualIndex].length];

			penalty[individualIndex] = this.repairIndividualAndComputePenalty(pop[individualIndex],
					repairedPopulation[individualIndex], repaired[individualIndex]);

		}

	}

	/**
	 * Given an individual, fills the array repairedIndividual so that if there is any infeasible value in the
	 * individual it is substituted with a feasible value. It also memorizes in the repaired array for each
	 * value whether it was repaired or not and return the penalty for the fitness of the individual.
	 *
	 * @param individual
	 * @param repairedIndividual
	 * @param repaired
	 * @return
	 */
	private double repairIndividualAndComputePenalty(double[] individual, double[] repairedIndividual, boolean[] repaired){

		double penalty = 0;

		boolean computePenalty = false;

		double[] difference = new double[individual.length];

		for(int paramIndex = 0; paramIndex < individual.length; paramIndex++) {
			// Check if the value is feasible
			if(this.cmaEsBoundaries.contains(individual[paramIndex])) {
				repairedIndividual[paramIndex] = individual[paramIndex];
				repaired[paramIndex] = false;
				difference[paramIndex] = 0;
			}else {
				if(individual[paramIndex] < this.cmaEsBoundaries.getLeftExtreme()) {
					repairedIndividual[paramIndex] = this.cmaEsBoundaries.getLeftExtreme();
				}else { // If it's not included in the interval nor smaller than the left extreme, then it must be greater than the right extreme
					repairedIndividual[paramIndex] = this.cmaEsBoundaries.getRightExtreme();
				}
				repaired[paramIndex] = true;
				computePenalty = true; // If we repair at least one individual, we have a non-zero penalty to compute
				difference[paramIndex] = (repairedIndividual[paramIndex] - individual[paramIndex]);
			}
		}

		if(computePenalty) {
			penalty = this.alpha * this.squareNorm(difference);
		}

		return penalty;

	}

	/**
	 * Computes the square of the norm of the given vector
	 *
	 * @param vector
	 * @return
	 */
	private double squareNorm(double[] vector) {

		double norm = 0;

		for(int i = 0; i < vector.length; i++) {
			norm += (vector[i] * vector[i]);
		}

		return norm;

	}

	/**
	 * Transforms the population sampled by CMA-ES to a population of individuals with their statistics
	 * by scaling down the values of the parameters from [-inf,+inf] to the interval of feasible values
	 * for each parameter.
	 * @return
	 */
	private CompleteMoveStats[] rescalePopulation(double[][] pop) {
        // Transform the initial population into a population of statistics for ContinuousMoves
        // (i.e. each individual is represented by an instance of CompleteMoveStats, that contains:
        // 1. the individual as a combination of parameter values rescaled from [-inf,+inf] to its
        // own interval of feasible values
        // 2. the sum of the fitness obtained by all evaluations of the individual
        // 3. the number of times the individual has been evaluated
		CompleteMoveStats[] population = new CompleteMoveStats[pop.length];

		for(int individualIndex = 0; individualIndex < pop.length; individualIndex++) {
			population[individualIndex] = this.rescaleIndividual(pop[individualIndex]);
		}

		return population;
	}

	private CompleteMoveStats rescaleIndividual(double[] individual) {
		double[] rescaledValuesOfIndividual = new double[individual.length];
		for(int paramIndex = 0; paramIndex < individual.length; paramIndex++) {
			rescaledValuesOfIndividual[paramIndex] = this.valueRescaler.mapToInterval(individual[paramIndex],
					this.cmaEsBoundaries.getLeftExtreme(), this.cmaEsBoundaries.getRightExtreme(),
					this.continuousParametersManager.getPossibleValuesInterval(paramIndex).getLeftExtreme(),
					this.continuousParametersManager.getPossibleValuesInterval(paramIndex).getRightExtreme());
		}
		return new CompleteMoveStats(new ContinuousMove(rescaledValuesOfIndividual));

	}

	public void evolvePopulation(SelfAdaptiveESProblemRepresentation roleProblem) {

		// If a role problem is passed to this function we know that the CMA-ES has not been stopped yet for the role

		roleProblem.getCMAEvolutionStrategy().updateDistribution(this.computeFitness(roleProblem));

		if(roleProblem.getCMAEvolutionStrategy().stopConditions.getNumber() > 0) { // Stop optimization and return mean solution to be evaluated

			String toLog = "Terminating role instance due to";
			for (String s : roleProblem.getCMAEvolutionStrategy().stopConditions.getMessages())
				toLog += ("  " + s);
			GamerLogger.log("CMAESManager", toLog);

			roleProblem.setPopulation(null, null, null);
			roleProblem.resetTotalUpdates();

			double[] meanX = roleProblem.getCMAEvolutionStrategy().getMeanX();
			double[] repairedMeanX = new double[meanX.length];
			boolean[] repaired = new boolean[meanX.length];
			double meanPenalty = this.repairIndividualAndComputePenalty(meanX, repairedMeanX, repaired);

			roleProblem.setMeanValueCombo(this.rescaleIndividual(repairedMeanX), repaired, meanPenalty);

		}else { // Get new population

			double[][] pop = roleProblem.getCMAEvolutionStrategy().samplePopulation();

			// The repaired population
			double[][] repairedPopulation = new double[pop.length][];

			// For each parameter in each individual, true if it has been repaired, false if it's still the original value.
			boolean[][] repaired = new boolean[pop.length][];

			double[] penalty = new double[pop.length];

	        this.repairPopulationAndComputePenalty(pop, repairedPopulation, repaired, penalty);

	        // Transform the initial population into a population of statistics for ContinuousMoves
	        // (i.e. each individual is represented by an instance of CompleteMoveStats, that contains:
	        // 1. the individual as a combination of parameter values rescaled from [-inf,+inf] to its
	        // own interval of feasible values
	        // 2. the sum of the fitness obtained by all evaluations of the individual
	        // 3. the number of times the individual has been evaluated
			roleProblem.setPopulation(this.rescalePopulation(repairedPopulation), repaired, penalty);
			roleProblem.resetTotalUpdates();

		}

	}

	private double[] computeFitness(SelfAdaptiveESProblemRepresentation roleProblem) {
		double[] fitness = new double[roleProblem.getPopulation().length];

		//String popString = "POPULATION = ";
		//String fitString = "FITNESS = [";

		for(int individualIndex = 0; individualIndex < roleProblem.getPopulation().length; individualIndex++) {

			//ContinuousMove combo = (ContinuousMove) roleProblem.getPopulation()[individualIndex].getTheMove();
			//popString += "[";
			//for(int paramIndex = 0; paramIndex < combo.getContinuousMove().length; paramIndex++) {
			//	popString += (" " + combo.getContinuousMove()[paramIndex]);
			//}
			//popString += " ]";

			// Compute fitness and invert it (i.e. fitness=-score) because CMA-ES minimizes the function, while we want to maximize
			// Also, add the penalty that was computed in advance
			if(roleProblem.getPopulation()[individualIndex].getVisits() <= 0) {
				GamerLogger.logError("EvolutionManager", "CMAESManager - Impossible to compute fitness of population. Found individual with no visits (visits=" + roleProblem.getPopulation()[individualIndex].getVisits() + ") to compute its fitness.");
				throw new RuntimeException("CMAESManager - Impossible to compute fitness of population. Found individual with no visits (visits=" + roleProblem.getPopulation()[individualIndex].getVisits() + ") to compute its fitness.");
			}
			fitness[individualIndex] = (this.maxReward - (roleProblem.getPopulation()[individualIndex].getScoreSum()/((double)roleProblem.getPopulation()[individualIndex].getVisits()))) + roleProblem.getPenalty()[individualIndex]; // -0.0 shouldn't be a problem here right?

			//fitString += (" " + fitness[individualIndex]);

		}

		//fitString += " ]";

		//System.out.println();
		//System.out.println(popString);
		//System.out.println(fitString);
		//System.out.println();

		return fitness;
	}

	public CompleteMoveStats updateMeanFitnessAndGetBest(SelfAdaptiveESProblemRepresentation roleProblem) {

		if(roleProblem.getMeanValueCombo().getVisits() <= 0) {
			GamerLogger.logError("EvolutionManager", "CMAESManager - Impossible to set fitness of mean value. The individual with mean value has no positive numebr of visits (visits=" + roleProblem.getMeanValueCombo().getVisits() + ") to compute its fitness.");
			throw new RuntimeException("CMAESManager - Impossible to set fitness of mean value. The individual with mean value has no positive numebr of visits (visits=" + roleProblem.getMeanValueCombo().getVisits() + ") to compute its fitness.");
		}
		double meanFitness = ( -(roleProblem.getMeanValueCombo().getScoreSum()/((double)roleProblem.getMeanValueCombo().getVisits())) ) + roleProblem.getMeanPenalty();

		roleProblem.setMeanValueCombo(null, null, 0);

		// Update fitness of mean solution and get best solution
		double[] bestX = roleProblem.getCMAEvolutionStrategy().setFitnessOfMeanX(meanFitness).getX();
		// It might be possible that the solution is infeasible, so check if it needs to be repaired
		double[] repairedBestX = new double[bestX.length];
		boolean[] repaired = new boolean[bestX.length];
		// For the best solution we will not update the fitness, so here we don't need to memorize any possible penalty
		this.repairIndividualAndComputePenalty(bestX, repairedBestX, repaired);

		return this.rescaleIndividual(repairedBestX);

	}

	public CompleteMoveStats updatePopulationFitnessAndGetBestSoFar(SelfAdaptiveESProblemRepresentation roleProblem) {

		roleProblem.getCMAEvolutionStrategy().updateDistribution(this.computeFitness(roleProblem));

		return this.getBestSoFar(roleProblem);

	}

	public CompleteMoveStats getBestSoFar(SelfAdaptiveESProblemRepresentation roleProblem) {

		// Return the best combination found so far

		double[] X;
		if(this.useMean) {
			X = roleProblem.getCMAEvolutionStrategy().getMeanX();
		}else {
			X = roleProblem.getCMAEvolutionStrategy().getBestX();
		}

		// It might be possible that the solution is infeasible, so check if it needs to be repaired
		double[] repairedX = new double[X.length];
		boolean[] repaired = new boolean[X.length];
		// For the best solution we will not update the fitness, so here we don't need to memorize any possible penalty
		this.repairIndividualAndComputePenalty(X, repairedX, repaired);

		return this.rescaleIndividual(repairedX);

	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);

		String params = indentation + "RANDOM_INITIAL_SOLUTION = " + this.randomInitialSolution +
				indentation + "DISABLE_STOP_TOL_FUN_HIST_TERMIANTION = " + this.disableStopTolFunHistTermination +
				indentation + "DISABLE_STOP_TOL_FUN_TERMIANTION = " + this.disableStopTolFunTermination +
				indentation + "USE_MEAN = " + this.useMean +
				indentation + "VALUE_RESCALER = " + this.valueRescaler.printComponent(indentation + "  ") +
				indentation + "CMA_ES_BOUNDARIES = " + this.cmaEsBoundaries.toString() +
				indentation + "ALPHA = " + this.alpha +
				indentation + "MAX_REWARD = " + this.maxReward +
				indentation + "INITIAL_STANDARD_DEVIATION = " + (0.3*(this.cmaEsBoundaries.getRightExtreme()-this.cmaEsBoundaries.getLeftExtreme()));

		if(superParams != null){
			return superParams + params;
		}else{
			return params;
		}

	}


	/*
	public static void main(String args[]) {

		double[] a = new double[]{0.3, 0.5, 0.7, 0.8};

		System.out.println(sqNorm(a));
	}

	private static double sqNorm(double[] vector) {

		double norm = 0;

		for(int i = 0; i < vector.length; i++) {
			norm += (vector[i] * vector[i]);
		}

		return norm;

	}
	*/

}
