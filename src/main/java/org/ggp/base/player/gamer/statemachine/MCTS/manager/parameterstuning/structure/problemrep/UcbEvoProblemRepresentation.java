package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.problemrep;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.CompleteMoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.FixedMab;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs.IncrementalMab;


public class UcbEvoProblemRepresentation extends EvoProblemRepresentation{

	/**
	 * The global Multi-Armed Bandit problem that keeps track of statistics for the
	 * n-dimensional tuple (n = num tuned parameters).
	 * Its arms correspond to the possible combinatorial moves (individuals) evaluated so far.
	 */
	private IncrementalMab globalMab;

	/**
	 * For each parameter, a multi-armed bandit problem where each arm corresponds to a possible
	 * value that can be assigned to that parameter. Keep track of the statistics for the n
	 * 1-dimensional tuples (one for each parameter being tuned).
	 */
	private FixedMab[] localMabs;

	public UcbEvoProblemRepresentation(CompleteMoveStats[] population, int[] classesLength) {

		super(population);

		this.globalMab = new IncrementalMab();

		this.localMabs = new FixedMab[classesLength.length];

		for(int i = 0; i < this.localMabs.length; i++){
			this.localMabs[i] = new FixedMab(classesLength[i]);
		}

	}

	public IncrementalMab getGlobalMab(){
		return this.globalMab;
	}

	public FixedMab[] getLocalMabs(){
		return this.localMabs;
	}

    /**
     * This method keeps factor*oldStatistic statistics. Factor should be in the interval [0,1].
     *
     * @param factor
     */
    @Override
	public void decreaseStatistics(double factor){
    	super.decreaseStatistics(factor);
    	this.globalMab.decreaseStatistics(factor);
    	for(int i = 0; i < this.localMabs.length; i++){
    		this.localMabs[i].decreaseStatistics(factor);
    	}
    }

}
