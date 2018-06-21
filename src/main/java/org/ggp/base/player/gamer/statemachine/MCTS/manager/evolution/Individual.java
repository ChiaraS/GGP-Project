package org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution;

public class Individual {

	// Total fitness obtained by summing the fitness value obtained after each fitness evaluation.
	private double totalFitness;

	// Number of times the fitness of this individual has been evaluated.
	private int numEvaluations;

	public Individual(){

		this.totalFitness = 0;

		this.numEvaluations = 0;
	}

	public double getTotalFitness(){
		return this.totalFitness;
	}

	public int getNumEvaluations(){
		return this.numEvaluations;
	}


	public double getAverageFitness(){

		if(this.numEvaluations == 0){
			return Double.MAX_VALUE;
		}

		/**
		 * Extra check to make sure that neither the numEvaluations nor the
		 * totalFitness exceed the maximum feasible value for an int type.
		 * TODO: remove this check once you are reasonably sure that this
		 * can never happen.
		 */
		if(this.numEvaluations < 0 || this.totalFitness < 0){
			throw new RuntimeException("Negative value for numEvaluations and/or totalFitness of an individual: numEvaluations=" + this.numEvaluations + ", totalFitness=" + this.totalFitness + ".");
		}

		return (((double)this.totalFitness)/((double)this.numEvaluations));

	}

	public void updateFitness(double value){

		this.totalFitness += value;

		this.numEvaluations++;

	}

}
