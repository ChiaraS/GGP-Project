package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.util.logging.GamerLogger;

public class EvoProblemRepresentation {

	/**
	 * Population of combinations (individuals) for one of the roles being tuned
	 * to which this problem representation in associated.
	 */
	protected CompleteMoveStats[] population;

	/**
	 * Number of times any of the individuals in the current population has been updated.
	 * Must be reset every time the population changes.
	 */
	protected int totalUpdates;

	public EvoProblemRepresentation(CompleteMoveStats[] population) {
		if(population.length < 1) {
			GamerLogger.logError("ParametersTuner", "EvoProblemRepresentation - Impossible to create EvoProblemRepresentation! Specified population must have at least one individual!");
			throw new RuntimeException("EvoProblemRepresentation - Impossible to create EvoProblemRepresentation! Specified population must have at least one individual!");
		}

		this.population = population;
		//this.totalUpdates = 0;
	}

	public CompleteMoveStats[] getPopulation(){
		return this.population;
	}

	public int getTotalUpdates(){
		return this.totalUpdates;
	}

	public void incrementTotalUpdates(){
		this.totalUpdates++;
	}

	public void setTotalUpdates(int totalUpdates) {
		this.totalUpdates = totalUpdates;
	}

	public void resetTotalUpdates() {
		this.totalUpdates = 0;
	}

    /**
     * This method keeps factor*oldStatistic statistics. Factor should be in the interval [0,1].
     *
     * @param factor
     */
    public void decreaseStatistics(double factor){
		for(int i = 0; i < this.population.length; i++){
			this.population[i].decreaseByFactor(factor);
		}
    }
}
