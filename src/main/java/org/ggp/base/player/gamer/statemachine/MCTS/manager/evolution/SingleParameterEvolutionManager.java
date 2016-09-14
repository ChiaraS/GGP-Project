package org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.logging.GamerLogger;


public class SingleParameterEvolutionManager {

	private Random random;

	private double explorationConstant;

	private double valueOffset;

	// Number of times any of the individuals had its fitness updated.
	private int numUpdates;

	// Individuals to be evaluated and evolved (for now they are just
	// pre-fixed values for the parameter K of the CADIABetaComputer).
	private Individual[] individuals;

	private int currentSelectedIndividual;

	public SingleParameterEvolutionManager(Random random, double explorationConstant, double valueOffset, Individual[] individuals) {

		this.random = random;

		this.explorationConstant = explorationConstant;

		this.valueOffset = valueOffset;

		this.numUpdates = 0;

		this.individuals = individuals;

		/*
		individuals = new Individual[8];

		individuals[0] = new Individual(10);
		individuals[1] = new Individual(50);
		individuals[2] = new Individual(100);
		individuals[3] = new Individual(250);
		individuals[4] = new Individual(500);
		individuals[5] = new Individual(750);
		individuals[6] = new Individual(1000);
		individuals[7] = new Individual(2000);

		*/

		this.currentSelectedIndividual = -1;

	}

	public double selectNextIndividual(){

		// This should mean that the fitness hasn't been computed for any of the individuals yet,
		// thus we return a random one.
		if(this.numUpdates == 0){
			this.currentSelectedIndividual = random.nextInt(this.individuals.length);
			return this.individuals[this.currentSelectedIndividual].getParameter();
		}

		double maxValue = -1;
		double[] individualsValues = new double[this.individuals.length];

		List<Integer> selectedIndividualsIndices = new ArrayList<Integer>();

		for(int i = 0; i < this.individuals.length; i++){

			individualsValues[i] = this.computeIndividualsValue(individuals[i]);

			if(individualsValues[i] == Double.MAX_VALUE){
				maxValue = individualsValues[i];
				selectedIndividualsIndices.add(new Integer(i));
			}else if(individualsValues[i] > maxValue){
				maxValue = individualsValues[i];
			}
		}

		// NOTE: as is, this code always selects an "unexplored" individual if there is one, even if
		// there is an individual that has a value higher than (Double.MAX_VALUE-this.valueOffset).
		if(maxValue < Double.MAX_VALUE){
			for(int i = 0; i < individualsValues.length; i++){
				if(individualsValues[i] >= (maxValue-this.valueOffset)){
					selectedIndividualsIndices.add(new Integer(i));
				}
			}
		}

		// Extra check (should never be true).
		if(selectedIndividualsIndices.isEmpty()){
			throw new RuntimeException("Evolution manager, K selection: detected no individuals with fitness value higher than -1.");
		}

		this.currentSelectedIndividual = selectedIndividualsIndices.get(this.random.nextInt(selectedIndividualsIndices.size())).intValue();
		return this.individuals[this.currentSelectedIndividual].getParameter();

	}

	private double computeIndividualsValue(Individual individual){

		int numEvaluations = individual.getNumEvaluations();
		int totalFitness = individual.getTotalFitness();

		/**
		 * Extra check to make sure that neither the numEvaluations nor the
		 * totalFitness exceed the maximum feasible value for an int type.
		 * TODO: remove this check once you are reasonably sure that this
		 * can never happen.
		 */
		if(numEvaluations < 0 || totalFitness < 0){
			throw new RuntimeException("Negative value for numEvaluations and/or totalFitness of an individual: numEvaluations=" + numEvaluations + ", totalFitness=" + totalFitness + ".");
		}

		if(numEvaluations == 0){
			return Double.MAX_VALUE;
		}

		return this.computeExploitation(numEvaluations, totalFitness) + this.computeExploration(numEvaluations);

	}

	private double computeExploitation(int numEvaluations, int totalFitness){

		// Assume that the totalFitness has already been checked to be positive and the numEvaluations to be non-negative.

		return ((((double)totalFitness)/((double)numEvaluations))/ 100.0);

	}

	private double computeExploration(int numEvaluations){

		return (this.explorationConstant * (Math.sqrt(Math.log(this.numUpdates)/((double)numEvaluations))));

	}

	public void updateFitness(int goal){

		this.individuals[this.currentSelectedIndividual].updateFitness(goal);

	}

	public void logIndividualsState(){

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "EvoManager", "");

		for(int i = 0; i < this.individuals.length; i++){

			GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "EvoManager", "PARAM_VALUE =;" + individuals[i].getParameter() + ";EVALS =;" + individuals[i].getNumEvaluations() + ";TOT_FITNESS =;" + individuals[i].getTotalFitness() + ";AVG_FITNESS =;" + individuals[i].getAverageFitness());

		}

	}

	public String getEvolutionManagerParameters() {
		return "EXPLORATION_CONSTANT = " + this.explorationConstant + ", VALUE_OFFSET = " + this.valueOffset;
	}

	public String printEvolutionManager() {
		String params = this.getEvolutionManagerParameters();

		if(params != null){
			return "(EVOLUTION_MANAGER = " + this.getClass().getSimpleName() + ", " + params + ")";
		}else{
			return "(EVOLUTION_MANAGER = " + this.getClass().getSimpleName() + ")";
		}
	}

}
