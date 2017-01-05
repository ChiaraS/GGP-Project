package org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.logging.GamerLogger;

/**
 * Evolution manager that manages one or more populations of individuals to be evolved.
 * The populations evolve independently from each other.
 *
 * For example this class can be used to evolve a population of parameter settings for
 * each role in the game independently. Or only evolve one population of parameter settings
 * for the role being played by the agent.
 *
 * NOTE: this class is not considered as a component of the search manager because as opposite
 * to other components it doesn't need to know details about the game being played to perform
 * its actions.
 *
 * @author C.Sironi
 *
 */
public class SingleParameterEvolutionManager{

	private Random random;

	private double evoC;

	private double evoValueOffset;

	/**
	 * If true the manager will re-scale the individuals'fintness between 0 and 1 with respect
	 * to the maximum and the minimum fitness of the currently considered individuals.
	 * If false the values will be rescaled using [0, 100] as extremes, like normally.
	 *
	 * NOTE: normalization should help stretching the values more, thus making more evident the difference
	 * between them and steering the manager towards using even more often the best values.
	 */
	private boolean useNormalization;

	/**
	 *  For each population, number of times any of the individuals in the population had its fitness updated.
	 *
	 *  Note that since for every simulation each population has one individual updated we could only keep a
	 *  single parameter that counts the updates for a population and the value will be the same for all other
	 *  populations.
	 */
	private int[] numUpdates;

	// For each population, individuals to be evaluated and evolved (for now they are just
	// pre-fixed values for the considered parameter).
	private Individual[][] populations;

	// For each population, the index in the corresponding array of individuals of the currently selected individual.
	private int[] currentSelectedIndividuals;

	public SingleParameterEvolutionManager(Random random, double explorationConstant, double valueOffset, boolean useNormalization) {

		this.random = random;
		this.evoC = explorationConstant;
		this.evoValueOffset = valueOffset;
		this.useNormalization = useNormalization;

		this.populations = null;
		this.numUpdates = null;
		this.currentSelectedIndividuals = null;

	}

	public void clear(){
		this.populations = null;
		this.numUpdates = null;
		this.currentSelectedIndividuals = null;
	}

	public void setUp(Individual[][] populations){
		this.populations = populations;
		this.numUpdates = new int[this.populations.length];
		this.currentSelectedIndividuals = new int[this.populations.length];

		for(int i = 0; i < this.populations.length; i++){
			this.numUpdates[i] = 0;
			this.currentSelectedIndividuals[i] = -1;
		}
	}

	// TODO: this shouldn't return a double but the set of the parameters that the individual is evaluating
	// Since for now we are using only single double values as parameters in each individual, we return an array
	// with a double value for each population.
	public double[] selectNextIndividuals(){

		double[] nextIndividuals = new double[this.populations.length];

		// For each population, select the best individual
		for(int i = 0; i < this.populations.length; i++){

			// This should mean that the fitness hasn't been computed for any of the individuals yet,
			// thus we return a random one.
			if(this.numUpdates[i] == 0){
				this.currentSelectedIndividuals[i] = random.nextInt(this.populations[i].length);
				nextIndividuals[i] = this.populations[i][this.currentSelectedIndividuals[i]].getParameter();
			}else{

				double minExtreme = 0.0;
				double maxExtreme = 100.0;
				double avgFitness;

				// If we want to normalize the values of the fitness before selecting an individual...
				if(this.useNormalization){

					// We need to compute the extremes in which to normalize
					minExtreme = Double.MAX_VALUE;
					maxExtreme = -Double.MAX_VALUE;

					for(int j = 0; j < this.populations[i].length; j++){

						int individualEvaluations = this.populations[i][j].getNumEvaluations();
						int totalFitness = this.populations[i][j].getTotalFitness();

						if(individualEvaluations != 0){
							avgFitness = (((double)totalFitness)/((double)individualEvaluations));

							if(avgFitness < minExtreme){
								minExtreme = avgFitness;
							}

							if(avgFitness > maxExtreme){
								maxExtreme = avgFitness;
							}
						}
					}

					// If no max or min value has been found because no individual has been evaluated once yet,
					// or max and min values are the same, then initilize them to the standard expected extremes,
					// that is [0, 100]
					if(minExtreme >= maxExtreme){
						minExtreme = 0.0;
						maxExtreme = 100.0;
					}

				}

				double maxValue = -1;
				double[] individualsValues = new double[this.populations[i].length];

				List<Integer> selectedIndividualsIndices = new ArrayList<Integer>();

				for(int j = 0; j < this.populations[i].length; j++){

					individualsValues[j] = this.computeIndividualsValue(populations[i][j], this.numUpdates[i], minExtreme, maxExtreme);

					if(individualsValues[j] == Double.MAX_VALUE){
						maxValue = individualsValues[j];
						selectedIndividualsIndices.add(new Integer(j));
					}else if(individualsValues[j] > maxValue){
						maxValue = individualsValues[j];
					}
				}

				/*
				System.out.print("Values for population " + i + ": [ ");
				for(int j = 0; j < individualsValues.length; j++){
					System.out.print(individualsValues[j] + " ");
				}
				System.out.println("]");
				*/


				// NOTE: as is, this code always selects an "unexplored" individual if there is one, even if
				// there is an individual that has a value higher than (Double.MAX_VALUE-this.valueOffset).
				if(maxValue < Double.MAX_VALUE){
					for(int j = 0; j < individualsValues.length; j++){
						if(individualsValues[j] >= (maxValue-this.evoValueOffset)){
							selectedIndividualsIndices.add(new Integer(j));
						}
					}
				}

				// Extra check (should never be true).
				if(selectedIndividualsIndices.isEmpty()){
					throw new RuntimeException("Evolution manager - SelectNextIndividuals(): detected no individuals with fitness value higher than -1.");
				}

				this.currentSelectedIndividuals[i] = selectedIndividualsIndices.get(this.random.nextInt(selectedIndividualsIndices.size())).intValue();
				nextIndividuals[i] = this.populations[i][this.currentSelectedIndividuals[i]].getParameter();

			}

		}

		/*
		System.out.print("Returning best individuals: [ ");
		for(int j = 0; j < nextIndividuals.length; j++){
			System.out.print(nextIndividuals[j] + " ");
		}
		System.out.println("]");

		System.out.println();
		//System.out.println("C = " + nextIndividuals[0]);
		*/

		return nextIndividuals;

	}

	private double computeIndividualsValue(Individual individual, int totPopulationEvaluations, double minExtreme, double maxExtreme){

		int individualEvaluations = individual.getNumEvaluations();
		int totalFitness = individual.getTotalFitness();

		/**
		 * Extra check to make sure that neither the numEvaluations nor the
		 * totalFitness exceed the maximum feasible value for an int type.
		 * TODO: remove this check once you are reasonably sure that this
		 * can never happen.
		 */
		if(individualEvaluations < 0 || totalFitness < 0){
			throw new RuntimeException("Negative value for numEvaluations and/or totalFitness of an individual: numEvaluations=" + individualEvaluations + ", totalFitness=" + totalFitness + ".");
		}

		if(individualEvaluations == 0){
			return Double.MAX_VALUE;
		}

		return this.computeExploitation(individualEvaluations, totalFitness, minExtreme, maxExtreme) + this.computeExploration(totPopulationEvaluations, individualEvaluations);

	}

	private double computeExploitation(int numEvaluations, int totalFitness, double minExtreme, double maxExtreme){

		// Assume that the totalFitness has already been checked to be positive and the numEvaluations to be non-negative.

		return this.normalize(((double)totalFitness)/((double)numEvaluations), minExtreme, maxExtreme);

	}

	private double normalize(double value, double leftExtreme, double rightExtreme){

		return (value - leftExtreme)/(rightExtreme - leftExtreme);

	}

	private double computeExploration(int totPopulationEvaluations, int individualEvaluations){

		return (this.evoC * (Math.sqrt(Math.log(totPopulationEvaluations)/((double)individualEvaluations))));

	}

	public void updateFitness(int[] goal){

		for(int i = 0; i < goal.length; i++){
			this.populations[i][this.currentSelectedIndividuals[i]].updateFitness(goal[i]);

			this.numUpdates[i]++;
		}

	}

	public void logIndividualsState(){

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "EvoManager", "");

		for(int i = 0; i < this.populations.length; i++){

			for(int j = 0; j < this.populations[i].length; j++){
				GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "EvoManager", "POPULATION=;" + i + ";PARAM_VALUE =;" + populations[i][j].getParameter() + ";EVALS =;" + populations[i][j].getNumEvaluations() + ";TOT_FITNESS =;" + populations[i][j].getTotalFitness() + ";AVG_FITNESS =;" + populations[i][j].getAverageFitness());
			}

		}

	}

	public String getEvolutionManagerParameters(String indentation) {

		String params = indentation + "EXPLORATION_CONSTANT = " + this.evoC + indentation + "VALUE_OFFSET = " + this.evoValueOffset + indentation + "USE_NORMALIZATION = " + this.useNormalization;

		if(this.numUpdates != null){

			String numUpdatesString = "[ ";

			for(int i = 0; i < this.numUpdates.length; i++){

				numUpdatesString += this.numUpdates[i] + " ";

			}

			numUpdatesString += "]";

			params += indentation + "num_updates = " + numUpdatesString;
		}else{
			params += indentation + "num_updates = null";
		}

		if(this.populations != null){

			String populationsString = "[ ";

			for(int i = 0; i < this.populations.length; i++){

				if(this.populations[i] != null){

					populationsString += "[ ";

					for(int j = 0; j < this.populations[i].length; j++){

						populationsString += this.populations[i][j].getParameter() + " ";

					}

					populationsString += "] ";

				}else{
					populationsString += "null ";
				}

			}

			populationsString += "]";

			params += indentation + "populations = " + populationsString;


		}else{
			params += indentation + "populations = null";
		}

		if(this.currentSelectedIndividuals != null){

			String currentSelectedIndividualsString = "[ ";

			for(int i = 0; i < this.currentSelectedIndividuals.length; i++){

				currentSelectedIndividualsString += this.currentSelectedIndividuals[i] + " ";

			}

			currentSelectedIndividualsString += "]";

			params += indentation + "current_selected_individuals_indices = " + currentSelectedIndividualsString;
		}else{
			params += indentation + "current_selected_individuals_indices = null";
		}

		return params;
	}

	public String printEvolutionManager(String indentation) {
		String params = this.getEvolutionManagerParameters(indentation);

		if(params != null){
			return this.getClass().getSimpleName() + params;
		}else{
			return this.getClass().getSimpleName();
		}
	}

	public int getNumPopulations() {
		return this.populations.length;
	}

}
